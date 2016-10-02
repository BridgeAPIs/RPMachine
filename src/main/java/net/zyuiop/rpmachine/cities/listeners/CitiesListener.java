package net.zyuiop.rpmachine.cities.listeners;

import net.bridgesapi.api.BukkitBridge;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.Plot;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CitiesListener implements Listener {

	private final CitiesManager manager;
	private final HashSet<EntityType> allowHurt;
	private final HashSet<Material> checkInteract;
	public CitiesListener(CitiesManager manager) {
		this.manager = manager;
		this.allowHurt = new HashSet<>();
		allowHurt.add(EntityType.SKELETON);
		allowHurt.add(EntityType.WITHER);
		allowHurt.add(EntityType.SLIME);
		allowHurt.add(EntityType.ZOMBIE);
		allowHurt.add(EntityType.PIG_ZOMBIE);
		allowHurt.add(EntityType.ENDERMAN);
		allowHurt.add(EntityType.SPIDER);
		allowHurt.add(EntityType.CAVE_SPIDER);
		allowHurt.add(EntityType.WITCH);
		checkInteract = new HashSet<>();
		checkInteract.add(Material.ACACIA_DOOR);
		checkInteract.add(Material.BIRCH_DOOR);
		checkInteract.add(Material.DARK_OAK_DOOR);
		checkInteract.add(Material.IRON_DOOR);
		checkInteract.add(Material.JUNGLE_DOOR);
		checkInteract.add(Material.WOOD_DOOR);
		checkInteract.add(Material.WOODEN_DOOR);
		checkInteract.add(Material.TRAP_DOOR);
		checkInteract.add(Material.FENCE_GATE);
		checkInteract.add(Material.ACACIA_FENCE_GATE);
		checkInteract.add(Material.BIRCH_FENCE_GATE);
		checkInteract.add(Material.DARK_OAK_FENCE_GATE);
		checkInteract.add(Material.JUNGLE_FENCE_GATE);
		checkInteract.add(Material.CHEST);
		checkInteract.add(Material.TRAPPED_CHEST);
		checkInteract.add(Material.ENDER_CHEST);
		checkInteract.add(Material.STONE_BUTTON);
		checkInteract.add(Material.WOOD_BUTTON);
		checkInteract.add(Material.LEVER);
		checkInteract.add(Material.FURNACE);
		checkInteract.add(Material.BURNING_FURNACE);
		checkInteract.add(Material.HOPPER);
		checkInteract.add(Material.HOPPER_MINECART);
		checkInteract.add(Material.DROPPER);
		checkInteract.add(Material.DISPENSER);
		checkInteract.add(Material.BEACON);
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		//if (event.getAction() == Action.RIGHT_CLICK_AIR)
		//	return;

		if (event.getAction() == Action.PHYSICAL) {
			event.setCancelled(!manager.canBuild(event.getPlayer(), event.getClickedBlock().getLocation()));
			return;
		}

		if (event.getItem() != null) {
			Material type = event.getItem().getType();
			if (type == Material.BUCKET || type == Material.WATER_BUCKET || type == Material.LAVA_BUCKET || type == Material.FLINT_AND_STEEL) {
				event.setCancelled(! manager.canBuild(event.getPlayer(), event.getClickedBlock().getLocation()));
				return;
			}
		}

		if (event.getClickedBlock() == null || !checkInteract.contains(event.getClickedBlock().getType()))
			return;

		event.setCancelled(!manager.canInteractWithBlock(event.getPlayer(), event.getClickedBlock().getLocation()));
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		new Thread(() -> {
			Player player = event.getPlayer();
			Set<String> dataKeys = BukkitBridge.get().getPlayerManager().getPlayerData(player.getUniqueId()).getValues().keySet();
			for (String key : dataKeys) {
				if (key.startsWith("topay.")) {
					String city = key.split(".")[0];
					double topay = BukkitBridge.get().getPlayerManager().getPlayerData(player.getUniqueId()).getDouble(key);
					player.sendMessage(ChatColor.RED + "ATTENTION ! Votre compte ne contient pas assez d'argent pour payer vos impots.");
					player.sendMessage(ChatColor.RED + "Vous devez " + ChatColor.AQUA + topay + ChatColor.RED + " à la ville de " + ChatColor.AQUA + city);
					player.sendMessage(ChatColor.RED + "Payez les rapidement avec " + ChatColor.AQUA + "/city paytaxes " + city);
				}
			}
		}).start();
	}

	@EventHandler
	public void onGamemode(PlayerGameModeChangeEvent event) {
		if (! BukkitBridge.get().getPermissionsManager().hasPermission(event.getPlayer(), "rp.gamemode") && event.getNewGameMode() != GameMode.SURVIVAL) {
			event.getPlayer().setGameMode(GameMode.SURVIVAL);
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Vous n'avez pas le droit d'accéder au gamemode créatif.");
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		event.setCancelled(!manager.canBuild(event.getPlayer(), event.getBlock().getLocation()));
	}

	@EventHandler
	public void onEntityExplodeEvent(EntityExplodeEvent entityExplodeEvent) {
		entityExplodeEvent.blockList().clear();
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent explodeEvent) {
		explodeEvent.blockList().clear();
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		event.setCancelled(!manager.canBuild(event.getPlayer(), event.getBlock().getLocation()));
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		event.setCancelled(!manager.canBuild(event.getPlayer(), event.getRightClicked().getLocation()));
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onHangingEntity(HangingBreakByEntityEvent event) {
		if (event.getRemover() instanceof Player)
			event.setCancelled(!manager.canBuild((Player) event.getRemover(), event.getEntity().getLocation()));
	}


	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && !allowHurt.contains(event.getEntityType()))
			event.setCancelled(!manager.canBuild((Player) event.getDamager(), event.getEntity().getLocation()));
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		new Thread(() -> {
			UUID id = event.getPlayer().getUniqueId();
			if (!isSameChunk(event.getFrom(), event.getTo())) {
				City c1 = manager.getCityHere(event.getFrom().getChunk());
				City c2 = manager.getCityHere(event.getTo().getChunk());
				boolean entering = false;
				boolean leaving = false;

				if (c1 != null && c2 != null && c1.getCityName().equals(c2.getCityName())) {

					Plot plot = c1.getPlotHere(event.getFrom());
					Plot to = c1.getPlotHere(event.getTo());

					if (plot != null && to != null && plot.getPlotName().equals(to.getPlotName()))
						return;

					boolean pOverride = (c1.getCouncils().contains(id) || c1.getMayor().equals(id));

					if (plot != null && (id.equals(plot.getOwner()) || plot.getPlotMembers().contains(id))) {
						//event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez votre parcelle.");
						leaving = true;
					} else if (pOverride && plot != null) {
						event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez la parcelle " + plot.getPlotName());
					}

					if (to != null && (id.equals(to.getOwner()) || to.getPlotMembers().contains(id))) {
						//event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur votre parcelle.");
						entering = true;
					} else if (pOverride && to != null) {
						event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur la parcelle " + to.getPlotName());
					}

					return;
				}

				if (c1 != null) {
					event.getPlayer().sendMessage(ChatColor.GOLD + "Vous quittez " + ChatColor.YELLOW + c1.getCityName() + ChatColor.GOLD + " !");
					boolean c1Override = (c1.getCouncils().contains(id) || c1.getMayor().equals(id));
					Plot from = c1.getPlotHere(event.getFrom());

					if (from != null && (id.equals(from.getOwner()) || from.getPlotMembers().contains(id))) {
						//event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez votre parcelle.");
						leaving = true;
					} else if (c1Override && from != null) {
						event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez la parcelle " + from.getPlotName());
					}
				}

				if (c2 != null) {
					event.getPlayer().sendMessage(ChatColor.GOLD + "Vous entrez à " + ChatColor.YELLOW + c2.getCityName() + ChatColor.GOLD + " !");

					boolean c2Override = (c2.getCouncils().contains(id) || c2.getMayor().equals(id));
					Plot to = c2.getPlotHere(event.getTo());

					if (to != null && (id.equals(to.getOwner()) || to.getPlotMembers().contains(id))) {
						//event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur votre parcelle.");
						entering = true;
					} else if (c2Override && to != null) {
						event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur la parcelle " + to.getPlotName());
					}
				}

				if (entering && !leaving) {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur votre parcelle.");
				} else if (!entering && leaving) {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez votre parcelle.");
				}
			} else {
				// Same chunk, same city.
				City city = manager.getCityHere(event.getFrom().getChunk());
				if (city == null)
					return;

				Plot plot = city.getPlotHere(event.getFrom());
				Plot to = city.getPlotHere(event.getTo());

				boolean pOverride = (city.getCouncils().contains(id) || city.getMayor().equals(id));
				if (plot != null && to != null && plot.getPlotName().equals(to.getPlotName()))
					return;

				if (plot != null && (id.equals(plot.getOwner()) || plot.getPlotMembers().contains(id))) {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez votre parcelle.");
				} else if (pOverride && plot != null) {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez la parcelle " + plot.getPlotName());
				}

				if (to != null && (id.equals(to.getOwner()) || to.getPlotMembers().contains(id))) {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur votre parcelle.");
				} else if (pOverride && to != null) {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur la parcelle " + to.getPlotName());
				}
			}
		}).start();
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent event) {
		City city = manager.getPlayerCity(event.getPlayer().getUniqueId());
		if (city != null) {
			event.setFormat(ChatColor.DARK_AQUA + "["+city.getCityName()+"]" + event.getFormat());
		}
	}

	boolean isSameChunk(Location l1, Location l2) {
		Chunk c1 = l1.getChunk();
		Chunk c2 = l2.getChunk();
		return (c1.getX() == c2.getX() && c1.getZ() == c2.getZ());
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		event.setCancelled(true);
		final Chunk chunk = event.getChunk();
		Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(),
				() -> chunk.unload(true, true), 60 * 2);
	}
}
