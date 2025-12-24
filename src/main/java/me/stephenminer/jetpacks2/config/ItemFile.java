package me.stephenminer.jetpacks2.config;

import me.stephenminer.jetpacks2.Jetpacks2;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ItemFile {
    private Jetpacks2 plugin;
    private String file;
    private File parent;

    public ItemFile(Jetpacks2 plugin, String file) {
        this.plugin = plugin;
        this.file = file;
        try {
            parent = new File(this.plugin.getDataFolder(), "items");
            if (!parent.exists()) parent.mkdirs();
        }catch (Exception e){ e.printStackTrace(); }
        saveDefaultConfig();
    }

    private FileConfiguration dataConfig = null;

    private File configFile = null;

    public void reloadConfig() {
        if (this.configFile == null) {
            this.configFile = new File(parent, file + ".yml");
            try {
                if (!configFile.exists()) configFile.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
/*
        InputStream defaultStream = this.plugin.getDataFolder().(file + ".yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }

 */
    }
    public FileConfiguration getConfig(){
        if (this.dataConfig == null)
            reloadConfig();

        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null)
            return;
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            Bukkit.broadcastMessage("COULD NOT SAVE TO CONFIG FILE: " + this.configFile);
        }
    }
    public void saveDefaultConfig(){
        if (this.configFile == null)
            this.configFile = new File(parent, file + ".yml");
        if (!this.configFile.exists()){
            try {
                configFile.createNewFile();
            }catch (Exception e){e.printStackTrace();}
            //this.plugin.saveResource(file + ".yml", false);
        }
    }

    public String fileName(){ return file; }
}
