package me.stephenminer.jetpacks2.jetpack;

import me.stephenminer.jetpacks2.Jetpacks2;
import me.stephenminer.jetpacks2.config.ItemFile;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class JetpackBuilder {
    private final Jetpacks2 plugin;
    public JetpackBuilder(){
        this.plugin = JavaPlugin.getPlugin(Jetpacks2.class);
    }

    public boolean hasJetpackData(ItemFile file){
        return file.getConfig().contains("jetpack-id");
    }

    public JetpackData loadJetpackRecord(ItemFile file){
        String jetpackId = file.getConfig().getString("jetpack-id");
        double thrust = 2;
        if (file.getConfig().contains("thrust"))
            thrust = file.getConfig().getDouble("thrust");
        double maxYVel = 2;
        if (file.getConfig().contains("max-y-velocity"))
            maxYVel = file.getConfig().getDouble("max-y-velocity");
        int maxFuel = 500;
        if (file.getConfig().contains("max-fuel"))
            maxFuel = file.getConfig().getInt("max-fuel");
        int consumption = 15;
        if (file.getConfig().contains("consumption"))
            consumption = file.getConfig().getInt("consumption");
        ActivationType activationType = ActivationType.OFFHAND_SWAP;
        if (file.getConfig().contains("activation-type"))
            activationType = ActivationType.valueOf(file.getConfig().getString("activation-type").toUpperCase());
        String fuelId =  "minecraft:blaze_powder";
        if (file.getConfig().contains("fuel-id"))
            fuelId = file.getConfig().getString("fuel-id");
        List<String> slotStrs = file.getConfig().getStringList("slots");
        EquipmentSlot[] slots = new EquipmentSlot[slotStrs.size()];
        for (int i = 0; i < slotStrs.size(); i++){
            String str = slotStrs.get(i);
            try {
                EquipmentSlot slot = EquipmentSlot.valueOf(str);
                slots[i] = slot;

            }catch (Exception e){
                plugin.getLogger().warning("Failed to parse equipment slot from " + str);
                slots[i] = EquipmentSlot.HAND;
            }
        }
        return new JetpackData(jetpackId, thrust, maxYVel, maxFuel, consumption, fuelId, activationType, slots);
    }
}
