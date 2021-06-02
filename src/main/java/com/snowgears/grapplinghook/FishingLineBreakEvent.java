package com.snowgears.grapplinghook;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FishingLineBreakEvent extends Event implements Cancellable{

	 private static final HandlerList handlers = new HandlerList();
	    private Player player;
	    private Location hookLocation;

	    private boolean cancelled = false;

		public FishingLineBreakEvent(Player p, Location hookLocation) {
			player = p;
			this.hookLocation = hookLocation;
	    }
		
		public Player getPlayer(){
			return player;
		}

		public Location getHookLocation(){
		return hookLocation;
	}

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
