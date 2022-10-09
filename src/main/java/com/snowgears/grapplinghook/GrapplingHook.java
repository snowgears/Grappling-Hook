package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.utils.ConfigUpdater;
import com.snowgears.grapplinghook.utils.RecipeLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GrapplingHook extends JavaPlugin{
	
	private GrapplingListener grapplingListener = new GrapplingListener(this);
	private CommandHandler commandHandler;
	private static GrapplingHook plugin;
	protected FileConfiguration config;

	private boolean usePerms = false;
	private boolean teleportHooked = false;
	private boolean useMetrics = false;
	private boolean consumeUseOnSlowfall = false;
	private String commandAlias;
	private RecipeLoader recipeLoader;


	public void onEnable(){
		plugin = this;
		getServer().getPluginManager().registerEvents(grapplingListener, this);
		
		File configFile = new File(this.getDataFolder() + "/config.yml");
		if(!configFile.exists())
		{
		  this.saveDefaultConfig();
		}
        try {
            ConfigUpdater.update(plugin, "config.yml", configFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
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
		consumeUseOnSlowfall = config.getBoolean("consumeUseOnSlowfall");
		commandAlias = config.getString("command");

		if(useMetrics){
			// You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
			int pluginId = 9957;

			try {
				Metrics metrics = new Metrics(this, pluginId);
			} catch(Exception e) {}

			// Optional: Add custom charts
			//metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));
		}

		commandHandler = new CommandHandler(this, "grapplinghook.operator", commandAlias, "Base command for the GrapplingHook plugin", "/gh", new ArrayList(Arrays.asList(commandAlias)));
	}

	public void onDisable(){
		recipeLoader.unloadRecipes();
	}

	public void reload(){
		HandlerList.unregisterAll(grapplingListener);

		onDisable();
		onEnable();
	}

	public RecipeLoader getRecipeLoader(){
		return recipeLoader;
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

	public boolean isConsumeUseOnSlowfall(){
		return consumeUseOnSlowfall;
	}

	public boolean usePerms(){
		return usePerms;
	}

	public boolean getTeleportHooked(){
		return teleportHooked;
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