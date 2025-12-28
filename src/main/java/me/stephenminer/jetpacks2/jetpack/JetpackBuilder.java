package me.stephenminer.jetpacks2.jetpack;

import me.stephenminer.jetpacks2.Jetpacks2;
import me.stephenminer.jetpacks2.config.ItemFile;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class JetpackBuilder {
    public static JetpackEffect DEFAULT_EFFECT = new JetpackEffect(Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE,1f,1f,true, false, new ParticleData[]{new ParticleData(Particle.LAVA,0, 0)});
    private final Jetpacks2 plugin;
    public JetpackBuilder(){
        this.plugin = JavaPlugin.getPlugin(Jetpacks2.class);
    }

    public boolean hasJetpackData(ItemFile file){
        return file.getConfig().getBoolean("jetpack-item");
    }

    public JetpackData loadJetpackRecord(ItemFile file){
        String jetpackId = file.fileName();
        double thrust = 2;
        if (file.getConfig().contains("thrust"))
            thrust = file.getConfig().getDouble("thrust");
        double maxYVel = 2;
        if (file.getConfig().contains("max-y-velocity"))
            maxYVel = file.getConfig().getDouble("max-y-velocity");
        double horizontalVel = 0.2d;
        if (file.getConfig().contains("horizontal-velocity"))
            horizontalVel = file.getConfig().getDouble("horizontal-velocity");
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
        if (file.getConfig().contains("uses-fuel"))
            fuelId = file.getConfig().getString("uses-fuel");
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
        JetpackEffect effect = createEffect(file);
        return new JetpackData(jetpackId, thrust, maxYVel, horizontalVel, maxFuel, consumption, fuelId, activationType, slots, effect);
    }

    public JetpackEffect createEffect(ItemFile file){
        if (!file.getConfig().contains("effect"))
            return DEFAULT_EFFECT;
        List<String> particleDataStrs = file.getConfig().getStringList("effect.particles");
        ParticleData[] particles = new ParticleData[particleDataStrs.size()];
        boolean weighted = file.getConfig().getBoolean("effect.weighted");
        for (int i = 0; i < particleDataStrs.size(); i++){
            String str = particleDataStrs.get(i);
            String[] unbox = str.split(",");
            Particle particle = Particle.LAVA;

            try {
                particle = Particle.valueOf(unbox[0].toUpperCase());
            }catch (Exception e){
                plugin.getLogger().warning("Failed to read Particle: " + unbox[0] + ", defaulting to " + particle.toString());
            }
            int amount = Integer.parseInt(unbox[1]);

            float weight = 1;
            if (weighted){
                if (unbox.length > 2) weight = Float.parseFloat(unbox[2]);
            }

            ParticleData data = new ParticleData(particle, weight, amount);
            particles[i] = data;
        }

        boolean hasSound = true;
        if (file.getConfig().contains("has-sound"))
            hasSound = file.getConfig().getBoolean("has-sound");
        Sound sound = null;
        float vol = -1;
        float pitch = -1;
        if (hasSound){
            sound = Sound.ENTITY_BLAZE_BURN;
            vol = 1;
            pitch = 1;
            if (file.getConfig().contains("sound"))
                sound = parseSound(file.getConfig().getString("sound"));
            if (file.getConfig().contains("vol"))
                vol = (float) file.getConfig().getDouble("vol");
            if (file.getConfig().contains("pitch"))
                pitch = (float) file.getConfig().getDouble("pitch");
        }
        Arrays.sort(particles, Comparator.comparingDouble(ParticleData::weight));
        return new JetpackEffect(sound, vol, pitch, hasSound, weighted, particles);
    }


    private Sound parseSound(String soundStr){
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundStr));
        if (sound == null){
            sound = Registry.SOUNDS.get(NamespacedKey.fromString(soundStr));
        }
        if (sound == null){
            try {
                sound = Sound.valueOf(soundStr.toUpperCase());
            }catch (Exception e){
                sound = Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE;
                plugin.getLogger().warning("Failed to parse sound from: " + soundStr + ", defaulting to " + sound.getKey().toString());

            }
        }
        return sound;
    }
}
