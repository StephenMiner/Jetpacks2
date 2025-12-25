package me.stephenminer.jetpacks2.item;

import me.stephenminer.jetpacks2.Jetpacks2;
import me.stephenminer.jetpacks2.config.ItemFile;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ItemBuilder {
    private final Jetpacks2 plugin;

    public ItemBuilder(){
        this.plugin = JavaPlugin.getPlugin(Jetpacks2.class);
    }


    /**
     * Takes a string formatted as "attribute,modifier-amount,slot"
     * and creates an AttributeModifier, adding it to the provided ItemMeta
     * @param str formatted as "attribute,modifier-amount,slot"
     * @param meta The ItemMeta to apply the AttributeModifier on
     */
    public void parseAddAttribute(ItemMeta meta, String str){
        String[] unbox = str.split(",");
        if (unbox.length < 3){
            plugin.getLogger().warning("ItemBuilder error: parseAttribute(): " + str + " does not contain enough parameters!");
            return;
        }
        Attribute attribute = findAttribute(unbox[0]);
        if (attribute == null) return;
        double mod;
        try{
            mod = Double.parseDouble(unbox[1]);
        } catch (Exception e){
            plugin.getLogger().warning("ItemBuilder error: parseAttribute(): Failed to read a double (decimal number) from " + unbox[1] + ", defaulting to 0");
            mod = 0;
        }
        EquipmentSlot slot;
        try {
            slot = EquipmentSlot.valueOf(unbox[2]);
        }catch (Exception e){
            plugin.getLogger().warning("ItemBuilder error: parseAttribute(): Failed to read EquipmentSlot from " + unbox[2] + ". Defaulting to MAINHAND");
            slot = EquipmentSlot.HAND;
        }
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), attribute.getKey().getKey(), mod ,AttributeModifier.Operation.ADD_NUMBER, slot);
        meta.addAttributeModifier(attribute, modifier);
    }

    private Attribute findAttribute(String attrStr){
        Attribute attribute = null;
        try{
            attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(attrStr));
        }catch (Exception ignored){}
        if (attribute == null) {
            try {
                attribute = Registry.ATTRIBUTE.get(NamespacedKey.fromString(attrStr));
            }catch (Exception ignored){}
        }
        if (attribute == null){
            try {
                attribute = Attribute.valueOf(attrStr);
            }catch (Exception e){
                plugin.getLogger().warning("ItemBuilder error: findAttribute(): " + attrStr + " is not an Attribute!");
                return null;
            }
        }
        return attribute;
    }

    public Map<Enchantment, Integer> getEnchantments(List<String> strs) {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        if (strs == null)
            return null;
        for (String key : strs) {
            if (key == null)
                continue;
            String[] unbox = key.split(",");
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(unbox[0]));
            if (enchant == null){
                enchant = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(unbox[0]));
            }
            if (enchant == null)
                enchant = Registry.ENCHANTMENT.get(NamespacedKey.fromString(unbox[0]));
            if (enchant == null) {
                plugin.getLogger().warning("Skipping enchantment string: " + key + ". The enchantment " + unbox[0] + " isn't a real enchant!");
                continue;
            }
            int level = Math.max(1, Integer.parseInt(unbox[1]));
            enchants.put(enchant, level);
        }
        return enchants;
    }

    public ItemFlag[] getFlags(List<String> flagStrs) {
        List<ItemFlag> returnList = new ArrayList<>();
        if (flagStrs == null)
            return null;
        for (String key : flagStrs) {
            if (key == null)
                continue;
            try {
                returnList.add(ItemFlag.valueOf(key));
            }catch (Exception e){
                plugin.getLogger().warning("Failed to parse ItemFlag: " + key);
            }
        }
        return returnList.toArray(new ItemFlag[0]);
    }

    /**
     * If jetpackdata is present in the ItemFile, then the initial jetpack data (id and fuel) will
     * be placed in the ItemMeta's persistent data container.
     * @param meta - The ItemMeta we're editing the PersistentDataContainer of
     * @param file - The ItemFile we're reading from
     */
    public void addInitJetpackData(ItemMeta meta, ItemFile file){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(plugin.itemId, PersistentDataType.STRING, file.fileName());
        if (file.getConfig().contains("jetpack-item")){
            int fuel = 500;
            if (file.getConfig().contains("max-fuel"))
                fuel = file.getConfig().getInt("max-fuel");
            container.set(plugin.fuel, PersistentDataType.INTEGER, fuel);
            ((Damageable) meta).setMaxDamage(fuel);
        }

    }


    public ItemStack construct(ItemFile file){
        Material mat = Material.FEATHER;
        if (file.getConfig().contains("material"))
            mat = plugin.materialFromString(file.getConfig().getString("material"));
        if (mat == null) mat = Material.FEATHER;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (file.getConfig().contains("name")) {
            String name = plugin.formatColor(ChatColor.translateAlternateColorCodes('&', file.getConfig().getString("name")));
            meta.setDisplayName(name);
        }
        if (file.getConfig().contains("lore")){
            List<String> lore = file.getConfig().getStringList("lore");
            for (int i = 0; i < lore.size(); i++){
                String entry = lore.remove(i);
                lore.addLast(plugin.formatColor(ChatColor.translateAlternateColorCodes('&', entry)));
            }
            meta.setLore(lore);
        }
        if (file.getConfig().contains("flags")){
            List<String> flagStrs = file.getConfig().getStringList("flags");
            meta.addItemFlags(getFlags(flagStrs));
        }
        if (file.getConfig().contains("attributes")){
            List<String> attributeStrs = file.getConfig().getStringList("attributes");
            for (String attributeStr : attributeStrs){
                parseAddAttribute(meta, attributeStr);
            }
        }
        List<String> enchantStrs = file.getConfig().getStringList("enchantments");
        Map<Enchantment, Integer> enchants = getEnchantments(enchantStrs);
        for (Enchantment enchantment : enchants.keySet()){
            meta.addEnchant(enchantment, enchants.get(enchantment), true);
        }

        addInitJetpackData(meta, file);
        item.setItemMeta(meta);
        return item;
    }




}
