package com.snowgears.grapplinghook.api;

import com.snowgears.grapplinghook.GrapplingHook;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class HookAPI {
	
	public static boolean isGrapplingHook(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		if(is.getType() == Material.FISHING_ROD && im != null) {
			PersistentDataContainer persistentData = im.getPersistentDataContainer();
			if (persistentData != null) {
				try {
					int uses = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "uses"), PersistentDataType.INTEGER);
					return (uses > 0);
				} catch (NullPointerException e) {
					return false;
				}
			}
		}
		return false;
	}

	public static ItemStack createGrapplingHook(int uses) {
		ItemStack hookItem = new ItemStack(Material.FISHING_ROD);
		ItemMeta hookItemMeta = hookItem.getItemMeta();
		hookItemMeta.setDisplayName(ChatColor.GOLD+"Grappling Hook");

		List<String> loreList = new ArrayList<>();
		loreList.add(ChatColor.GRAY+"Uses left - "+ChatColor.GREEN+uses);
		hookItemMeta.setLore(loreList);

		PersistentDataContainer persistentData = hookItemMeta.getPersistentDataContainer();
		persistentData.set(new NamespacedKey(GrapplingHook.getPlugin(), "timeBetweenGrapples"), PersistentDataType.INTEGER, 0);
		persistentData.set(new NamespacedKey(GrapplingHook.getPlugin(), "fallDamage"), PersistentDataType.INTEGER, 0);
		persistentData.set(new NamespacedKey(GrapplingHook.getPlugin(), "slowFall"), PersistentDataType.INTEGER, 1);
		persistentData.set(new NamespacedKey(GrapplingHook.getPlugin(), "lineBreak"), PersistentDataType.INTEGER, 1);
		persistentData.set(new NamespacedKey(GrapplingHook.getPlugin(), "uses"), PersistentDataType.INTEGER, uses);

		hookItem.setItemMeta(hookItemMeta);

		return hookItem;
	}

	public static boolean getHookInHandHasFallDamage(Player player){

		try {
			ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			int fallDamage = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "fallDamage"), PersistentDataType.INTEGER);
			return fallDamage > 0;
		} catch (NullPointerException npe){}
		return false;
	}

	public static boolean getHookInHandHasSlowFall(Player player){

		try {
			ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			int slowFall = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "slowFall"), PersistentDataType.INTEGER);
			return slowFall > 0;
		} catch (NullPointerException npe){}
		return false;
	}

	public static boolean getHookInHandHasLineBreak(Player player){

		try {
			ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			int lineBreak = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "lineBreak"), PersistentDataType.INTEGER);
			return lineBreak > 0;
		} catch (NullPointerException npe){}
		return false;
	}

	public static boolean getHookInHandHasAirHook(Player player){

		try {
			ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			int airHook = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "airHook"), PersistentDataType.INTEGER);
			return airHook > 0;
		} catch (NullPointerException npe){}
		return false;
	}

	public static boolean getHookInHandHasStickyHook(Player player){

		try {
			ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			int stickyHook = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "stickyHook"), PersistentDataType.INTEGER);
			return stickyHook > 0;
		} catch (NullPointerException npe){}
		return false;
	}

	public static double getHookInHandVelocityThrow(Player player){

		try {
			ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			double velocityThrow = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "velocityThrow"), PersistentDataType.DOUBLE);
			return velocityThrow;
		} catch (NullPointerException npe){}
		return 1.0;
	}

	public static double getHookInHandVelocityPull(Player player){

		try {
			ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			double velocityPull = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "velocityPull"), PersistentDataType.DOUBLE);
			return velocityPull;
		} catch (NullPointerException npe){}
		return 1.0;
	}

	public static int getHookInHandTimeBetweenGrapples(Player player){

		try {
			ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			int timeBetweenGrapples = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "timeBetweenGrapples"), PersistentDataType.INTEGER);
			return timeBetweenGrapples;
		} catch (NullPointerException npe){}
		return 0;
	}

	//returns the recipe # of the hook (from the recipes.yml file)
	public static int getHookRecipeNumber(ItemStack hook){

		try {
			ItemMeta im = hook.getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			int recipeNumber = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "recipe"), PersistentDataType.INTEGER);
			return recipeNumber;
		} catch (NullPointerException npe){}
		return 0;
	}
	
//	public static int getUses(ItemStack is) {
//		ItemMeta im = is.getItemMeta();
//		String usesLine = im.getLore().get(0);
//		String uses = usesLine.substring(usesLine.indexOf("a")+1, usesLine.length());
//
//		if(isInteger(uses))
//			return Integer.parseInt(uses);
//		else
//			return 0;
//	}
	
	public static boolean playerOnCooldown(Player player) {
		if(GrapplingHook.getPlugin().getGrapplingListener().noGrapplePlayers.containsKey(player.getUniqueId()))
			 return true;
		return false;
	}
	
	public static void removePlayerCooldown(Player player) {
		if(GrapplingHook.getPlugin().getGrapplingListener().noGrapplePlayers.containsKey(player.getUniqueId()))
			GrapplingHook.getPlugin().getGrapplingListener().noGrapplePlayers.remove(player.getUniqueId());
	}
	
	public static void addPlayerCooldown(final Player player, int seconds) {
		if(GrapplingHook.getPlugin().getGrapplingListener().noGrapplePlayers.containsKey(player.getUniqueId()))
			Bukkit.getServer().getScheduler().cancelTask(GrapplingHook.getPlugin().getGrapplingListener().noGrapplePlayers.get(player.getUniqueId()));
		
		int taskId = GrapplingHook.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(GrapplingHook.getPlugin(),new Runnable() {
			  public void run(){
				 removePlayerCooldown(player);
			  }
	  	}, (seconds*20));

		GrapplingHook.getPlugin().getGrapplingListener().noGrapplePlayers.put(player.getUniqueId(), taskId);
	}
	
//	public static void setUses(ItemStack is, int uses) {
//		ItemMeta im = is.getItemMeta();
//		List<String> lore = new ArrayList<String>();
//
//		lore.add(ChatColor.GRAY+"Uses left: "+ChatColor.GREEN+uses);
//
//		im.setLore(lore);
//		is.setItemMeta(im);
//	}

	public static void breakHookInHand(Player player){
		player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 10f, 1f);
	}
	
	public static boolean addUse(Player player , ItemStack hook){
		if(player.getGameMode() == GameMode.CREATIVE)
			return true;
		ItemMeta im = hook.getItemMeta();

		PersistentDataContainer persistentData = im.getPersistentDataContainer();
		try {
			int uses = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "uses"), PersistentDataType.INTEGER);

			if((uses - 1) == 0){ //hook has reached maximum uses
				breakHookInHand(player);
				return false;
			}
			else{
				String oldUses = "" + uses;
				List<String> oldLore = im.getLore();
				List<String> newLore = new ArrayList<>();
				for(String loreLine : oldLore){
					newLore.add(loreLine.replace(oldUses, "" + (uses-1)));
				}

				persistentData.set(new NamespacedKey(GrapplingHook.getPlugin(), "uses"), PersistentDataType.INTEGER, uses-1);
				im.setLore(newLore);
				hook.setItemMeta(im);
			}

		} catch (NullPointerException npe) {
			breakHookInHand(player);
			return false;
		}
		return true;
	}
	
	public static void playGrappleSound(Location loc) {
		loc.getWorld().playSound(loc, Sound.ENTITY_MAGMA_CUBE_JUMP, 10f, 1f);
	}
	
    private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
}
