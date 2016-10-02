package net.zyuiop.rpmachine.economy.listeners;

import net.md_5.bungee.api.ChatColor;
import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.PlayerData;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.economy.jobs.Job;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

import java.util.Iterator;

import static org.bukkit.entity.EntityType.*;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class PlayerListener implements Listener {

	private final RPMachine plugin;

	public PlayerListener(RPMachine plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		plugin.getScoreboardManager().addPlayer(event.getPlayer());
		event.setJoinMessage(BukkitBridge.get().getPermissionsManager().getDisplay(BukkitBridge.get().getPermissionsManager().getApi().getUser(event.getPlayer().getUniqueId())) + event.getPlayer().getName() + ChatColor.YELLOW + org.bukkit.ChatColor.ITALIC + " a rejoint le serveur !");

		PlayerData d = BukkitBridge.get().getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());
		if (!d.getKeys().contains("rpmoney")) {
			d.setDouble("rpmoney", 150.0);
			event.setJoinMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Bienvenue à " + BukkitBridge.get().getPermissionsManager().getDisplay(BukkitBridge.get().getPermissionsManager().getApi().getUser(event.getPlayer().getUniqueId())) + event.getPlayer().getName() + ChatColor.YELLOW + org.bukkit.ChatColor.ITALIC + " !");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeave(PlayerQuitEvent event) {
		plugin.getScoreboardManager().removePlayer(event.getPlayer());
		event.setQuitMessage(BukkitBridge.get().getPermissionsManager().getDisplay(BukkitBridge.get().getPermissionsManager().getApi().getUser(event.getPlayer().getUniqueId())) + event.getPlayer().getName() + ChatColor.YELLOW + org.bukkit.ChatColor.ITALIC + " s'est déconnecté !");
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getBlockPlaced().getType() == Material.MOB_SPAWNER) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		ItemStack stack = player.getItemInHand();
		if (stack != null && stack.getType() == Material.MOB_SPAWNER) {
			Job job = RPMachine.getInstance().getJobsManager().getJob(player.getUniqueId());
			if (job != null && job.getJobName().equalsIgnoreCase("chasseur")) {
				EntityType type = event.getRightClicked().getType();
				if (type == COW || type == SHEEP || type == MUSHROOM_COW || type == HORSE || type == CHICKEN || type == PIG || type == RABBIT) {
					if (!(event.getRightClicked() instanceof Ageable) || ((Ageable) event.getRightClicked()).isAdult()) {
						event.getRightClicked().remove();
						player.sendMessage(ChatColor.GREEN + "Entité capturée !");

						Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
							SpawnEgg egg = new SpawnEgg(type);
							ItemStack hand = player.getInventory().getItemInHand();
							if (hand.getAmount() > 1)
								hand.setAmount(hand.getAmount() - 1);
							else
								hand = null;
							player.setItemInHand(hand);
							player.getInventory().addItem(egg.toItemStack(1));
						}, 3);
					} else {
						player.sendMessage(ChatColor.RED + "Impossible de capturer un bébé !");
					}
				} else {
					player.sendMessage(ChatColor.RED + "Cette entité ne peut pas être capturée.");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Seuls les chasseurs peuvent attraper des entités !");
			}
		} else if (stack != null) {
			if (stack.getType() == Material.MONSTER_EGG) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
		if (entityDeathEvent.getEntityType() != EntityType.PLAYER) {
			Iterator<ItemStack> iter = entityDeathEvent.getDrops().iterator();
			while (iter.hasNext()) {
				ItemStack stack = iter.next();
				if ((stack.getType() == Material.IRON_INGOT && entityDeathEvent.getEntity().getType() == EntityType.IRON_GOLEM) || stack.getType() == Material.GOLD_NUGGET)
					iter.remove();
			}
		}
	}

}
