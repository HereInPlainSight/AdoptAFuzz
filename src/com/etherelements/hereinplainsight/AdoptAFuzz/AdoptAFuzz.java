package com.etherelements.hereinplainsight.AdoptAFuzz;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AdoptAFuzz extends JavaPlugin implements Listener{

	public void onEnable(){
		final FileConfiguration config = this.getConfig();

		config.options().copyDefaults(true);
		saveConfig();
		AdoptionListener.refreshConfig(this);
		EconHandler.canHazEconomy(this);

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
	
	public void sendToConsole(String msg){
		getServer().getConsoleSender().sendMessage("[" + this.getName() + "] " + msg);
	}
}
