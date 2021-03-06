package com.etherelements.hereinplainsight.AdoptAFuzz;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconHandler {
	public static EconomyHandler handler;
	public static Economy economy = null;

	public enum EconomyHandler{
		VAULT, NONE;
	}

	protected static void canHazEconomy(AdoptAFuzz plugin){
		if (plugin.getConfig().getBoolean("enableEconomy")){
			Plugin pVault = plugin.getServer().getPluginManager().getPlugin("Vault");
			if (pVault != null){
				RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
				economy = economyProvider.getProvider();
				if (economy != null) {
					handler = EconomyHandler.VAULT;
					plugin.sendToConsole("Economy system provided by: Vault v" + pVault.getDescription().getVersion() + " and " + economy.getName() + ".");
					return;
				}
			}
			plugin.sendToConsole("No economy plugin detected.");
		}
		handler = EconomyHandler.NONE;
	}

	public static boolean hasEnough(String player, double amount){
		if (handler == EconomyHandler.VAULT){
			if (economy.isEnabled()){
				return economy.has(player, amount);
			}
		}
		return true;
	}

	public static boolean charge(String player, double amount){
		if (handler == EconomyHandler.VAULT){
			if (!hasEnough(player, amount)){
				return false;
			}else{
				if (economy.isEnabled()){
					economy.withdrawPlayer(player, amount);
				}
			} 
		}
		return true;
	}
	
	public static boolean payout(String player, double amount){
		if (handler == EconomyHandler.VAULT)
			if (economy.isEnabled())
				economy.depositPlayer(player, amount);
		return true;
	}
	
	public static boolean payout(OfflinePlayer player, double amount){
		return payout(player.getName(), amount);
	}
}
