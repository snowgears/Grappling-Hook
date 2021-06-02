package com.snowgears.grapplinghook;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerGrappleEvent extends Event implements Cancellable{

	 private static final HandlerList handlers = new HandlerList();
	    private Player player;
	 	private Entity entity;
	 	private Location pullLocation;
	    private ItemStack hookItem;
//	    private String message;
	    private boolean cancelled = false;
	    
		public PlayerGrappleEvent(Player p, Entity e, Location l) {
			player = p;
			entity = e;
			pullLocation = l;
	        hookItem = p.getInventory().getItemInMainHand();
	    }
		
		public Player getPlayer(){
			return player;
		}
	 
	    public Entity getPulledEntity() {
	        return entity;
	    }
	    
	    public Location getPullLocation(){
	    	return pullLocation;
	    }
	    
	    public ItemStack getHookItem() {
	        return hookItem;
	    }
	    
//	    public String getMessage(){
//	    	return message;
//	    }
//	    
//	    public void setMessage(String s){
//	    	message = s;
//	    }

	    public HandlerList getHandlers() {
	        return handlers;
	    }
	 
	    public static HandlerList getHandlerList() {
	        return handlers;
	    }

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void setCancelled(boolean set) {
			cancelled = set;
		}
}
