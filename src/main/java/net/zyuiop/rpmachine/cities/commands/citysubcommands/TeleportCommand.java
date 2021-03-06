package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand implements SubCommand {

	private final CitiesManager citiesManager;
	private final double price;
	private final boolean fromCityOnly;

	public TeleportCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;

		price = RPMachine.getInstance().getConfig().getDouble("citytp.cost", 1D);
		fromCityOnly = RPMachine.getInstance().getConfig().getBoolean("citytp.fromCityOnly", false);
	}


	@Override
	public String getUsage() {
		return "[ville]";
	}

	@Override
	public String getDescription() {
		return "Vous téléporte dans votre ville. Si [ville] est fourni, vous téléporte dans la ville [ville] pour un prix de " + price + " " + EconomyManager.getMoneyName();
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (fromCityOnly && !RPMachine.getInstance().getCitiesManager().checkCityTeleport(player)) {
				return;
			}

			City playerCity = citiesManager.getPlayerCity(player.getUniqueId());
			City target;
			boolean pay = false;
			if (args.length > 0) {
				target = citiesManager.getCity(args[0]);
				if (target == null) {
					sender.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
					return;
				} else if (target.isRequireInvite() && !target.getInvitedUsers().contains(player.getUniqueId()) && (playerCity == null || !playerCity.getCityName().equalsIgnoreCase(target.getCityName()))) {
					sender.sendMessage(ChatColor.RED + "Cette ville est privée.");
					return;
				} else if (playerCity == null || !playerCity.getCityName().equalsIgnoreCase(target.getCityName())) {
					pay = true;
				}
			} else {
				target = playerCity;
				if (target == null) {
					sender.sendMessage(ChatColor.RED + "Vous ne faites partie d'aucune ville.");
					return;
				}
			}

			VirtualLocation vspawn = target.getSpawn();
			if (vspawn == null) {
				sender.sendMessage(ChatColor.RED + "Cette ville ne dispose d'aucun point de spawn.");
				return;
			}

			Location spawn = vspawn.getLocation();
			if (!spawn.getChunk().isLoaded())
				spawn.getChunk().load();

			if (pay) {
				RPMachine.getInstance().getEconomyManager().withdrawMoneyWithBalanceCheck(player.getUniqueId(), price, (newAmount, difference) -> {
					if (difference == 0) {
						player.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'argent pour faire cela.");
					} else {
						target.setMoney(target.getMoney() + price);
						citiesManager.saveCity(target);
						Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> player.teleport(spawn));
						ReflectionUtils.getVersion().playEndermanTeleport(spawn, player);
						player.sendMessage(ChatColor.GOLD + "Vous avez été téléporté et " + price + " " + EconomyManager.getMoneyName() + " vous a été débité.");
					}
				});
			} else {
				Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> player.teleport(spawn));
				ReflectionUtils.getVersion().playEndermanTeleport(spawn, player);
				player.sendMessage(ChatColor.GOLD + "Vous avez été téléporté.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas faire cela.");
		}

	}
}
