package me.stephenminer.jetpacks2.recipe;

import me.stephenminer.jetpacks2.Jetpacks2;
import me.stephenminer.jetpacks2.config.ItemFile;
import me.stephenminer.jetpacks2.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class RecipeConstructor {
    private final String itemId;
    private final ItemFile config;
    private final Jetpacks2 plugin;

    public RecipeConstructor(String itemId){
        this.itemId = itemId;
        this.plugin = JavaPlugin.getPlugin(Jetpacks2.class);
        this.config = new ItemFile(plugin, itemId);
    }

    public boolean validItemId(){ return plugin.jetpacks.containsKey(itemId); }


    public ShapedRecipe construct(){
        if (!config.getConfig().contains("recipe")) return null;
        List<String> recipeLines = config.getConfig().getStringList("recipe");
        NamespacedKey recipeKey = new NamespacedKey(plugin, itemId + "-recipe");
        ItemStack item = new ItemBuilder().construct(config);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, item);
        List<String> rows = new ArrayList<>();
        int row = 0;
        int index = 0;
        String[] rawIngredients = new String[27];
        for (String line : recipeLines){
            StringBuilder charLine = new StringBuilder();
            String[] split = line.split(",");
            for (String entry : split){
                if (entry.equals("-")) charLine.append(' ');
                else {
                    charLine.append((char) ('a' + index));
                    rawIngredients[index] = entry;
                }
                index++;
            }
            String str = charLine.toString();
            if (!str.isBlank()) rows.add(str);
            row++;
            if (row == 3) break;
        }
        recipe.shape(rows.toArray(new String[0]));
        for (int i = 0; i < rawIngredients.length; i++){
            String entry = rawIngredients[i];
            if (entry == null) continue;
            Material mat = plugin.materialFromString(entry);
            if (mat == null){
                if (!plugin.itemFileExists(entry)){
                    plugin.getLogger().warning("Failed to generate ingredient from " + entry);
                    return null;
                    // recipe.setIngredient((char) ('a' + i), Material.AIR);
                }
                ItemFile file = new ItemFile(plugin, entry);
                ItemStack exact = new ItemBuilder().construct(file);
                recipe.setIngredient((char) ('a' + i), new RecipeChoice.ExactChoice(exact));
            }else recipe.setIngredient((char)('a' + i), mat);
        }
        plugin.recipeKeys.add(recipeKey);
        return recipe;
    }
}
