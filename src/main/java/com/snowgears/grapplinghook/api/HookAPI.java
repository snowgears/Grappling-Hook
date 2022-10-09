package com.snowgears.grapplinghook.api;

import com.snowgears.grapplinghook.GrapplingHook;
import com.snowgears.grapplinghook.utils.HookSettings;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
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

	public static ItemStack createGrapplingHook(String hookID) {
		HookSettings hookSettings = GrapplingHook.getPlugin().getGrapplingListener().getHookSettings(hookID);
		if(hookSettings == null)
			return null;

		return hookSettings.getHookItem();
	}

	public static HookSettings getHookSettingsForHookInHand(Player player){
		return getHookSettingsForHook(player.getInventory().getItemInMainHand());
	}

	public static HookSettings getHookSettingsForHook(ItemStack hook){
		try {
			ItemMeta im = hook.getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			String hookID = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "id"), PersistentDataType.STRING);
			return GrapplingHook.getPlugin().getGrapplingListener().getHookSettings(hookID);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean getHookInHandHasFallDamage(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.isFallDamage();
	}

	public static boolean getHookInHandHasSlowFall(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.isSlowFall();
	}

	public static boolean getHookInHandHasLineBreak(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.isLineBreak();
	}

	public static boolean getHookInHandHasStickyHook(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.isStickyHook();
	}

	public static double getHookInHandVelocityThrow(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return 0;
		return hookSettings.getVelocityThrow();
	}

	public static double getHookInHandVelocityPull(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return 0;
		return hookSettings.getVelocityPull();
	}

	public static int getHookInHandTimeBetweenGrapples(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return 0;
		return hookSettings.getTimeBetweenGrapples();
	}

	public static boolean canHookEntityType(Player player, EntityType entityType){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.canHookEntityType(entityType);
	}

	public static boolean canHookMaterial(Player player, Material material){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.canHookMaterial(material);
	}


	//returns the recipe # of the hook (from the recipes.yml file)
	public static String getHookID(ItemStack hook){

		HookSettings hookSettings = getHookSettingsForHook(hook);
		if(hookSettings == null)
			return null;
		return hookSettings.getId();
	}
	
	public static boolean isPlayerOnCoolDown(Player player) {
		return GrapplingHook.getPlugin().getGrapplingListener().isPlayerOnCoolDown(player);
	}
	
	public static void removePlayerCoolDown(Player player) {
		GrapplingHook.getPlugin().getGrapplingListener().removePlayerCoolDown(player);
	}
	
	public static void addPlayerCoolDown(final Player player, int seconds) {
		GrapplingHook.getPlugin().getGrapplingListener().addPlayerCoolDown(player, seconds);
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
