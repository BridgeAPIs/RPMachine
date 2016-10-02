package net.zyuiop.rpmachine.cities;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.CityFloor;
import net.zyuiop.rpmachine.cities.data.VirtualChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import com.google.gson.Gson;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CitiesManager {

	private final RPMachine rpMachine;
	private ConcurrentHashMap<String, City> cities = new ConcurrentHashMap<>();
	private TreeSet<CityFloor> floors = new TreeSet<>(new CitiesComparator());
	private HashSet<UUID> bypass = new HashSet<>();
	private int creationPrice;
	private int increaseFactor;

	public void addBypass(UUID id) {
		bypass.add(id);
	}

	public void removeBypass(UUID id) {
		bypass.remove(id);
	}

	public boolean isBypassing(UUID id) {
		return bypass.contains(id);
	}

	public CitiesManager(RPMachine plugin) {
		this.rpMachine = plugin;
		File cityFolder = new File(plugin.getDataFolder().getPath() + "/cities");
		if (!cityFolder.isDirectory())
			return;

		Gson gson = new Gson();

		for (File file : cityFolder.listFiles()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				City city = gson.fromJson(reader, City.class);
				cities.put(city.getCityName(), city);
				plugin.getLogger().info("Loaded city " + city.getCityName());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// Load floors
		for (Map<?, ?> floor : rpMachine.getConfig().getMapList("floors")) {
			String name = (String) floor.get("name");
			int inhabitants = (Integer) floor.get("inhabitants");
			int maxsurface = (Integer) floor.get("max-chunks");
			int maxtaxes = (Integer) floor.get("max-taxes");
			int chunkPrice = (Integer) floor.get("chunk-price");

			floors.add(new CityFloor(name, inhabitants, maxsurface, maxtaxes, chunkPrice));
			plugin.getLogger().info("Loaded CityFloor " + name);
		}

		creationPrice = rpMachine.getConfig().getInt("createcity.price", 500);
		increaseFactor = rpMachine.getConfig().getInt("createcity.incrfactor", 5);
	}

	public void payTaxes(boolean force) {
		for (City city : cities.values()) {
			city.payTaxes(force);
		}
	}

 	public City getCity(String name) {
		return cities.get(name);
	}

	public ConcurrentHashMap<String, City> getCities() {
		return cities;
	}

	public boolean createCity(City city) {
		if (cities.containsKey(city.getCityName()))
			return false;

		String fileName = city.getCityName().replace("/", "_");
		fileName = fileName.replace("\\", "_");
		File file = new File(rpMachine.getDataFolder().getPath() + "/cities/" + fileName + ".json");
		if (file.exists()) {
			int i = 1;
			while (i <= 100 && file.exists()) {
				file = new File(rpMachine.getDataFolder().getPath() + "/cities/" + fileName + "(" + i + ").json");
				i++;
			}

			if (file.exists())
				return false;
		}

		try {
			city.setFileName(file.getName());
			this.cities.put(city.getCityName(), city);
			boolean create = file.createNewFile();
			if (!create)
				return false;
			saveCity(city);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void saveCity(City city) {
		new Thread(() -> {
			File file = new File(rpMachine.getDataFolder().getPath() + "/cities/" + city.getFileName());
			if (!file.exists())
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file));
				new Gson().toJson(city, City.class, writer);
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}).start();
	}

	public CityFloor getFloor(City city) {
		for (CityFloor floor : floors) {
			if (floor.getInhabitants() <= city.countInhabitants())
				return floor;
		}
		return null;
	}

	public TreeSet<CityFloor> getFloors() {
		return floors;
	}

	public City getPlayerCity(UUID player) {
		for (City city : cities.values()) {
			if (city.getInhabitants().contains(player))
				return city;
		}
		return null;
	}

	public City getCityHere(Chunk chunk) {
		VirtualChunk vchunk = new VirtualChunk(chunk);
		for (City city : cities.values()) {
			if (city.getChunks().contains(vchunk))
				return city;
		}
		return null;
	}

	public boolean canBuild(Player player, Location location) {
		if (bypass.contains(player.getUniqueId()))
			return true;

		if (location.getWorld().getName().equals("world")) {
			City city = getCityHere(location.getChunk());
			return city == null || city.canBuild(player, location);
		} else {
			return true;
		}
	}

	public boolean canInteractWithBlock(Player player, Location location) {
		if (bypass.contains(player.getUniqueId()))
			return true;

		if (location.getWorld().getName().equals("world")) {
			City city = getCityHere(location.getChunk());
			return city == null || city.canInteractWithBlock(player, location);
		} else {
			return true;
		}
	}

	public double getCreationPrice() {
		return creationPrice + (increaseFactor * cities.size());
	}

	public void removeCity(City city) {
		File file = new File(rpMachine.getDataFolder().getPath() + "/cities/" + city.getFileName());
		file.delete();
		cities.remove(city.getCityName());
	}
}
