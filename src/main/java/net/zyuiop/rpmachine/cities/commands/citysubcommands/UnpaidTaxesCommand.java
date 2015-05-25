package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.bridgesapi.api.BukkitBridge;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class UnpaidTaxesCommand implements SubCommand {

	private final CitiesManager citiesManager;

	public UnpaidTaxesCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Affiche les impôts impayés dans votre ville.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
			} else if (!city.getMayor().equals(player.getUniqueId()) && !city.getCouncils().contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "Vous ne pouvez pas gérer les impôts de cette ville.");
			} else {
				player.sendMessage(ChatColor.GOLD + "-----[ Impôts impayés ]-----");
				for (Map.Entry<UUID, Double> entry : city.getTaxesToPay().entrySet()) {
					String name = BukkitBridge.get().getUUIDTranslator().getName(entry.getKey(), true);
					player.sendMessage(ChatColor.YELLOW + " - " + name + " doit " + ChatColor.RED + entry.getValue() + ChatColor.YELLOW + " à la ville.");
				}
			}
		}
	}
}