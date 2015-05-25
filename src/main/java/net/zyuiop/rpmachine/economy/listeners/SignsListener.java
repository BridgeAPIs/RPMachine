package net.zyuiop.rpmachine.economy.listeners;


import net.bridgesapi.api.BukkitBridge;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.Plot;
import net.zyuiop.rpmachine.economy.shops.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class SignsListener implements Listener {

	private final RPMachine plugin;

	public SignsListener(RPMachine plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		if (!event.getPlayer().getWorld().getName().equals("world"))
			return;

		if (event.getLine(0).equalsIgnoreCase("Shop")) {
			String price = event.getLine(1);
			String bundleSize = event.getLine(2);
			String action = event.getLine(3);

			if (price == null || bundleSize == null || action == null) {
				showSignsRules(event.getPlayer());
				event.getBlock().breakNaturally();
			} else {
				try {
					Double dprice = Double.valueOf(price);
					Integer ibundle = Integer.valueOf(bundleSize);

					ShopSign sign = new ShopSign(event.getBlock().getLocation());

					if (ibundle > 64) {
						event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas vendre des lots de plus de 64 items.");
						return;
					} else {
						sign.setAmountPerPackage(ibundle);
					}

					if (dprice > 9999999) {
						event.getPlayer().sendMessage(ChatColor.RED + "Le prix entré est trop grand.");
						return;
					} else {
						sign.setPrice(dprice);
					}

					if (action.equalsIgnoreCase("achat")) {
						sign.setAction(ShopAction.BUY);
					} else if (action.equalsIgnoreCase("vente")) {
						sign.setAction(ShopAction.SELL);
					} else {
						showSignsRules(event.getPlayer());
					}

					sign.setOwnerId(event.getPlayer().getUniqueId());
					sign.setOwnerName(event.getPlayer().getName());

					event.getPlayer().sendMessage(ChatColor.AQUA + "["+ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] "+ChatColor.GREEN + "Votre boutique est presque prête à l'emploi. Cliquez droit avec l'item que vous souhaitez vendre pour terminer la configuration.");

					plugin.getShopsManager().create(sign);
				} catch (Exception e) {
					showSignsRules(event.getPlayer());
				}
			}
		} else if (event.getLine(0).equalsIgnoreCase("AdminShop") && BukkitBridge.get().getPermissionsManager().hasPermission(event.getPlayer(), "rp.adminshop")) {
			String price = event.getLine(1);
			String bundleSize = event.getLine(2);
			String action = event.getLine(3);

			if (price == null || bundleSize == null || action == null) {
				showSignsRules(event.getPlayer());
			} else {
				try {
					Double dprice = Double.valueOf(price);
					Integer ibundle = Integer.valueOf(bundleSize);

					AdminShopSign sign = new AdminShopSign(event.getBlock().getLocation());

					if (ibundle > 64) {
						event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas vendre des lots de plus de 64 items.");
						return;
					} else {
						sign.setAmountPerPackage(ibundle);
					}

					if (dprice > 9999999) {
						event.getPlayer().sendMessage(ChatColor.RED + "Le prix entré est trop grand.");
						return;
					} else {
						sign.setPrice(dprice);
					}

					if (action.equalsIgnoreCase("achat")) {
						sign.setAction(ShopAction.BUY);
					} else if (action.equalsIgnoreCase("vente")) {
						sign.setAction(ShopAction.SELL);
					} else {
						showSignsRules(event.getPlayer());
					}

					sign.setOwnerId(event.getPlayer().getUniqueId());
					event.getPlayer().sendMessage(ChatColor.AQUA + "["+ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] "+ChatColor.GREEN + "Votre boutique est presque prête à l'emploi. Cliquez droit avec l'item que vous souhaitez vendre pour terminer la configuration.");

					plugin.getShopsManager().create(sign);
				} catch (Exception e) {
					showSignsRules(event.getPlayer());
				}
			}
		} else if (event.getLine(0).equalsIgnoreCase("PlotShop")) {
			String price = event.getLine(1);
			String plotname = event.getLine(2);
			String restrict = event.getLine(3);

			if (price == null || plotname == null || restrict == null) {
				showPlotSignsRules(event.getPlayer());
				event.getBlock().breakNaturally();
			} else if (!event.getBlock().getWorld().getName().equals("world")) {
				event.getPlayer().sendMessage(ChatColor.RED + "Votre panneau ne se trouve pas dans une ville.");
				event.getBlock().breakNaturally();
			} else {
				try {
					Double dprice = Double.valueOf(price);
					City city = RPMachine.getInstance().getCitiesManager().getCityHere(event.getBlock().getChunk());
					if (city == null) {
						event.getPlayer().sendMessage(ChatColor.RED + "Votre panneau ne se trouve pas dans une ville.");
						event.getBlock().breakNaturally();
						return;
					}

					Plot plot = city.getPlots().get(plotname);
					if (plot == null) {
						event.getPlayer().sendMessage(ChatColor.RED + "Cette parcelle n'existe pas.");
						event.getBlock().breakNaturally();
						return;
					}

					if (!event.getPlayer().getUniqueId().equals(plot.getOwner())) {
						if (! (city.getMayor().equals(event.getPlayer().getUniqueId()) || (plot.getOwner() == null && city.getCouncils().contains(event.getPlayer().getUniqueId())))) {
							event.getPlayer().sendMessage(ChatColor.RED + "Vous n'êtes pas propriétaire de cette parcelle.");
							event.getBlock().breakNaturally();
							return;
						}
					}

					if (dprice > 9999999) {
						event.getPlayer().sendMessage(ChatColor.RED + "Le prix entré est trop grand.");
						return;
					}

					boolean citizensOnly = restrict.equalsIgnoreCase("citizens");
					PlotSign sign = new PlotSign(event.getBlock().getLocation(), plot.getPlotName(), citizensOnly, city.getCityName());
					sign.setOwnerId(event.getPlayer().getUniqueId());
					sign.setPrice(dprice);

					event.getPlayer().sendMessage(ChatColor.AQUA + "["+ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] "+ChatColor.GREEN + "Votre boutique est prête à l'emploi.");

					plugin.getShopsManager().create(sign);
				} catch (Exception e) {
					showPlotSignsRules(event.getPlayer());
				}
			}
		}
	}

	void showSignsRules(Player player) {
		player.sendMessage(ChatColor.RED + "Merci de respecter les règles de panneaux de shops : ");
		player.sendMessage(ChatColor.YELLOW + "- Shop");
		player.sendMessage(ChatColor.YELLOW + "- <prix d'achat/vente>");
		player.sendMessage(ChatColor.YELLOW + "- <taille des lots>");
		player.sendMessage(ChatColor.YELLOW + "- <achat (pour ACHETER aux joueurs) / vente (pour VENDRE aux joueurs)>");
	}

	void showPlotSignsRules(Player player) {
		player.sendMessage(ChatColor.RED + "Merci de respecter les règles de panneaux de ventes de parcelles : ");
		player.sendMessage(ChatColor.YELLOW + "- PlotShop");
		player.sendMessage(ChatColor.YELLOW + "- <prix de vente> : Prix auquel vous vendez la parcelle. La ville prend 20% de taxes.");
		player.sendMessage(ChatColor.YELLOW + "- <nom de la parcelle> : nom de la parcelle à vendre");
		player.sendMessage(ChatColor.YELLOW + "- <all|citizens> : définit si la parcelle peut être achetée par tous (all) ou par les citoyens de la ville uniquement (citizens)");
	}

	@EventHandler
	public void onSignClick(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null)
			return;
		AbstractShopSign sign = plugin.getShopsManager().get(event.getClickedBlock().getLocation());
		if (sign == null)
			return;
		else if (event.getPlayer().isSneaking() && BukkitBridge.get().getPermissionsManager().hasPermission(event.getPlayer(), "sign.debug")) {
			Player p = event.getPlayer();
			p.sendMessage(ChatColor.YELLOW + "-----[ Débug Shop ] -----");
			p.sendMessage(ChatColor.YELLOW + "Price : " + sign.getPrice());
			if (sign instanceof ShopSign) {
				ShopSign ssign = (ShopSign) sign;
				p.sendMessage(ChatColor.YELLOW + "Action : " + ssign.getAction());
				p.sendMessage(ChatColor.YELLOW + "Item : " + ssign.getItemType());
				p.sendMessage(ChatColor.YELLOW + "Amount per package : " + ssign.getAmountPerPackage());
				p.sendMessage(ChatColor.YELLOW + "Owner (Name/UUID) : " + ((ShopSign) sign).getOwnerName() + " / " + sign.getOwnerId());
				p.sendMessage(ChatColor.YELLOW + "Available items : " + ((ShopSign) sign).getAvailable());
			} else if (sign instanceof AdminShopSign) {
				AdminShopSign asign = (AdminShopSign) sign;
				p.sendMessage(ChatColor.YELLOW + "Action : " + asign.getAction());
				p.sendMessage(ChatColor.YELLOW + "Item : " + asign.getItemType());
				p.sendMessage(ChatColor.YELLOW + "Amount per package : " + asign.getAmountPerPackage());
				p.sendMessage(ChatColor.YELLOW + "Admin Shop");
			} else {
				PlotSign psign = (PlotSign) sign;
				p.sendMessage(ChatColor.YELLOW + "Parcelle : " + psign.getPlotName());
				p.sendMessage(ChatColor.YELLOW + "Ville : " + psign.getCityName());
				p.sendMessage(ChatColor.YELLOW + "Citizens Only : " + psign.isCitizensOnly());
			}
		} else
			sign.rightClick(event.getPlayer(), event);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.SIGN || event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN) {
			event.setCancelled(true);
			AbstractShopSign sign = plugin.getShopsManager().get(event.getBlock().getLocation());
			if (sign == null)
				event.setCancelled(false);
			else
				sign.breakSign(event.getPlayer());
		}
	}
}