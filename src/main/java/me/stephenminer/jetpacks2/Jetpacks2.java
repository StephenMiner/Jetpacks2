package me.stephenminer.jetpacks2;

import me.stephenminer.jetpacks2.commands.JetpackGive;
import me.stephenminer.jetpacks2.config.ItemFile;
import me.stephenminer.jetpacks2.jetpack.JetpackBuilder;
import me.stephenminer.jetpacks2.jetpack.JetpackData;
import me.stephenminer.jetpacks2.listener.JetpackListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Jetpacks2 extends JavaPlugin {
    public static Pattern HEX_PATTERN = Pattern.compile("(#[A-Fa-f0-9]{6})");
    public Map<String, JetpackData> jetpacks;

    public NamespacedKey itemId;
    public NamespacedKey fuel;

    @Override
    public void onEnable() {
        this.itemId = new NamespacedKey(this, "id");
        this.jetpacks = new HashMap<>();
        this.fuel = new NamespacedKey(this, "fuel");
        createJetpackRecords();
        registerListeners();
        addCommands();
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerListeners(){
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new JetpackListener(), this);
    }

    private void addCommands(){
        this.getCommand("jetpackgive").setExecutor(new JetpackGive());

    }



    public void createJetpackRecords(){
        File itemDirectory = new File(this.getDataFolder(), "items");
        boolean pass = itemDirectory.exists();
        if (!pass)
            pass = itemDirectory.mkdirs();
        if (!pass){
            this.getLogger().warning("Something went wrong loading items directory");
            return;
        }
        File[] files = itemDirectory.listFiles();
        if (files == null) return;
        JetpackBuilder builder = new JetpackBuilder();
        for (File file : files){
            if (file.getName().contains(".yml")){
                ItemFile itemFile = new ItemFile(this, file.getName().replace(".yml",""));
                if (!builder.hasJetpackData(itemFile)) continue;
                JetpackData data = builder.loadJetpackRecord(itemFile);
                jetpacks.put(data.id(), data);
            }
        }
    }

    /*
    /**
     * Loads a JetpackData record from a yml file name provided in the function parameter
     * @param jetpackId
     * @return

    public JetpackData loadJetpackRecord(String jetpackId){
        double thrust = 0.03d;
        double yThrust = 0.09;
        double maxYVelocity = 1.25d;
        int maxFuel = 200;
        String fuelId = "COAL";
        ItemFile file = new ItemFile(this, jetpackId.replace(".yml",""));
        if (!jetpackId.contains(".yml")) return new JetpackData(thrust,yThrust, maxYVelocity, maxFuel, fuelId);

        if (file.getConfig().contains("thrust"))
            thrust =file.getConfig().getDouble("thrust");
        if (file.getConfig().contains("y-thrust"))
            yThrust = file.getConfig().getDouble("y-thrust");
        if (file.getConfig().contains("max-y-velocity"))
            maxYVelocity = file.getConfig().getDouble("max-y-velocity");
        if (file.getConfig().contains("max-fuel"))
            maxFuel = file.getConfig().getInt("max-fuel");
        if (file.getConfig().contains("fuel-id"))
            fuelId = file.getConfig().getString("fuel-id");
        return new JetpackData(thrust, yThrust, maxYVelocity, maxFuel, fuelId);
    }
    */

    public String formatColor(String str){
        if (str == null) return null;
        Matcher matcher = HEX_PATTERN.matcher(str);
        while (matcher.find())
            str = str.replace(matcher.group(), "" + ChatColor.of(matcher.group()));
        return str;
    }
}
