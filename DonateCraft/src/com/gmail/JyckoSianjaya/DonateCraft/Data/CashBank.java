package com.gmail.JyckoSianjaya.DonateCraft.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.JyckoSianjaya.DonateCraft.Main.DonateCraft;
import com.gmail.JyckoSianjaya.DonateCraft.Objects.Cash;
import com.gmail.JyckoSianjaya.DonateCraft.Utils.Utility;

public class CashBank {
	private static CashBank instance;
	private final HashMap<UUID, Cash> Cashes = new HashMap<UUID, Cash>();
	private final HashMap<Integer, String> cachecash = new HashMap<Integer, String>();
	private final ArrayList<String> TOP_CASH_BALANCES = new ArrayList<String>();
	private CashBank() {
		LoadFiles();
		resetCashBalance();
		new BukkitRunnable() {
			@Override
			public void run() {
				synchronized(this) {
				resetCashTopBalance();
				}
			}
		}.runTaskTimerAsynchronously(DonateCraft.getInstance(), 3600L, 3600L);
	}
	public final static CashBank getInstance() {
		if (instance == null) {
			instance = new CashBank();
		}
		return instance;
	}
	public final  Boolean hasCash(final Player p) {
		return (Cashes.containsKey(p.getUniqueId()));
	}
	public final Cash getCash(final Player p) {
		return Cashes.get(p.getUniqueId());
	}
	public final Cash getCash(final UUID uuid) {
		return Cashes.get(uuid);
	}
	public final void setCash(final Player p, final Cash cash) {
		Cashes.put(p.getUniqueId(), cash);
	}
	public final void setCash(final UUID uuid, final Cash cash) {
		Cashes.put(uuid, cash);
	}
	public final void LoadFiles() {
		final File folder = new File(DonateCraft.getInstance().getDataFolder(), "playerdata" + File.separator);
		final File[] cashfiles = folder.listFiles();
		for (final File file : cashfiles) {
			final String name = file.getName().replaceAll(".yml", "");
			final UUID uuid = UUID.fromString(name);
			final YamlConfiguration cashs = YamlConfiguration.loadConfiguration(file);
			final int cash = cashs.getInt("cash");
			final Cash cas = new Cash(cash);
			setCash(uuid, cas);
		}
	}
	public final void resetCashTopBalance() {
		for (final Player p : Bukkit.getOnlinePlayers()) {
			if (getCash(p) == null) {
				continue;
			}
			if (getCash(p).getCashAmount() <= 0) {
				continue;
			}
		}
		TOP_CASH_BALANCES.clear();
		final File folder = new File(DonateCraft.getInstance().getDataFolder(), "playerdata");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		final ArrayList<Integer> caches = new ArrayList<Integer>();
		for (final File ss : folder.listFiles()) {
			final YamlConfiguration yml = YamlConfiguration.loadConfiguration(ss);
			final UUID uuid = UUID.fromString(yml.getString("uuid"));
			final int acs = yml.getInt("cash");
			final String owner = Bukkit.getOfflinePlayer(uuid).getName();
			caches.add(acs);
		}
		Collections.sort(caches);
		Collections.reverse(caches);
		for (final int ie : caches) {
			addTop(ie);
		}
	}
	public final void resetCashBalance() {
		for (final Player p : Bukkit.getOnlinePlayers()) {
			if (getCash(p) == null) {
				continue;
			}
			if (getCash(p).getCashAmount() <= 0) {
				continue;
			}
		}
		cachecash.clear();
		TOP_CASH_BALANCES.clear();
		final File folder = new File(DonateCraft.getInstance().getDataFolder(), "playerdata");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		final ArrayList<Integer> caches = new ArrayList<Integer>();
		for (final File ss : folder.listFiles()) {
			final YamlConfiguration yml = YamlConfiguration.loadConfiguration(ss);
			final UUID uuid = UUID.fromString(yml.getString("uuid"));
			final int acs = yml.getInt("cash");
			final String owner = Bukkit.getOfflinePlayer(uuid).getName();
			addCache(owner, acs);
			caches.add(acs);
		}
		Collections.sort(caches);
		Collections.reverse(caches);
		for (int ie : caches) {
			addTop(ie);
		}
	}
	private final void addTop(int key) {
		this.TOP_CASH_BALANCES.add(cachecash.get(key) + "~" + key);
	}
	private final void addCache(String u, int amount) {
		this.cachecash.put(amount, u);
	}
	public final  ArrayList<String> getTopBalance() {
		return this.TOP_CASH_BALANCES;
	}
	public final String getTopBalances(int index) {
		return this.TOP_CASH_BALANCES.get(index);
	}
}
