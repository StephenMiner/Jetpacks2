package me.stephenminer.jetpacks2.jetpack;

import me.stephenminer.jetpacks2.Jetpacks2;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public record JetpackData(String id, double thrust, double maxYVelocity, int maxFuel, int consumption, String fuelId, ActivationType activationType, EquipmentSlot[] slots) {

    public boolean usesFuel(){
        return !fuelId.equalsIgnoreCase("false");
    }


    public boolean matchesFuel(ItemStack item){
        if (!usesFuel()) return true;
        if (item == null) return false;
        Material type = item.getType();
        if (type.getKey().getKey().equalsIgnoreCase(fuelId)
                || type.getKey().toString().equalsIgnoreCase(fuelId)
                || type.name().equalsIgnoreCase(fuelId))
            return true;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Jetpacks2 plugin = JavaPlugin.getPlugin(Jetpacks2.class);
        if (!container.has(plugin.itemId, PersistentDataType.STRING)) return false;
        return container.get(plugin.itemId, PersistentDataType.STRING).equalsIgnoreCase(fuelId);
    }
}
