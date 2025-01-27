package com.gmail.JyckoSianjaya.DonateCraft.Data;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.gmail.JyckoSianjaya.DonateCraft.Database.SimpleSQL;
import com.gmail.JyckoSianjaya.DonateCraft.Main.DonateCraft;
import com.gmail.JyckoSianjaya.DonateCraft.Manager.DCRunnable;
import com.gmail.JyckoSianjaya.DonateCraft.Manager.DCTask;
import com.gmail.JyckoSianjaya.DonateCraft.Objects.ACWallet;
import com.gmail.JyckoSianjaya.DonateCraft.Objects.Cash;
import com.gmail.JyckoSianjaya.DonateCraft.Utils.UUIDCacher;
import com.gmail.JyckoSianjaya.DonateCraft.Utils.Utility;

public final class PlayerData {
	private static PlayerData instance;
	private final DonateCraft MainInst = DonateCraft.getInstance();
	private final CashBank cbank = CashBank.getInstance();
	private final ACCashBank acbank = ACCashBank.getInstance();
	private PlayerData() {
	}
	public final static PlayerData getInstance() {
		if (instance == null) {
			instance = new PlayerData();
		}
		return instance;
	}
	public final void setData(final Player p, final Cash cash) {
		if (DataStorage.getInstance().useSQL()) return;
		DCRunnable.getInstance().addTask(new DCTask(){
			int health = 1;
			@Override
			public void runTask() {
		final File f = new File(MainInst.getDataFolder(), "playerdata" + File.separator +  p.getUniqueId() + ".yml");
		final YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
		String nick = "";
		file.set("cash", cash.getCashAmount());
		file.set("uuid", p.getUniqueId().toString());
		file.set("nick", p.getName());
	    ACWallet accw = new ACWallet(0);
		if (acbank.getACWallet(p) != null) {
			accw = acbank.getACWallet(p);
		}
		if (accw.getAmount() <=0 && cash.getCashAmount() <= 0) return;
		file.set("ac-cash", accw.getAmount());
		try {
		if (!f.exists()) {
			f.createNewFile();
		}
		file.save(f);
		} catch (IOException e) {
		}
			}

			@Override
			public void reduceTicks() {
				// TODO Auto-generated method stub
				health--;
			}

			@Override
			public int getLiveTicks() {
				// TODO Auto-generated method stub
				return health;
			}
	});
	}
	public final YamlConfiguration getData(final UUID p) {
		File f = new File(MainInst.getDataFolder(), "playerdata" + File.separator +  p + ".yml");
		if (!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		f = new File(MainInst.getDataFolder(), "playerdata" + File.separator +  p + ".yml");
		final YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
		Cash cash = new Cash(0);
		if (CashBank.getInstance().getCash(p) != null) {
			cash = CashBank.getInstance().getCash(p);
		}
		file.set("cash", cash.getCashAmount());
		file.set("uuid", p.toString());
		String name = null;
		if (Bukkit.getPlayer(p) != null) {
			name = Bukkit.getPlayer(p).getName();
		}
		if (Bukkit.getOfflinePlayer(p) != null) {
			name = Bukkit.getOfflinePlayer(p).getName();
		}
		if (name == null) {
			name = UUIDCacher.getInstance().getNick(p);
		}
		file.set("nick", name);
	    ACWallet accw = new ACWallet(0);
		if (acbank.getACWallet(p) != null) {
			accw = acbank.getACWallet(p);
		}
		if (accw.getAmount() <=0 && cash.getCashAmount() <= 0) return null;
		file.set("ac-cash", accw.getAmount());
		f.delete();
		return file;
	}
	public final void loadData(final YamlConfiguration yml) {
		Cash cash = new Cash(0);
		UUID p = UUID.fromString(yml.getString("uuid"));
		if (CashBank.getInstance().getCash(p) != null) {
			cash = CashBank.getInstance().getCash(p);
		}
		cash.setCash(yml.getInt("cash"));

	    ACWallet accw = new ACWallet(0);
		if (acbank.getACWallet(p) != null) {
			accw = acbank.getACWallet(p);
		}
		accw.setAmount(yml.getInt("ac-cash"));
		CashBank.getInstance().setCash(p, cash);
		ACCashBank.getInstance().setACWallet(p, accw);
	}
	public final ACWallet getSQLACCash(final UUID p) {
		final YamlConfiguration yml = this.getSQLData(p);
		int cash = yml.getInt("ac-cash");
		ACWallet wallet = new ACWallet(cash);
		ACCashBank.getInstance().setACWallet(p, wallet);
		return wallet;
	}
	public final Cash getSQLCash(final UUID p) {
		final YamlConfiguration yml = this.getSQLData(p);
		int cash = yml.getInt("cash");
		Cash cas = new Cash(cash);
		CashBank.getInstance().setCash(p, cas);
		return cas;
	}
	public final YamlConfiguration getSQLData(final UUID p) {
		File f = new File(DonateCraft.getInstance().getDataFolder(), "DummyData.yml");
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
		yml.set("uuid", p.toString());
		if (!SimpleSQL.getInstance().hasRecord(p.toString())) {
			String todo = "INSERT INTO DCPlayerData (uuid, data) VALUES (\"" + p.toString() + "\"," + "\"" + yml.saveToString() + "\")";
			SimpleSQL.getInstance().getUpdate(todo);
			return yml;
		}
		try {
			ResultSet result = SimpleSQL.getInstance().getResult("SELECT * FROM DCPlayerData WHERE uuid='" + p.toString() + "'");
			while (result.next()) {
				yml.loadFromString(result.getString("data"));
			}
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return yml;
	}
	public final void saveData(final YamlConfiguration yml) {
		Cash cash = new Cash(0);
		UUID p = UUID.fromString(yml.getString("uuid"));
		if (CashBank.getInstance().getCash(p) != null) {
			cash = CashBank.getInstance().getCash(p);
			CashBank.getInstance().removeCash(p);
		}
		yml.set("cash", cash.getCashAmount());
		yml.set("uuid", p.toString());
		String name = null;
		if (Bukkit.getPlayer(p) != null) {
			name = Bukkit.getPlayer(p).getName();
		}
		if (Bukkit.getOfflinePlayer(p) != null) {
			name = Bukkit.getOfflinePlayer(p).getName();
		}
		if (name == null) {
			name = UUIDCacher.getInstance().getNick(p);
		}
		yml.set("nick", name);
	    ACWallet accw = new ACWallet(0);
		if (acbank.getACWallet(p) != null) {
			accw = acbank.getACWallet(p);
		}
		if (accw.getAmount() <=0 && cash.getCashAmount() <= 0) return;
		yml.set("ac-cash", accw.getAmount());
		if (!SimpleSQL.getInstance().hasRecord(p.toString())) {
			SimpleSQL.getInstance().getUpdate("INSERT INTO DCPlayerData (uuid, data) VALUES ('" + p.toString() + "', '" + yml.saveToString() + "')");
		}
		SimpleSQL.getInstance().getUpdate("UPDATE DCPlayerData SET data='" + yml.saveToString() + "' WHERE uuid='" + p.toString() +"'");
	}
	public final void setData(final UUID p) {
		if (DataStorage.getInstance().useSQL()) return;
		final File f = new File(MainInst.getDataFolder(), "playerdata" + File.separator +  p + ".yml");
		if (!f.exists()) {
			try {
			f.createNewFile();
			} catch (IOException e) {

			}
		}
		final YamlConfiguration file = YamlConfiguration.loadConfiguration(f);
		Cash cash = new Cash(0);
		if (CashBank.getInstance().getCash(p) != null) {
			cash = CashBank.getInstance().getCash(p);
			CashBank.getInstance().removeCash(p);
		}
		file.set("cash", cash.getCashAmount());
		file.set("uuid", p.toString());
		String name = null;
		if (Bukkit.getPlayer(p) != null) {
			name = Bukkit.getPlayer(p).getName();
		}
		if (Bukkit.getOfflinePlayer(p) != null) {
			name = Bukkit.getOfflinePlayer(p).getName();
		}
		if (name == null) {
			name = UUIDCacher.getInstance().getNick(p);
		}
		file.set("nick", name);
	    ACWallet accw = new ACWallet(0);
		if (acbank.getACWallet(p) != null) {
			accw = acbank.getACWallet(p);
			acbank.removeACWallet(p);
		}
		if (accw.getAmount() <=0 && cash.getCashAmount() <= 0) return;
		file.set("ac-cash", accw.getAmount());
		try {
			file.save(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public final ACWallet getACCash(final UUID uuid) {
		if (DataStorage.getInstance().useSQL()) return null;

		final UUID uu = uuid;
		final File pfile = new File(MainInst.getDataFolder(), "playerdata" + File.separator + uu + ".yml");
		final YamlConfiguration pyml = YamlConfiguration.loadConfiguration(pfile);
		if (!pfile.exists()) {
			return null;
			
			try {
			pfile.createNewFile();
			final YamlConfiguration writer = YamlConfiguration.loadConfiguration(pfile);
			writer.set("cash", 0);
			writer.set("ac-cash", 0);
			writer.set("uuid", uu.toString());
			writer.save(pfile);
			} catch (final IOException e) {
				Utility.sendConsole("[DC] Couldn't save file for Player: " + p.getName());
			}
			
		}
		ACWallet cash = null;
		if (acbank.getACWallet(uu) == null) {
			final int acc = pyml.getInt("ac-cash");
			cash = new ACWallet(acc);
			acbank.setACWallet(uu, cash);
		}
		return cash;
	}

	public final Cash getCash(final UUID uuid) {
		if (DataStorage.getInstance().useSQL()) return null;
		Cash cash2 = null;
		final UUID uu = uuid;
		final File pfile = new File(MainInst.getDataFolder(), "playerdata" + File.separator + uu.toString() + ".yml");
		final YamlConfiguration pyml = YamlConfiguration.loadConfiguration(pfile);
		if (!pfile.exists()) {
			return null;
			
			try {
			pfile.createNewFile();
			final YamlConfiguration writer = YamlConfiguration.loadConfiguration(pfile);
			writer.set("cash", 0);
			writer.set("ac-cash", 0);
			writer.set("uuid", uu.toString());
			writer.save(pfile);
			} catch (final IOException e) {
				Utility.sendConsole("[DC] Couldn't save file for Player: " + p.getName());
			}
			
		}
		if (cbank.getOriginalCash(uu) == null) {
			final int cash = pyml.getInt("cash");
			cash2 = new Cash(cash);
			cbank.setCash(uuid, cash2);
		}
		if (acbank.getACWallet(uu) == null) {
			final int acc = pyml.getInt("ac-cash");
			acbank.setACWallet(uu, new ACWallet(acc));
		}
		return cash2;
	}
	public final Cash getCash(final Player p) {
		if (DataStorage.getInstance().useSQL()) return null;

		Cash cash2 = null;
		final UUID uu = p.getUniqueId();
		final File pfile = new File(MainInst.getDataFolder(), "playerdata" + File.separator + uu.toString() + ".yml");
		final YamlConfiguration pyml = YamlConfiguration.loadConfiguration(pfile);
		if (!pfile.exists()) {
			return null;
			
			try {
			pfile.createNewFile();
			final YamlConfiguration writer = YamlConfiguration.loadConfiguration(pfile);
			writer.set("cash", 0);
			writer.set("ac-cash", 0);
			writer.set("uuid", uu.toString());
			writer.save(pfile);
			} catch (final IOException e) {
				Utility.sendConsole("[DC] Couldn't save file for Player: " + p.getName());
			}
			
		}
		if (cbank.getOriginalCash(uu) == null) {
			final int cash = pyml.getInt("cash");
			cash2 = new Cash(cash);
			cbank.setCash(p, cash2);
		}
		if (acbank.getACWallet(uu) == null) {
			final int acc = pyml.getInt("ac-cash");
			acbank.setACWallet(uu, new ACWallet(acc));
		}
		cash2 = cbank.getCash(p);
		return cash2;
	}
}
