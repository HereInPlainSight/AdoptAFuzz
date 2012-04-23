package com.etherelements.hereinplainsight.AdoptAFuzz;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AdoptAFuzz extends JavaPlugin{

	public void onEnable(){
		final FileConfiguration config = this.getConfig();

		config.options().copyDefaults(true);
		saveConfig();
		if (config.options().configuration().contains("untamedSpawns")){
			updateConfig("untamedSpawns", config);
		}
		AdoptionListener.refreshConfig(this);
		EconHandler.canHazEconomy(this);
		if (config.options().configuration().getBoolean("avoidbugs")){
			sendToConsole("AVOID BUGS ENABLED!");
			sendToConsole("Previous ownership will NOT be maintained any more!");
		}

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new AdoptionListener(), this);
	}

	public void onDisable(){
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		boolean player = sender instanceof Player;
		if (cmd.getName().compareToIgnoreCase("adoptafuzz") == 0 && args.length == 0){
			if ((player ? !sender.hasPermission("adoptafuzz.commands.reload") : false)){
				sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
				return true;
			}
			this.reloadConfig();
			AdoptionListener.refreshConfig(this);
			EconHandler.canHazEconomy(this);
			if (player){
				sender.sendMessage("Configuration reloaded.");
				sendToConsole("Configuration reloaded by " + sender.getName() + ".");
			}else{
				sendToConsole("Configuration reloaded.");
			}
		}else{
			String msg = "Arguments not supported.";
			if (player)
				sender.sendMessage(msg);
			sendToConsole(msg);
		}
		return true;
	}
	
	public void updateConfig(String old, FileConfiguration config){
		sendToConsole("Updating configuration...");
		if (old.compareTo("untamedSpawn") == 1){
			config.createSection("spawns");
			config.set("spawns.kittens.untamed", config.getBoolean("untamedSpawns.kittens"));
			config.set("spawns.puppies.untamed", config.getBoolean("untamedSpawns.puppies"));
			config.set("untamedSpawns", null);
			saveConfig();
		}
		this.reloadConfig();
		sendToConsole("Done!");
	}
	
	public void sendToConsole(String msg){
		getServer().getConsoleSender().sendMessage("[" + this.getName() + "] " + msg);
	}
}
