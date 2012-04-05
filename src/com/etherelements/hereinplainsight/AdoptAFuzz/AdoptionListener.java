package com.etherelements.hereinplainsight.AdoptAFuzz;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class AdoptionListener implements Listener{
	public static AdoptAFuzz plugin;

	public AdoptionListener(AdoptAFuzz plugin){
		AdoptionListener.plugin = plugin;
	}
	
	@EventHandler
	public void onCreatureSpawnEvent(CreatureSpawnEvent event){
		if (!event.isCancelled() && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING){
			EntityType type = event.getEntity().getType();
			if ((type != EntityType.OCELOT && !plugin.getConfig().getBoolean("kittensSpawnUntamed")) || (type != EntityType.WOLF && !plugin.getConfig().getBoolean("puppiesSpawnUntamed"))){
				return;
			}
			Tameable babee = (Tameable) event.getEntity();
			if (babee.isTamed()){
				babee.setTamed(false);
//				babee.setOwner(null);
			}
		}
	}

	@EventHandler
	public void onEntityTameEvent(EntityTameEvent event){
		if (!event.isCancelled() && event.getEntity() instanceof Ocelot){
			Ocelot kitty = (Ocelot) event.getEntity();
			if (kitty.getCatType() != Ocelot.Type.WILD_OCELOT){
				event.setCancelled(true);
				kitty.setTamed(true);
				kitty.setOwner(event.getOwner());
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
		if (!event.isCancelled()){
			int item = event.getPlayer().getItemInHand().getTypeId();
			Entity clicked = event.getRightClicked();
			if ((clicked.getType() == EntityType.OCELOT && item == plugin.getConfig().getInt("unclaimCatsWith")) || (clicked.getType() == EntityType.WOLF && item == plugin.getConfig().getInt("unclaimDogsWith"))){
				Tameable untamed = (Tameable) clicked;
				String type = (clicked.getType().getName().toLowerCase().matches("ozelot") ? "ocelot" : "wolf");
				Player player = event.getPlayer();
				if (untamed.getOwner() == player && player.hasPermission("adoptafuzz." + type + ".unclaim")){
					if ((player.hasPermission("adoptafuzz.free." + type) || EconHandler.hasEnough(player.getName(), plugin.getConfig().getDouble("costToUntame" + (type.matches("ocelot") ? "Cats" : "Dogs"))))){
						if (!player.hasPermission("adoptafuzz.free." + type))
							if (EconHandler.charge(player.getName(), plugin.getConfig().getDouble("costToUntame" + (type.matches("ocelot") ? "Cats" : "Dogs"))))
								player.sendMessage(ChatColor.GREEN + "You have been charged " + plugin.getConfig().getDouble("costToUntame" + (type.matches("ocelot") ? "Cats" : "Dogs")) + " for unclaiming a pet.");
						event.setCancelled(true);
						untamed.setTamed(false);
//						untamed.setOwner(null);
						if (player.getGameMode() != GameMode.CREATIVE)
							event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount()-1);
					}
				}
			}
		}
	}
}
