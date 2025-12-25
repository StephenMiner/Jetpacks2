package me.stephenminer.jetpacks2.commands;

import me.stephenminer.jetpacks2.Jetpacks2;
import me.stephenminer.jetpacks2.config.ItemFile;
import me.stephenminer.jetpacks2.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class JetpackGive implements CommandExecutor, TabCompleter {
    private final Jetpacks2 plugin;
    public JetpackGive(){
        this.plugin = JavaPlugin.getPlugin(Jetpacks2.class);
    }

    //jetpackgive [id] [player] [amount]
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("jetpacks.commands.givejetpack")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        if (args.length < 3){
            sender.sendMessage(ChatColor.RED + "Not enough arguments!");
            sender.sendMessage(ChatColor.YELLOW + "Proper Usage: /jetpacksgive [jetpack-id] [player] [amount]");
            return false;
        }
        String jetpackId = args[0];
        if (!validJetpackId(jetpackId)){
            sender.sendMessage(ChatColor.RED + jetpackId + " is not a valid jetpack id!");
            return false;
        }

        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null){
            sender.sendMessage(ChatColor.RED + "Could not find Player with the name " + args[1] + " on the server!");
            return false;
        }

        int amount = 1;
        try{
            amount = Integer.parseInt(args[2]);
        } catch(Exception ignored){}
        ItemStack item = getItemFromId(jetpackId);
        if (item == null){
            sender.sendMessage(ChatColor.RED + "Failed to create an item from jetpack-id " + jetpackId);
            return false;
        }
        item.setAmount(amount);
        HashMap<Integer, ItemStack> result = player.getInventory().addItem(item);
        World world = player.getWorld();
        if (!result.isEmpty()) {
            for (ItemStack give : result.values()) {
                world.dropItemNaturally(player.getLocation(), give);
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Gave " + player.getName() + " the item!");
        player.sendMessage(ChatColor.GREEN + "You've received an item!");
        return true;
    }

    private boolean validJetpackId(String jetpackId){
        return plugin.jetpacks.containsKey(jetpackId);
    }

    private ItemStack getItemFromId(String jetpackId){
        if (!validJetpackId(jetpackId)) return null;
        ItemBuilder builder = new ItemBuilder();
        ItemFile file = new ItemFile(plugin, jetpackId);
        return builder.construct(file);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        if (args.length == 1) return itemIds(args[0]);
        return null;
    }



    private List<String> filter(Collection<String> base, String match){
        match = match.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String entry : base){
            String temp = ChatColor.stripColor(entry).toLowerCase();
            if (temp.contains(match)) filtered.add(entry);
        }
        return filtered;
    }

    private List<String> itemIds(String match){
        File root = new File(plugin.getDataFolder(), "items");
        File[] files = root.listFiles();
        List<String> itemIds = new ArrayList<>();
        for (File file : files){
            String name = file.getName();
            if (name.contains(".yml"))
                itemIds.add(name.replace(".yml",""));
        }
        return filter(itemIds, match);
    }
}
