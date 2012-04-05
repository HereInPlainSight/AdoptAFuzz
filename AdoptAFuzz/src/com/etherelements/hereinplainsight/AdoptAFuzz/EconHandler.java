package com.etherelements.hereinplainsight.AdoptAFuzz;

import java.util.Random;

import net.milkbowl.vault.economy.Economy;

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
					System.out.println("[" + plugin.getName() + "] Economy system provided by: Vault v" + pVault.getDescription().getVersion() + ", " + economy.getName() + ", and the letter " + new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ").charAt(new Random().nextInt(24)) + ".");
					return;
				}
			}
			System.out.println("[" + plugin.getName() + "] No economy plugin detected.");
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
		if (handler == EconomyHandler.VAULT) {
			if (!hasEnough(player, amount)) {
				return false;
			}else{
				if (economy.isEnabled()) {
					economy.withdrawPlayer(player, amount);
				}
			} 
		}
		return true;
	}
}
