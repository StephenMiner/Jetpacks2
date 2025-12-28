package me.stephenminer.jetpacks2.jetpack;

import me.stephenminer.jetpacks2.Jetpacks2;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class JetpackController {
    private final Jetpacks2 plugin;
    private final Map<UUID, ItemMeta> activatedThrusters;
    private final Set<UUID> updateFuel;

    private final double[] sin, cos;
    private final int points;

    public JetpackController(){
        this.plugin = JavaPlugin.getPlugin(Jetpacks2.class);
        this.activatedThrusters = new HashMap<>();
        this.updateFuel = new HashSet<>();
        this.points = 12;
        sin = new double[2*points + 1];
        cos = new double[2*points + 1];
        int i = 0;
        for (double theta = 0; theta <= Math.PI * 2; theta += Math.PI/points){
            sin[i] = Math.sin(theta);
            cos[i] = Math.cos(theta);
            i++;
        }
    }

/*
    public void t(Player p){
        playEffect(p.getLocation().clone().add(0,1,0).add(p.getLocation().getDirection().setY(0).multiply(-0.2)));
    }
 */

    /**
     * Attempts to begin thrust activation for the jetpack. If thrust is already activated,
     * @param player The player attempting to activate thrust
     * @param item an ItemStack confirmed to already be a jetpack item
     * @param jetpack The Jetpack data assosciated with the input item
     * @param activationSlot The EquipmentSlot the player attempted to activate this item from
     * @return true if thrust activation was successful, false otherwise (no fuel, already activated -> deactivate).
     */
    public boolean attemptThrust(Player player, ItemStack item, JetpackData jetpack, EquipmentSlot activationSlot){

        int fuel = item.getItemMeta().getPersistentDataContainer().getOrDefault(plugin.fuel, PersistentDataType.INTEGER, jetpack.maxFuel());
        if (fuel <= 0)
            return false;
        UUID uuid = player.getUniqueId();
        if (activatedThrusters.containsKey(uuid)){
          //  player.sendMessage(ChatColor.YELLOW + "Deactivating activated jetpack!");
            activatedThrusters.remove(player.getUniqueId());
            return false;
        }
        Damageable meta = (Damageable) item.getItemMeta();
        meta.setMaxDamage(jetpack.maxFuel());
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        activatedThrusters.put(uuid, meta);
        player.sendMessage(ChatColor.GREEN + "Jetpack activated");
        //player.get
       // player.sendMessage("" + player.getVelocity().getX() + "," + player.getVelocity().getZ());
        new BukkitRunnable(){
            int f = fuel; // necessary because that's jsut how it is :p.
            @Override
            public void run(){
                if (f <= 0 || !activatedThrusters.containsKey(player.getUniqueId()) || player.isDead() || !player.isOnline()){
                    this.cancel();
                    container.set(plugin.fuel, PersistentDataType.INTEGER, f);
                    item.setItemMeta(meta);
                    player.sendMessage(ChatColor.RED + "Jetpack deactivated");
                    activatedThrusters.remove(uuid);
                    player.setAllowFlight(false);
                    return;
                }
                if (!item.equals(player.getInventory().getItem(activationSlot))) {
                    player.sendMessage(ChatColor.RED + "Jetpack deactivated");
                    plugin.getLogger().info("Exit due to item inequality");
                    this.cancel();
                    container.set(plugin.fuel, PersistentDataType.INTEGER, f);
                    item.setItemMeta(meta);
                    activatedThrusters.remove(uuid);
                    player.setAllowFlight(false);
                    return;
                }
                player.setAllowFlight(true);
                //System.out.println(player.getVelocity().getX());
                Vector direction = player.getLocation().getDirection().clone().setY(0).normalize();
                if (!player.isOnGround())
                    direction = direction.multiply(0.2);
                else direction = direction.multiply(0);
                direction = direction.setY(Math.min(player.getVelocity().getY() + 0.08 * jetpack.thrust(), jetpack.maxYVelocity()));
                player.setVelocity(direction);
                f = getFuel(meta);
                f -= jetpack.consumption();
                container.set(plugin.fuel, PersistentDataType.INTEGER, f);
                meta.setDamage(jetpack.maxFuel() - f);
                item.setItemMeta(meta);
                playEffect(jetpack, player.getLocation().clone().add(0,1.25,0).add(direction.setY(0).multiply(-0.4)));
                player.playSound(player, Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, 1f,1f);

            }
        }.runTaskTimer(plugin, 1, 1);
        return true;
    }

    private void playEffect(JetpackData jetpack, Location center){
        Location l = center.clone();
        double yaw = (-center.getYaw() + 180) * 0.017453292F;
        double rSin = Math.sin(yaw);
        double rCos = Math.cos(yaw);
        for (double h = 0; h <= 1.6; h+=0.2) {
            for (int i = 0; i <= points; i++) {
                double x = h/2 * cos[i];
                double z = h/2 * sin[i];
                double dy = - h;
                // rotate point around y axis
                double dx = x * rCos + z * rSin;
                double dz = x * -rSin + z*rCos;
                l.add(dx, dy, dz);
                /*
                if (ThreadLocalRandom.current().nextInt(3) <= 1)
                    world.spawnParticle(Particle.FLAME, l, 0);
                else world.spawnParticle(Particle.SMOKE_NORMAL, l, 0);

                 */

                //world.spawnParticle(Particle.LAVA, l, 0);
                jetpack.effect().playParticle(l);
                //System.out.println("coords:" + cos[i] + "," + h + "," + sin[i]);
                l.subtract(dx, dy, dz);
            }
        }
    }

    private int getFuel(ItemMeta meta){
        return meta.getPersistentDataContainer().getOrDefault(plugin.fuel, PersistentDataType.INTEGER, -1);
    }

    public boolean usingJetpack(Player player){
        return activatedThrusters.containsKey(player.getUniqueId());
    }



}
