package net.zyuiop.rpmachine.economy;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.LandOwner;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.projects.Project;
import org.bukkit.ChatColor;

import java.util.UUID;

/**
 * @author zyuiop
 */
public class TaxPayerToken {
	private UUID playerUuid;
	private boolean admin;
	private String cityName;
	private String companyName;
	private String projectName;

	public TaxPayerToken() {

	}

	public TaxPayer getTaxPayer() {
		if (isAdmin()) {
			return AdminAccountHolder.INSTANCE;
		} else if (playerUuid != null) {
			return RPMachine.database().getPlayerData(playerUuid);
		} else if (cityName != null) {
			return RPMachine.getInstance().getCitiesManager().getCity(cityName);
		} else if (projectName != null) {
			return RPMachine.getInstance().getProjectsManager().getZone(projectName);
		} else {
			return null;
		}
	}

	public String displayable() {
		if (isAdmin()) {
			return ChatColor.RED + "La Confédération";
		} else if (playerUuid != null) {
			return ChatColor.YELLOW + "(Joueur) " + ChatColor.GOLD + RPMachine.database().getUUIDTranslator().getName(playerUuid);
		} else if (cityName != null) {
			City city = RPMachine.getInstance().getCitiesManager().getCity(cityName);
			if (city == null) {
				return ChatColor.RED + "Ville supprimée";
			} else {
				return ChatColor.AQUA + "(Ville) " + ChatColor.DARK_AQUA + city.getCityName();
			}
		} else if (projectName != null) {
			Project project = RPMachine.getInstance().getProjectsManager().getZone(projectName);
			if (project == null) {
				return ChatColor.RED + "Projet supprimé";
			} else {
				return ChatColor.GREEN + "(Projet) " + ChatColor.DARK_GREEN + project.getPlotName();
			}
		}

		return null;
	}

	public String shortDisplayable() {
		if (isAdmin()) {
			return ChatColor.RED + "Confédération";
		} else if (playerUuid != null) {
			return ChatColor.GOLD + RPMachine.database().getUUIDTranslator().getName(playerUuid);
		} else if (cityName != null) {
			City city = RPMachine.getInstance().getCitiesManager().getCity(cityName);
			if (city == null) {
				return ChatColor.RED + "Ville";
			} else {
				return ChatColor.DARK_AQUA + city.getCityName();
			}
		} else if (projectName != null) {
			Project project = RPMachine.getInstance().getProjectsManager().getZone(projectName);
			if (project == null) {
				return ChatColor.RED + "Projet";
			} else {
				return ChatColor.DARK_GREEN + project.getPlotName();
			}
		}

		return null;
	}

	public LandOwner getLandOwner() {
		TaxPayer tp = getTaxPayer();
		return tp != null && tp instanceof LandOwner ? (LandOwner) tp : null;
	}

	public ShopOwner getShopOwner() {
		TaxPayer tp = getTaxPayer();
		return tp != null && tp instanceof ShopOwner ? (ShopOwner) tp : null;
	}

	public UUID getPlayerUuid() {
		return playerUuid;
	}

	public void setPlayerUuid(UUID playerUuid) {
		this.playerUuid = playerUuid;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public static TaxPayerToken fromPayer(TaxPayer owner) {
		TaxPayerToken tp = new TaxPayerToken();
		if (owner instanceof PlayerData)
			tp.setPlayerUuid(((PlayerData) owner).getUuid());
		else if (owner instanceof AdminAccountHolder)
			tp.setAdmin(true);
		else if (owner instanceof City)
			tp.setCityName(((City) owner).getCityName());
		else if (owner instanceof Project)
			tp.setProjectName(((Project) owner).getPlotName());

		return tp;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TaxPayerToken)) return false;

		TaxPayerToken token = (TaxPayerToken) o;

		if (admin != token.admin) return false;
		if (playerUuid != null ? !playerUuid.equals(token.playerUuid) : token.playerUuid != null) return false;
		if (cityName != null ? !cityName.equals(token.cityName) : token.cityName != null) return false;
		return companyName != null ? companyName.equals(token.companyName) : token.companyName == null;
	}

	@Override
	public int hashCode() {
		int result = playerUuid != null ? playerUuid.hashCode() : 0;
		result = 31 * result + (admin ? 1 : 0);
		result = 31 * result + (cityName != null ? cityName.hashCode() : 0);
		result = 31 * result + (companyName != null ? companyName.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "TaxPayerToken{" +
				"playerUuid=" + playerUuid +
				", admin=" + admin +
				", cityName='" + cityName + '\'' +
				", companyName='" + companyName + '\'' +
				'}';
	}
}
