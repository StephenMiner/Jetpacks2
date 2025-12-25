package me.stephenminer.jetpacks2.listener;

import me.stephenminer.jetpacks2.Jetpacks2;
import me.stephenminer.jetpacks2.jetpack.ActivationType;
import me.stephenminer.jetpacks2.jetpack.FuelData;
import me.stephenminer.jetpacks2.jetpack.JetpackController;
import me.stephenminer.jetpacks2.jetpack.JetpackData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class JetpackListener implements Listener {
    private final Jetpacks2 plugin;
    private final JetpackController controller;

    public JetpackListener(){
        this.plugin = JavaPlugin.getPlugin(Jetpacks2.class);
        this.controller = new JetpackController();
    }


    @EventHandler
    public void clickActivation(PlayerInteractEvent event){
        if (!event.hasItem() || event.getAction() == Action.PHYSICAL) return;
        Player player = event.getPlayer();
        boolean found = false;
        boolean left = event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR;
        if (player.isSneaking())
            found = activateJetpack(player, left ? ActivationType.SHIFT_LEFT_CLICK : ActivationType.SHIFT_RIGHT_CLICK);
        else found = activateJetpack(player, left ? ActivationType.LEFT_CLICK : ActivationType.RIGHT_CLICK);
        if (found)
            event.setCancelled(true);
    }

    @EventHandler
    public void offhandActivation(PlayerSwapHandItemsEvent event){
        if (event.getMainHandItem() == null) return;
        Player player = event.getPlayer();
        boolean found = false;
        if (player.isSneaking())
            found = activateJetpack(player, ActivationType.SHIFT_OFFHAND);
        else found = activateJetpack(player, ActivationType.SHIFT_RIGHT_CLICK);

        if (found)
            event.setCancelled(true);
    }

    @EventHandler
    public void fuelJetpack(InventoryClickEvent event){
        ItemStack cursor = event.getCursor();
        ItemStack item  = event.getCurrentItem();
        if (item == null || cursor == null) return;

        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(plugin.itemId, PersistentDataType.STRING)) return;
        String jetpackId = container.get(plugin.itemId, PersistentDataType.STRING);
        if (plugin.jetpacks.containsKey(jetpackId)){
            FuelData fuel = findFuelData(cursor);
            if (fuel == null) return;
            JetpackData jetpack = plugin.jetpacks.get(jetpackId);
            boolean usedFuel = refuelJetpack(meta, fuel, jetpack);
            if (!usedFuel) return;
            item.setItemMeta(meta);
            cursor.setAmount(cursor.getAmount() - 1);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, Sound.ENTITY_BLAZE_SHOOT,1,1);
        }

    }

    @EventHandler
    public void blockFlight(PlayerToggleFlightEvent event){
        if (controller.usingJetpack(event.getPlayer())) {
            event.setCancelled(true);
        }
    }



    private boolean activateJetpack(Player player, ActivationType type){
        PlayerInventory inv = player.getInventory();
        Collection<JetpackData> vals = plugin.jetpacks.values();
        for (JetpackData data : vals){
            if (type != data.activationType()) continue;
            for (EquipmentSlot slot : data.slots()){
                ItemStack inSlot = inv.getItem(slot);
                if (itemIsJetpack(inSlot, data.id())) {
                    controller.attemptThrust(player, inSlot, data, slot);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean itemIsJetpack(ItemStack item, String jetpackId){
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (!container.has(plugin.itemId, PersistentDataType.STRING)) return false;
        return container.get(plugin.itemId, PersistentDataType.STRING).equalsIgnoreCase(jetpackId);
    }


    private FuelData findFuelData(ItemStack item){
        // First check if the item is tied to specific fuel data
        if (item.hasItemMeta()) {
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(plugin.itemId, PersistentDataType.STRING)){
                String id = container.get(plugin.itemId, PersistentDataType.STRING);
                if (plugin.fuelDataMap.containsKey(id))
                    return plugin.fuelDataMap.get(id);
            }
        }
        // Second check if the item's material is tied to generic fuel data
        if (plugin.fuelDataMap.containsKey(item.getType().getKey().toString()))
            return plugin.fuelDataMap.get(item.getType().getKey().toString());
        return null;
    }

    private boolean refuelJetpack(ItemMeta jetpackMeta, FuelData fuel, JetpackData jetpack){
        PersistentDataContainer container = jetpackMeta.getPersistentDataContainer();
        if (!container.has(plugin.fuel, PersistentDataType.INTEGER)) return false;
        int currentFuel = container.get(plugin.fuel, PersistentDataType.INTEGER);
        if (currentFuel + fuel.fillAmount() >= jetpack.maxFuel()) return false;
        container.set(plugin.fuel, PersistentDataType.INTEGER, currentFuel + fuel.fillAmount());
        return true;
    }


}

