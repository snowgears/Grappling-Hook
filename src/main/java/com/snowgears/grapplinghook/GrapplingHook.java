package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.api.HookAPI;
import com.snowgears.grapplinghook.utils.Metrics;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class GrapplingHook extends JavaPlugin {

    public static GrapplingHook plugin;
    public static boolean usePerms = false;
    public static boolean teleportHooked = false;
    public static boolean fallDamage = false;
    public static boolean disableCrafting = false;
    public static int woodUses = 0;
    public static int stoneUses = 0;
    public static int ironUses = 0;
    public static int goldUses = 0;
    public static int diamondUses = 0;
    public static int timeBetweenUses = 0;
    public final GrapplingListener alisten = new GrapplingListener(this);
    protected FileConfiguration config;

    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(alisten, this);

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats
        }

        File configFile = new File(this.getDataFolder() + "/config.yml");

        if (!configFile.exists()) {
            this.saveDefaultConfig();
        }

        usePerms = getConfig().getBoolean("usePermissions");
        teleportHooked = getConfig().getBoolean("teleportToHook");
        fallDamage = getConfig().getBoolean("fallDamageWithHook");
        disableCrafting = getConfig().getBoolean("disableCrafting");

        woodUses = getConfig().getConfigurationSection("Uses").getInt("wood");
        stoneUses = getConfig().getConfigurationSection("Uses").getInt("stone");
        ironUses = getConfig().getConfigurationSection("Uses").getInt("iron");
        goldUses = getConfig().getConfigurationSection("Uses").getInt("gold");
        diamondUses = getConfig().getConfigurationSection("Uses").getInt("diamond");

        timeBetweenUses = getConfig().getInt("timeBetweenGrapples");

        if (!disableCrafting) {
            for (Material plankMaterial : Tag.PLANKS.getValues()) {
                ShapedRecipe woodRecipe = new ShapedRecipe(new NamespacedKey(this,
                    plankMaterial.getKey().toString().toLowerCase(Locale.ROOT).replace(":", "_")
                        + "_grappling_hook_recipe"
                ), HookAPI.createGrapplingHook(woodUses))
                    .shape(" **", " &*", "   ")
                    .setIngredient('*', plankMaterial)
                    .setIngredient('&', Material.FISHING_ROD);
                getServer().addRecipe(woodRecipe);
            }

            ShapedRecipe stoneRecipe = new ShapedRecipe(new NamespacedKey(this, "stone_grappling_hook_recipe"),
                HookAPI.createGrapplingHook(stoneUses)
            )
                .shape(" **", " &*", "   ")
                .setIngredient('*', Material.COBBLESTONE)
                .setIngredient('&', Material.FISHING_ROD);

            ShapedRecipe ironRecipe = new ShapedRecipe(new NamespacedKey(this, "iron_grappling_hook_recipe"),
                HookAPI.createGrapplingHook(ironUses)
            )
                .shape(" **", " &*", "   ")
                .setIngredient('*', Material.IRON_INGOT)
                .setIngredient('&', Material.FISHING_ROD);

            ShapedRecipe goldRecipe = new ShapedRecipe(new NamespacedKey(this, "gold_grappling_hook_recipe"),
                HookAPI.createGrapplingHook(goldUses))
                .shape(" **", " &*", "   ")
                .setIngredient('*', Material.GOLD_INGOT)
                .setIngredient('&', Material.FISHING_ROD);

            ShapedRecipe diamondRecipe = new ShapedRecipe(new NamespacedKey(this, "diamond_grappling_hook_recipe"),
                HookAPI.createGrapplingHook(diamondUses)
            )
                .shape(" **", " &*", "   ")
                .setIngredient('*', Material.DIAMOND)
                .setIngredient('&', Material.FISHING_ROD);

            getServer().addRecipe(stoneRecipe);
            getServer().addRecipe(ironRecipe);
            getServer().addRecipe(goldRecipe);
            getServer().addRecipe(diamondRecipe);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if ((cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give"))) {
                    if (player.hasPermission("grapplinghook.command.give")) {
                        player.setItemInHand(HookAPI.createGrapplingHook(50));
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "You are not authorized to do that.");
                    }
                }
            }
            return true;
        } else if (args.length == 2) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if ((cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give") && args[1].length() > 0)) {
                    if (player.hasPermission("grapplinghook.command.give")) {
                        if (isInteger(args[1])) {
                            player.setItemInHand(HookAPI.createGrapplingHook(Integer.parseInt(args[1])));
                        } else if (Bukkit.getPlayer(args[1]) != null) {
                            Bukkit.getPlayer(args[1]).getInventory().addItem(HookAPI.createGrapplingHook(50));
                            Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GRAY + player.getName() + " has given you" +
                                " a grappling hook with 50 uses!");
                        } else
                            player.sendMessage(ChatColor.RED + "Incorrect arguments. '/gh give <player>'.");
                    } else
                        player.sendMessage(ChatColor.DARK_RED + "You are not authorized to do that.");
                }
            } else { //sender from console
                if ((cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give") && args[1].length() > 0)) {
                    if (Bukkit.getPlayer(args[1]) != null) {
                        Bukkit.getPlayer(args[1]).getInventory().addItem(HookAPI.createGrapplingHook(50));
                        Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GRAY + " You have been given a grappling hook" +
                            " with 50 uses by the server!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect arguments. '/gh give <player>'.");
                    }
                }
            }
            return true;
        } else if (args.length == 3) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give")) {
                    if (player.hasPermission("grapplinghook.command.give")) {
                        if (isInteger(args[2])) {
                            if (Bukkit.getPlayer(args[1]) != null) {
                                int uses = Integer.parseInt(args[2]);
                                Bukkit.getPlayer(args[1]).getInventory().addItem(HookAPI.createGrapplingHook(uses));
                                Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GRAY + player.getName() + " has given" +
                                    " you a grappling hook with " + uses + " uses!");
                            } else
                                player.sendMessage(ChatColor.RED + "That player could not be found. '/gh give " +
                                    "<player> <#>'.");

                        } else
                            player.sendMessage(ChatColor.RED + "Incorrect arguments. '/gh give <player> <#>'.");
                    } else
                        player.sendMessage(ChatColor.DARK_RED + "You are not authorized to do that.");
                }
            } else { //sending from console
                if (cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give")) {
                    if (isInteger(args[2])) {
                        if (Bukkit.getPlayer(args[1]) != null) {
                            int uses = Integer.parseInt(args[2]);
                            Bukkit.getPlayer(args[1]).getInventory().addItem(HookAPI.createGrapplingHook(uses));
                            Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GRAY + "You have been given a grappling " +
                                "hook with " + uses + " uses by the server!");
                        } else
                            sender.sendMessage(ChatColor.RED + "That player could not be found. '/gh give <player> " +
                                "<#>'.");

                    } else
                        sender.sendMessage(ChatColor.RED + "Incorrect arguments. '/gh give <player> <#>'.");
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
