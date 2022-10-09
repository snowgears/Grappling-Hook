package com.snowgears.grapplinghook.utils;

import com.snowgears.grapplinghook.GrapplingHook;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class RecipeLoader {

    private GrapplingHook plugin;
    private File recipesFile;

    public RecipeLoader(GrapplingHook plugin){
        this.plugin = plugin;
        this.recipesFile = new File(plugin.getDataFolder(), "recipes.yml");

        loadRecipes();
    }

    private void loadRecipes() {
        int loadedCount = 0;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(recipesFile);
        if (config.getConfigurationSection("recipes") == null) {
            plugin.getLogger().log(Level.SEVERE, "Recipes file could not be loaded.");
            return;
        }

        Set<String> allRecipeNumbers = config.getConfigurationSection("recipes").getKeys(false);

        for (String recipeNumber : allRecipeNumbers) {
            System.out.println("[GrapplingHook] Recipe loading - "+recipeNumber);
            boolean enabled = config.getBoolean("recipes." + recipeNumber + ".enabled");
            if (enabled) {
                String id = config.getString("recipes." + recipeNumber + ".id");
                String name = config.getString("recipes." + recipeNumber + ".name");
                List<String> lore;
                try {
                    lore = config.getStringList("recipes." + recipeNumber + ".lore");
                } catch (NullPointerException e) {
                    lore = new ArrayList<>();
                }
                int uses = config.getInt("recipes." + recipeNumber + ".uses");
                double velocityThrow = config.getDouble("recipes." + recipeNumber + ".velocityThrow");
                double velocityPull = config.getDouble("recipes." + recipeNumber + ".velocityPull");
                int timeBetweenGrapples = config.getInt("recipes." + recipeNumber + ".timeBetweenGrapples");
                boolean fallDamage = config.getBoolean("recipes." + recipeNumber + ".fallDamage");
                boolean slowFall = config.getBoolean("recipes." + recipeNumber + ".slowFall");
                boolean lineBreak = config.getBoolean("recipes." + recipeNumber + ".lineBreak");
                boolean stickyHook = config.getBoolean("recipes." + recipeNumber + ".stickyHook");
                int customModelData = config.getInt("recipes." + recipeNumber + ".customModelData", 0);

                HookSettings hookSettings = new HookSettings(id, uses, velocityThrow, velocityPull, timeBetweenGrapples, fallDamage, slowFall, lineBreak, stickyHook, customModelData);

                try {
                    boolean isBlackList = false;
                    String blackListString = config.getString("recipes." + recipeNumber + ".entity.listType");
                    List<String> entityTypeStrings = config.getStringList("recipes." + recipeNumber + ".entity.list");

                    List<EntityType> entityTypeList = new ArrayList<>();
                    for(String entityTypeString : entityTypeStrings){
                        try {
                            if(!entityTypeString.isEmpty())
                                entityTypeList.add(EntityType.valueOf(entityTypeString));
                        } catch (IllegalArgumentException e){
                            plugin.getLogger().log(Level.WARNING, "unrecognized entity type in recipe "+recipeNumber+": "+entityTypeString);
                        }
                    }

                    if(blackListString.equalsIgnoreCase("blacklist"))
                        isBlackList = true;
                    hookSettings.setEntityList(isBlackList, entityTypeList);
                } catch (NullPointerException e){
                    hookSettings.setEntityList(true, new ArrayList<>());
                }

                try {
                    boolean isBlackList = false;
                    String blackListString = config.getString("recipes." + recipeNumber + ".entity.listType");
                    List<String> materialStrings = config.getStringList("recipes." + recipeNumber + ".material.list");

                    List<Material> materialList = new ArrayList<>();
                    for(String materialString : materialStrings){
                        try {
                            if(!materialString.isEmpty())
                                materialList.add(Material.valueOf(materialString));
                        } catch (IllegalArgumentException e){
                            plugin.getLogger().log(Level.WARNING, "unrecognized material in recipe "+recipeNumber+": "+materialString);
                        }
                    }

                    if(blackListString.equalsIgnoreCase("blacklist"))
                        isBlackList = true;
                    hookSettings.setMaterialList(isBlackList, materialList);
                } catch (NullPointerException e){
                    hookSettings.setMaterialList(true, new ArrayList<>());
                }

                ItemStack hookItem = new ItemStack(Material.FISHING_ROD);
                ItemMeta hookItemMeta = hookItem.getItemMeta();
                hookItemMeta.setDisplayName(formatString(name, uses));
                if(lore != null && !lore.isEmpty()) {
                    List<String> loreList = new ArrayList<>();
                    for(String loreString : lore) {
                        loreList.add(formatString(loreString, uses));
                    }
                    hookItemMeta.setLore(loreList);
                }

                if(customModelData != 0)
                    hookItemMeta.setCustomModelData(Integer.valueOf(customModelData));

                PersistentDataContainer persistentData = hookItemMeta.getPersistentDataContainer();
                persistentData.set(new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER, uses);
                persistentData.set(new NamespacedKey(plugin, "id"), PersistentDataType.STRING, id);

                hookItem.setItemMeta(hookItemMeta);

                hookSettings.setHookItem(hookItem);
                //register the hook setting in map
                plugin.getGrapplingListener().addHookSettings(id, hookSettings);

                NamespacedKey key = new NamespacedKey(plugin, "hook_item_" + id);
                ShapedRecipe recipe = new ShapedRecipe(key, hookItem);

                HashMap<String, Material> materialMap = new HashMap<>();
                Set<String> materialKeys = config.getConfigurationSection("recipes." + recipeNumber + ".recipe.materials").getKeys(false);
                for (String materialKey : materialKeys) {
                    try {
                        Material material = Material.valueOf(config.getString("recipes." + recipeNumber + ".recipe.materials." + materialKey));
                        materialMap.put(materialKey, material);
                    } catch (IllegalArgumentException iae) {
                        plugin.getLogger().log(Level.WARNING, "ERROR READING MATERIAL VALUE IN RECIPES.YML FILE");
                    } catch (NullPointerException npe) {
                        plugin.getLogger().log(Level.WARNING, "NULL ERROR READING MATERIAL VALUE IN RECIPES.YML FILE");
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

        plugin.getLogger().log(Level.INFO, "Loaded "+loadedCount+" recipes.");
    }

    public void unloadRecipes(){
        NamespacedKey key;
        for(String hookID : plugin.getGrapplingListener().getHookIDs()){
            key = new NamespacedKey(plugin, "hook_item_" + hookID);
            Bukkit.removeRecipe(key);
        }
    }

    private String formatString(String unformattedString, int uses){
        unformattedString = unformattedString.replace("[uses]", ""+uses);
        unformattedString = ChatColor.translateAlternateColorCodes('&', unformattedString);
        return unformattedString;
    }
}
