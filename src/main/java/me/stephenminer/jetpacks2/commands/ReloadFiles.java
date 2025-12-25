package me.stephenminer.jetpacks2.commands;

import me.stephenminer.jetpacks2.Jetpacks2;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadFiles implements CommandExecutor {
    private final Jetpacks2 plugin;

    public ReloadFiles(){
        this.plugin = JavaPlugin.getPlugin(Jetpacks2.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("jetpacks.commands.reload")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        plugin.fuelFile.reloadConfig();
        plugin.createJetpackRecords();
        plugin.constructFuelData();
        sender.sendMessage(ChatColor.GREEN + "reloaded all plugin config files!");
        return true;
    }
}
