package com.snowgears.grapplinghook;


import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.snowgears.grapplinghook.api.HookAPI;
import com.snowgears.grapplinghook.utils.Metrics;


public class GrapplingHook extends JavaPlugin{
	
	public GrapplingListener listener;

	public boolean usePerms = false;
	public boolean teleportHooked = false;
	public boolean fallDamage = false;
	public int timeBetweenUses = 0;
	private boolean debug;

	public void onEnable(){
		try {
		     Metrics metrics = new Metrics(this);
		     metrics.start();
		}
		catch (IOException e) {
		     // Failed to submit the stats
		}

		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();

		FileConfiguration config = getConfig();
		
		usePerms = config.getBoolean("usePermissions");
		debug = config.getBoolean("debug");
		teleportHooked = config.getBoolean("teleportToHook");
		fallDamage = config.getBoolean("fallDamageWithHook");
		boolean disableCrafting = config.getBoolean("disableCrafting");

		int woodUses = config.getInt("Uses.wood");
		int stoneUses = config.getInt("Uses.stone");
		int ironUses = config.getInt("Uses.iron");
		int goldUses = config.getInt("Uses.gold");
		int diamondUses = config.getInt("Uses.diamond");
		
		timeBetweenUses = config.getInt("timeBetweenGrapples");

		if(!disableCrafting){
			ShapelessRecipe woodRecipe = new ShapelessRecipe(new NamespacedKey(this, "WOODEN_GRAPPLING_HOOK"), HookAPI.createGrapplingHook(woodUses));
			woodRecipe.addIngredient(new RecipeChoice.MaterialChoice(Material.ACACIA_PLANKS,
					Material.BIRCH_PLANKS,
					Material.DARK_OAK_PLANKS,
					Material.JUNGLE_PLANKS,
					Material.OAK_PLANKS,
					Material.SPRUCE_PLANKS));
			woodRecipe.addIngredient(new RecipeChoice.MaterialChoice(Material.ACACIA_PLANKS,
					Material.BIRCH_PLANKS,
					Material.DARK_OAK_PLANKS,
					Material.JUNGLE_PLANKS,
					Material.OAK_PLANKS,
					Material.SPRUCE_PLANKS));
			woodRecipe.addIngredient(new RecipeChoice.MaterialChoice(Material.ACACIA_PLANKS,
					Material.BIRCH_PLANKS,
					Material.DARK_OAK_PLANKS,
					Material.JUNGLE_PLANKS,
					Material.OAK_PLANKS,
					Material.SPRUCE_PLANKS));
			woodRecipe.addIngredient(1, Material.FISHING_ROD);

			ShapelessRecipe stoneRecipe = new ShapelessRecipe(new NamespacedKey(this, "STONE_GRAPPLING_HOOK"), HookAPI.createGrapplingHook(stoneUses));
			stoneRecipe.addIngredient(3, Material.COBBLESTONE);
			stoneRecipe.addIngredient(1, Material.FISHING_ROD);

			ShapelessRecipe ironRecipe = new ShapelessRecipe(new NamespacedKey(this, "IRON_GRAPPLING_HOOK"), HookAPI.createGrapplingHook(ironUses));
			ironRecipe.addIngredient(3, Material.IRON_INGOT);
			ironRecipe.addIngredient(1, Material.FISHING_ROD);

			ShapelessRecipe goldRecipe = new ShapelessRecipe(new NamespacedKey(this, "GOLDEN_GRAPPLING_HOOK"), HookAPI.createGrapplingHook(goldUses));
			goldRecipe.addIngredient(3, Material.GOLD_INGOT);
			goldRecipe.addIngredient(1, Material.FISHING_ROD);

			ShapelessRecipe diamondRecipe = new ShapelessRecipe(new NamespacedKey(this, "DIAMOND_GRAPPLING_HOOK"), HookAPI.createGrapplingHook(diamondUses));
			diamondRecipe.addIngredient(3, Material.DIAMOND);
			diamondRecipe.addIngredient(1, Material.FISHING_ROD);
			
			getServer().addRecipe(woodRecipe);
			getServer().addRecipe(stoneRecipe);
			getServer().addRecipe(ironRecipe);
			getServer().addRecipe(goldRecipe);
			getServer().addRecipe(diamondRecipe);
		}

		HookAPI.plugin = this;
		listener = new GrapplingListener(this);
		getServer().getPluginManager().registerEvents(listener, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		int uses = 50;
		if (args.length == 3) {
			try {
				uses = Integer.parseInt(args[2]);
			} catch (NumberFormatException ex) {
				sender.sendMessage(ChatColor.DARK_RED + args[2] + " is not a valid number!");
				uses = 50;
			}
		}

		if(args.length == 1) {
			if(sender instanceof Player) {
				Player player = (Player)sender;
				if ((cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give"))) {
					if(player.hasPermission("grapplinghook.command.give"))
						if (player.getInventory().firstEmpty() != -1) {
							player.getInventory().addItem(HookAPI.createGrapplingHook(5));
						}
						else {
							sender.sendMessage(ChatColor.RED + player.getName() + "'s inventory is full!");
						}
					else
						player.sendMessage(ChatColor.DARK_RED + "You are not authorized to do that.");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "If you are not a player, you must specify target player when using this command.");
			}
		}
		else if(args.length >= 2) {
			if(sender instanceof Player){
				Player player = (Player)sender;
				if ((cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give") && args[1].length() > 0)) {
					if(player.hasPermission("grapplinghook.command.give")){
						if(isInteger(args[1])) {
							if (player.getInventory().firstEmpty() != -1) {
								player.getInventory().addItem(HookAPI.createGrapplingHook(Integer.parseInt(args[1])));
							}
							else {
								sender.sendMessage(ChatColor.RED + player.getName() + "'s inventory is full!");
							}
						}
						else if(Bukkit.getPlayer(args[1]) != null){
							Player target = Bukkit.getPlayer(args[1]);
							if (target.getInventory().firstEmpty() != -1) {
								target.getInventory().addItem(HookAPI.createGrapplingHook(uses));
								target.sendMessage(ChatColor.GRAY + player.getName()+" has given you a grappling hook with "+ uses +" uses!");
							}
							else {
								sender.sendMessage(ChatColor.RED + args[1] + "'s inventory is full!");
							}
						}
						else
							player.sendMessage(ChatColor.RED + "Incorrect arguments. '/gh give <player>'.");
					}
					else
						player.sendMessage(ChatColor.DARK_RED + "You are not authorized to do that.");
				}
			}
			else { //sender from console
				if ((cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give") && args[1].length() > 0)) {
					Player target = Bukkit.getPlayer(args[1]);
					if (target != null){
						if (target.getInventory().firstEmpty() != -1) {
							target.getInventory().addItem(HookAPI.createGrapplingHook(uses));
							target.sendMessage(ChatColor.GRAY+" You have been given a grappling hook with " + uses + " uses by the server!");
						}
						else {
							sender.sendMessage(ChatColor.RED + args[1] + "'s inventory is full!");
						}
					}
					else
						sender.sendMessage(ChatColor.RED+"Incorrect arguments. '/gh give <player>'.");
				}
			}
		}

        return true;
    }
	
    private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}

	public void logMessage(Level level, String msg) {
		if (this.debug) {
			this.getLogger().log(level, msg);
		}
	}
}