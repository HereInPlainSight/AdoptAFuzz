package com.etherelements.hereinplainsight.AdoptAFuzz;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.etherelements.hereinplainsight.AdoptAFuzz.EconHandler.EconomyHandler;

public class AdoptionListener implements Listener{
	public static FileConfiguration config;

	public static void refreshConfig(AdoptAFuzz plugin){
		config = plugin.getConfig();
	}
	
	public boolean canTame(Player player, LivingEntity animal){
		String action = toTameOrAdopt((Tameable) animal);
		String type = (animal.getType().getName().toLowerCase().matches("ozelot") ? "ocelots" : "wolves");
		boolean freebie = player.hasPermission("adoptafuzz.free.economy." + action + "." + type);
		double costs = config.getDouble("costs." + action + "." + type, 0);
		if (!freebie && !EconHandler.hasEnough(player.getName(), costs)){
			player.sendMessage(ChatColor.RED + "You do not have enough to do that. (Cost: " + costs + ")");
			return false;
		}
		if (freebie || EconHandler.hasEnough(player.getName(), costs)){
			if (costs != 0 && !freebie && EconHandler.handler == EconomyHandler.VAULT)
				if (EconHandler.charge(player.getName(), costs))
					player.sendMessage(ChatColor.GREEN + "You have been charged " + costs + " for your new " + (type.matches("ocelots") ? "cat" : "dog") + "!");
		}
		Tameable tmp = (Tameable) animal;
		tmp.setOwner(null);
		return true;
	}

	public String toTameOrAdopt(Tameable animal){
		if (animal.getOwner() != null)
			return "adopt";
		else
			return "tame";
	}

	@EventHandler
	public void onCreatureSpawnEvent(CreatureSpawnEvent event){
		if (!event.isCancelled() && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING){
			EntityType type = event.getEntity().getType();
			if ((type != EntityType.OCELOT && !config.getBoolean("untamedSpawns.kittens", false)) || (type != EntityType.WOLF && !config.getBoolean("untamedSpawns.puppies", false))){
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
		if (event.isCancelled())
			return;
		if (event.getEntity() instanceof Ocelot){
			Ocelot kitty = (Ocelot) event.getEntity();
			Player player = (Player) event.getOwner();
			if (kitty.getCatType() != Ocelot.Type.WILD_OCELOT){
				event.setCancelled(true);
				if (canTame(player, kitty)){
					kitty.setTamed(true);
					kitty.setOwner(player);
					player.getItemInHand().setAmount(player.getItemInHand().getAmount()-1);
				}
			}else{
				event.setCancelled(!canTame(player, kitty));
			}
		}else if (event.getEntity() instanceof Wolf){
			if (!canTame((Player) event.getOwner(), event.getEntity()))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
		if (!event.isCancelled()){
			int item = event.getPlayer().getItemInHand().getTypeId();
			Entity clicked = event.getRightClicked();
			if ((clicked.getType() == EntityType.OCELOT && item == config.getInt("unclaimWith.cats", 350)) || (clicked.getType() == EntityType.WOLF && item == config.getInt("unclaimWith.dogs", 287))){
				Tameable untamed = (Tameable) clicked;
				String type = (clicked.getType().getName().toLowerCase().matches("ozelot") ? "ocelots" : "wolves");
				Player player = event.getPlayer();
				if (untamed.getOwner() == player && player.hasPermission("adoptafuzz." + type + ".unclaim")){
					double cost = config.getDouble("costs.unclaim." + (type.matches("ocelots") ? "cats" : "dogs"));
					boolean freebie = player.hasPermission("adoptafuzz.free.economy.unclaim." + type);
					if (freebie || EconHandler.hasEnough(player.getName(), cost)){
						if (cost != 0 && !freebie && EconHandler.handler == EconomyHandler.VAULT)
							if (EconHandler.charge(player.getName(), cost))
								player.sendMessage(ChatColor.GREEN + "You have been charged " + cost + " for unclaiming a " + (type.matches("ocelots") ? "cat" : "dog") + ".");
						event.setCancelled(true);
						untamed.setTamed(false);
						if (player.getGameMode() != GameMode.CREATIVE && !player.hasPermission("adoptafuzz.free.item." + type))
							event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount()-1);
					}
				}
			}
		}
	}
}
