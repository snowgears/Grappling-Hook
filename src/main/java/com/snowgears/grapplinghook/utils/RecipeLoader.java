package com.snowgears.grapplinghook.utils;

import com.snowgears.grapplinghook.GrapplingHook;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RecipeLoader {

    private GrapplingHook plugin;
    private File recipesFile;
    private HashMap<String, List<String>> entityBlackLists = new HashMap<>();

    public RecipeLoader(GrapplingHook plugin){
        this.plugin = plugin;
        this.recipesFile = new File(plugin.getDataFolder(), "recipes.yml");

        loadRecipes();
    }

    private void loadRecipes() {

        try {
            ConfigUpdater.update(plugin, "recipes.yml", recipesFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int loadedCount = 0;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(recipesFile);
        if (config.getConfigurationSection("recipes") == null) {
            System.out.println("[GrapplingHook] [ERROR] Recipes file could not be loaded.");
            return;
        }

        Set<String> allRecipeNumbers = config.getConfigurationSection("recipes").getKeys(false);

        for (String recipeNumber : allRecipeNumbers) {
            boolean enabled = config.getBoolean("recipes." + recipeNumber + ".enabled");
            if (enabled) {
                String name = config.getString("recipes." + recipeNumber + ".name");
                String lore = config.getString("recipes." + recipeNumber + ".lore");
                int uses = config.getInt("recipes." + recipeNumber + ".uses");
                int timeBetweenGrapples = config.getInt("recipes." + recipeNumber + ".timeBetweenGrapples");
                boolean fallDamage = config.getBoolean("recipes." + recipeNumber + ".fallDamage");
                boolean slowFall = config.getBoolean("recipes." + recipeNumber + ".slowFall");
                boolean lineBreak = config.getBoolean("recipes." + recipeNumber + ".lineBreak");
                boolean airHook = config.getBoolean("recipes." + recipeNumber + ".airHook");

                try {
                    List<String> entityBlackList = config.getStringList("recipes." + recipeNumber + ".entityBlacklist");
                    entityBlackLists.put(formatString(name, uses), entityBlackList);
                } catch (NullPointerException e){
                    entityBlackLists.put(formatString(name, uses), new ArrayList<>());
                }

                ItemStack hookItem = new ItemStack(Material.FISHING_ROD);
                ItemMeta hookItemMeta = hookItem.getItemMeta();
                hookItemMeta.setDisplayName(formatString(name, uses));
                if(lore != null && !lore.isEmpty()) {
                    List<String> loreList = new ArrayList<>();
                    loreList.add(formatString(lore, uses));
                    hookItemMeta.setLore(loreList);
                }

                PersistentDataContainer persistentData = hookItemMeta.getPersistentDataContainer();
                persistentData.set(new NamespacedKey(plugin, "timeBetweenGrapples"), PersistentDataType.INTEGER, timeBetweenGrapples);
                persistentData.set(new NamespacedKey(plugin, "fallDamage"), PersistentDataType.INTEGER, (fallDamage ? 1 : 0));
                persistentData.set(new NamespacedKey(plugin, "slowFall"), PersistentDataType.INTEGER, (slowFall ? 1 : 0));
                persistentData.set(new NamespacedKey(plugin, "lineBreak"), PersistentDataType.INTEGER, (lineBreak ? 1 : 0));
                persistentData.set(new NamespacedKey(plugin, "airHook"), PersistentDataType.INTEGER, (airHook ? 1 : 0));
                persistentData.set(new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER, uses);
                persistentData.set(new NamespacedKey(plugin, "recipe"), PersistentDataType.INTEGER, Integer.parseInt(recipeNumber));

                hookItem.setItemMeta(hookItemMeta);

                NamespacedKey key = new NamespacedKey(plugin, "hook_item_" + recipeNumber);
                ShapedRecipe recipe = new ShapedRecipe(key, hookItem);

                HashMap<String, Material> materialMap = new HashMap<>();
                Set<String> materialKeys = config.getConfigurationSection("recipes." + recipeNumber + ".recipe.materials").getKeys(false);
                for (String materialKey : materialKeys) {
                    try {
                        Material material = Material.valueOf(config.getString("recipes." + recipeNumber + ".recipe.materials." + materialKey));
                        materialMap.put(materialKey, material);
                    } catch (IllegalArgumentException iae) {
                        System.out.println("[GrapplingHook] ERROR READING MATERIAL VALUE IN RECIPES.YML FILE");
                        iae.printStackTrace();
                    } catch (NullPointerException npe) {
                        System.out.println("[GrapplingHook] NULL ERROR READING MATERIAL VALUE IN RECIPES.YML FILE");
                    }
                }

                //System.out.println("recipe #"+recipeNumber);
                String[] threeLettersArray = new String[3];
                List<String> recipeLines = config.getStringList("recipes." + recipeNumber + ".recipe.shape");
                for (int i = 0; i < recipeLines.size(); i++) {
                    String threeLetters = "";
                    String[] splitRecipeLine = recipeLines.get(i).split("\\[");
                    for (String splitRecipeLinePart : splitRecipeLine) {
                        if (splitRecipeLinePart.contains("]")) {
                            String letter = splitRecipeLinePart.replaceAll("]", "");
                            if (letter.isEmpty())
                                letter = "";
                            threeLetters += letter;
                        }
                    }
                    threeLettersArray[i] = threeLetters;
              //      System.out.println("["+threeLettersArray[i]+"]");
                }

                if(threeLettersArray[0].isEmpty()){
                    recipe.shape(threeLettersArray[1], threeLettersArray[2]);
                }
                else if(threeLettersArray[2].isEmpty()){
                    recipe.shape(threeLettersArray[0], threeLettersArray[1]);
                }
                else {
                    recipe.shape(threeLettersArray[0], threeLettersArray[1], threeLettersArray[2]);
                }
                //System.out.println(threeLettersArray[0] + ", "+ threeLettersArray[1] + ", "+threeLettersArray[2]);

                //recipe.shape("1 1")

                for (Map.Entry<String, Material> entry : materialMap.entrySet()) {
                    if(entry.getValue().toString().contains("_PLANKS")){
                        recipe.setIngredient(entry.getKey().charAt(0), new RecipeChoice.MaterialChoice(Tag.PLANKS));
                    }
                    else {
                        recipe.setIngredient(entry.getKey().charAt(0), entry.getValue());
                    }
                }
                Bukkit.addRecipe(recipe);
                loadedCount++;
            }
        }
        System.out.println("[GrapplingHook] Loaded "+loadedCount+" recipes.");
    }

    private String formatString(String unformattedString, int uses){
        unformattedString = unformattedString.replace("[uses]", ""+uses);
        unformattedString = ChatColor.translateAlternateColorCodes('&', unformattedString);
        return unformattedString;
    }

    public boolean isEntityOnHookBlacklist(Player player, Entity hooked){
        try {
            ItemStack is = player.getInventory().getItemInMainHand();
            String name = is.getItemMeta().getDisplayName();
            for(String entity : entityBlackLists.get(name)){
                if(entity.equalsIgnoreCase(hooked.getType().toString())) {
                    return true;
                }
            }

        } catch (Exception e){
            return false;
        }
        return false;
    }
}
