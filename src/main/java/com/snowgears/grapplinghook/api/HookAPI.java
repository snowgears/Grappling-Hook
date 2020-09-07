package com.snowgears.grapplinghook.api;

import com.snowgears.grapplinghook.GrapplingHook;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class HookAPI {
	
	public static boolean isGrapplingHook(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		if(is.getType() == Material.FISHING_ROD && im.getDisplayName() != null && im.getDisplayName().equals(ChatColor.GOLD+"Grappling Hook"))
			return true;
		return false;
	}
	
	public static ItemStack createGrapplingHook(int uses) {
		ItemStack is = new ItemStack(Material.FISHING_ROD);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GOLD+"Grappling Hook");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY+"Uses left: "+ChatColor.GREEN+uses);
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}
	
	public static int getUses(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		String usesLine = im.getLore().get(0);
		String uses = usesLine.substring(usesLine.indexOf("a")+1, usesLine.length());
		
		if(isInteger(uses))
			return Integer.parseInt(uses);
		else
			return 0;
	}
	
	public static boolean playerOnCooldown(Player player) {
		if(GrapplingHook.plugin.alisten.noGrapplePlayers.containsKey(player.getName()))
			 return true;
		return false;
	}
	
	public static void removePlayerCooldown(Player player) {
		if(GrapplingHook.plugin.alisten.noGrapplePlayers.containsKey(player.getName()))
			GrapplingHook.plugin.alisten.noGrapplePlayers.remove(player.getName());
	}
	
	public static void addPlayerCooldown(final Player player, int seconds) {
		if(GrapplingHook.plugin.alisten.noGrapplePlayers.containsKey(player.getName()))
			Bukkit.getServer().getScheduler().cancelTask(GrapplingHook.plugin.alisten.noGrapplePlayers.get(player.getName()));
		
		int taskId = GrapplingHook.plugin.getServer().getScheduler().scheduleSyncDelayedTask(GrapplingHook.plugin,new Runnable() {
			  public void run(){
				 removePlayerCooldown(player);
			  }
	  	}, (seconds*20));
		
		GrapplingHook.plugin.alisten.noGrapplePlayers.put(player.getName(), taskId);
	}
	
	public static void setUses(ItemStack is, int uses) {
		ItemMeta im = is.getItemMeta();
		List<String> lore = new ArrayList<String>();
		
		lore.add(ChatColor.GRAY+"Uses left: "+ChatColor.GREEN+uses);
		
		im.setLore(lore);
		is.setItemMeta(im);
	}
	
	public static boolean addUse(Player player , ItemStack hook){
		if(player.getGameMode() == GameMode.CREATIVE)
			return true;
		ItemMeta im = hook.getItemMeta();
		String usesLine = im.getLore().get(0);
		String uses = usesLine.substring(usesLine.indexOf("a")+1, usesLine.length());

		if(isInteger(uses) == false){
			player.setItemInHand(new ItemStack(Material.AIR));
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 10f, 1f);
			return false;
		}
		else{
			int currentUses = Integer.parseInt(uses);
			currentUses--;
			
			if(currentUses == 0){ //hook has reached maximum uses
				player.setItemInHand(new ItemStack(Material.AIR));
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 10f, 1f);
				return false;
			}
			else{
				ArrayList<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GRAY+"Uses left: "+ChatColor.GREEN+currentUses);
				im.setLore(lore);
				hook.setItemMeta(im);
			}
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
