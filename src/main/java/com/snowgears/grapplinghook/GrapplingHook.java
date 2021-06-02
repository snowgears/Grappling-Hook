package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.api.HookAPI;
import com.snowgears.grapplinghook.utils.RecipeLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class GrapplingHook extends JavaPlugin{
	
	private GrapplingListener grapplingListener = new GrapplingListener(this);
	private static GrapplingHook plugin;
	protected FileConfiguration config;
	private Plugin shopPlugin;

	private static boolean usePerms = false;
	private static boolean teleportHooked = false;
	private static boolean useMetrics = false;
	private static RecipeLoader recipeLoader;


	public void onEnable(){
		plugin = this;
		getServer().getPluginManager().registerEvents(grapplingListener, this);
		
		File configFile = new File(this.getDataFolder() + "/config.yml");
		if(!configFile.exists())
		{
		  this.saveDefaultConfig();
		}
		config = YamlConfiguration.loadConfiguration(configFile);

		File recipeConfigFile = new File(getDataFolder(), "recipes.yml");
		if (!recipeConfigFile.exists()) {
			recipeConfigFile.getParentFile().mkdirs();
			this.copy(getResource("recipes.yml"), recipeConfigFile);
		}
		recipeLoader = new RecipeLoader(plugin);
		
		usePerms = config.getBoolean("usePermissions");
		teleportHooked = config.getBoolean("teleportToHook");
		useMetrics = config.getBoolean("useMetrics");

		if(useMetrics){
			// You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
			int pluginId = 9957;
			Metrics metrics = new Metrics(this, pluginId);

			// Optional: Add custom charts
			//metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));
		}

		try {
			shopPlugin = Bukkit.getServer().getPluginManager().getPlugin("Shop");
		} catch (NullPointerException npe){
			shopPlugin = null;
		}
	}

	public RecipeLoader getRecipeLoader(){
		return recipeLoader;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 1){
			if(sender instanceof Player){
				Player player = (Player)sender;
				if ((cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give"))) {
					if(player.hasPermission("grapplinghook.command.give"))
						player.setItemInHand(HookAPI.createGrapplingHook(50));
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
			}
			return true;
		}
		else if(args.length == 2){
			if(sender instanceof Player){
				Player player = (Player)sender;
				if ((cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give") && args[1].length() > 0)) {
					if(player.hasPermission("grapplinghook.command.give")){
						if(isInteger(args[1]))
							player.setItemInHand(HookAPI.createGrapplingHook(Integer.parseInt(args[1])));
						else if(Bukkit.getPlayer(args[1]) != null){
							Bukkit.getPlayer(args[1]).getInventory().addItem(HookAPI.createGrapplingHook(50));
							Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GRAY+player.getName()+" has given you a grappling hook with 50 uses!");
						}
						else
							player.sendMessage(ChatColor.RED+"Incorrect arguments. '/gh give <player>'.");
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
			}
			else{ //sender from console
				if ((cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give") && args[1].length() > 0)) {
						if(Bukkit.getPlayer(args[1]) != null){
							Bukkit.getPlayer(args[1]).getInventory().addItem(HookAPI.createGrapplingHook(50));
							Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GRAY+" You have been given a grappling hook with 50 uses by the server!");
						}
						else
							sender.sendMessage(ChatColor.RED+"Incorrect arguments. '/gh give <player>'.");
				}
			}
			return true;
		}
		else if(args.length == 3){
			if(sender instanceof Player){
				Player player = (Player)sender;
				if (cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give")) {
					if(player.hasPermission("grapplinghook.command.give")){
						if(isInteger(args[2])){
							if(Bukkit.getPlayer(args[1]) != null){
								int uses = Integer.parseInt(args[2]);
								Bukkit.getPlayer(args[1]).getInventory().addItem(HookAPI.createGrapplingHook(uses));
								Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GRAY+player.getName()+" has given you a grappling hook with "+uses+" uses!");
							}
							else
								player.sendMessage(ChatColor.RED+"That player could not be found. '/gh give <player> <#>'.");
								
						}
						else
							player.sendMessage(ChatColor.RED+"Incorrect arguments. '/gh give <player> <#>'.");
					}
					else
						player.sendMessage(ChatColor.DARK_RED+"You are not authorized to do that.");
				}
			}
			else{ //sending from console
				if (cmd.getName().equalsIgnoreCase("gh") && args[0].equalsIgnoreCase("give")) {
						if(isInteger(args[2])){
							if(Bukkit.getPlayer(args[1]) != null){
								int uses = Integer.parseInt(args[2]);
								Bukkit.getPlayer(args[1]).getInventory().addItem(HookAPI.createGrapplingHook(uses));
								Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GRAY+"You have been given a grappling hook with "+uses+" uses by the server!");
							}
							else
								sender.sendMessage(ChatColor.RED+"That player could not be found. '/gh give <player> <#>'.");
								
						}
						else
							sender.sendMessage(ChatColor.RED+"Incorrect arguments. '/gh give <player> <#>'.");
				}
			}
			return true;
		}
        return false;
    }
	
    private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}

	public static GrapplingHook getPlugin(){
		return plugin;
	}

	public GrapplingListener getGrapplingListener(){
		return grapplingListener;
	}

	public boolean usePerms(){
		return usePerms;
	}

	public boolean getTeleportHooked(){
		return teleportHooked;
	}

	public NamespacedKey getShopNamedSpaceKey(){
		if(shopPlugin == null)
			return null;
		return new NamespacedKey(shopPlugin, "display");
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}