package me.stephenminer.jetpacks2.jetpack;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.concurrent.ThreadLocalRandom;

public record JetpackEffect (Sound sound, float vol, float pitch, boolean hasSound, boolean weighted, ParticleData[] particles){


    public void playParticle(Location loc){
        final World world = loc.getWorld();
        if (weighted){
            float total = 0;
            for (ParticleData data : particles) total += data.weight();
            float roll = ThreadLocalRandom.current().nextFloat(total);
            float sum = 0;
            for (int i = 0; i < particles.length; i++){
                float weight = particles[i].weight();
                sum += weight;
                if (roll <= sum) {
                    world.spawnParticle(particles[i].particle(), loc, particles[i].amount());
                    return;
                }
            }
        }else{
            for (ParticleData data : particles){
                world.spawnParticle(data.particle(), loc, data.amount());
            }
        }
    }

}
