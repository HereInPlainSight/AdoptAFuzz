package com.etherelements.hereinplainsight.AdoptAFuzz;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AdoptAFuzz extends JavaPlugin implements Listener{

	public void onEnable(){
		final FileConfiguration config = this.getConfig();

		config.options().copyDefaults(true);
		saveConfig();
		EconHandler.canHazEconomy(this);

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
	}

	public void onDisable(){
	}

	@EventHandler
	public void onCreatureSpawnEvent(CreatureSpawnEvent event){
		if (!event.isCancelled() && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING){
			if ((event.getEntity().getType() != EntityType.OCELOT && !getConfig().getBoolean("kittensSpawnUntamed")) || (event.getEntity().getType() != EntityType.WOLF && !getConfig().getBoolean("puppiesSpawnUntamed"))){
				return;
			}
			Tameable babee = (Tameable) event.getEntity();
			if (babee.isTamed()){
				babee.setTamed(false);
				babee.setOwner(null);
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
			if ((clicked.getType() == EntityType.OCELOT && item == getConfig().getInt("unclaimCatsWith")) || (clicked.getType() == EntityType.WOLF && item == getConfig().getInt("unclaimDogsWith"))){
				Tameable untamed = (Tameable) clicked;
				String type = clicked.getType().getName().toLowerCase().matches("ozelot") ? "ocelot" : "wolf";
				Player player = event.getPlayer();
				if (untamed.getOwner() == player && player.hasPermission(this.getName() + "." + type + ".unclaim")){
					System.out.println(player.getName() + "has: " + this.getName() + "." + type + ".unclaim (" + player.hasPermission(this.getName() + "." + type + ".unclaim") + ")");
					if ((player.hasPermission(this.getName() + ".free." + type) || EconHandler.hasEnough(player.getName(), getConfig().getDouble("costToUntame" + (type.matches("ocelot") ? "Cats" : "Dogs"))))){
						if (!player.hasPermission(this.getName() + ".free." + type))
							if (EconHandler.charge(player.getName(), getConfig().getDouble("costToUntame" + (type.matches("ocelot") ? "Cats" : "Dogs"))))
								player.sendMessage("You have been charged " + getConfig().getDouble("costToUntame" + (type.matches("ocelot") ? "Cats" : "Dogs")) + " for unclaiming a pet.");
						event.setCancelled(true);
						untamed.setTamed(false);
						untamed.setOwner(null);
						if (player.getGameMode() != GameMode.CREATIVE)
							event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount()-1);
					}
				}
			}
		}
	}
}
