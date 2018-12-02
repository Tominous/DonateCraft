package com.gmail.JyckoSianjaya.DonateCraft.Commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.JyckoSianjaya.DonateCraft.Data.ACCashBank;
import com.gmail.JyckoSianjaya.DonateCraft.Data.CashBank;
import com.gmail.JyckoSianjaya.DonateCraft.Data.LangStorage;
import com.gmail.JyckoSianjaya.DonateCraft.Data.LangStorage.LongMessage;
import com.gmail.JyckoSianjaya.DonateCraft.Data.LangStorage.Message;
import com.gmail.JyckoSianjaya.DonateCraft.Objects.ACWallet;
import com.gmail.JyckoSianjaya.DonateCraft.Objects.Cash;
import com.gmail.JyckoSianjaya.DonateCraft.Utils.Utility;

public class CashCmdHandler {
	private static CashCmdHandler instance;
	private LangStorage lang = LangStorage.getInstance();
	private CashBank cashb = CashBank.getInstance();
	private CashCmdHandler() {
	}
	public static CashCmdHandler getInstance() {
		if (instance == null) {
			instance = new CashCmdHandler();
		}
		return instance;
	}
	public void ManageCashCommand(CommandSender sender, Command cmd, String[] args) {
		int length = args.length;
		if (length < 1) {
			if (!(sender instanceof Player)) {
				Utility.sendMsg(sender, "&cOnly players!");
				return;
			}
			Player p = (Player) sender;
			for (String str : lang.getMessage(LongMessage.SHOW_CASH)) {
				if (str.contains("%p")) {
					str = str.replaceAll("%p", p.getName());
				}
				if (str.contains("%CASH%")) {
					Cash cash = null;
					if (cashb.hasCash(p)) {
					cash = cashb.getCash(p.getUniqueId());
					}
					int money = 0;
					if (cash != null) {
						money = cash.getCashAmount();
					}
					str = str.replaceAll("%CASH%", "" + money);

				}
				Utility.sendMsg(sender, str);
			}
			if (p.hasPermission("cash.admin")) {
				Utility.sendMsg(sender, "&c&lAdmin Notes: &7You can use /cash &fset&7/&fadd&7/&fremove &7<Player> <Amount>");
			}
			return;
		}
		switch (args[0].toString().toLowerCase()) {
		case "top":
		case "tops":
		case "leaderboard":
			if (!sender.hasPermission("cash.top")) {
				Utility.sendMsg(sender, lang.getMessage(Message.NOPERM));
				return;
			}
			if (length < 2) {
				ArrayList<String> arrays = cashb.getTopBalance();
				for (String str : lang.getMessage(LongMessage.CTOP_OPENING)) {
					if (str.contains("%r")) {
						str = str.replaceAll("%r", "1");
					}
					if (str.contains("%m")) {
						str = str.replaceAll("%m", "10");
					}
					Utility.sendMsg(sender, str);
				}
				int aalength = arrays.size();
				if (aalength < 10) {
					for (int i = 0; i < aalength; i++) {
						try {
							arrays.get(i);
						} catch (IndexOutOfBoundsException e) {
							break;
						}
						String thebal = arrays.get(i);
						String[] info = thebal.split("~");
						String msg = lang.getMessage(Message.CTOP_FORMAT).replaceAll("%RANK%", "" + (i + 1)).replaceAll("%p", info[0]).replaceAll("%cash%", info[1]);
						Utility.sendMsg(sender, msg);
					}
				}
				else {
					for (int i = 0; i < 9; i++) {
						try {
							arrays.get(i);
						} catch (IndexOutOfBoundsException e) {
							break;
						}
					String thebal = arrays.get(i);
					String[] info = thebal.split("~");
					String msg = lang.getMessage(Message.CTOP_FORMAT).replaceAll("%RANK%", "" + (i + 1)).replaceAll("%p", info[0]).replaceAll("%cash%", info[1]);
					Utility.sendMsg(sender, msg);
					}
				}
				for (String str : lang.getMessage(LongMessage.CTOP_CLOSING)) {
					if (str.contains("%r")) {
						str = str.replaceAll("%r", "1");
					}
					if (str.contains("%m")) {
						str = str.replaceAll("%m", "10");
					}
					Utility.sendMsg(sender, str);
				}
				return;
			}
			if (length < 3) {
				int max = 0;
				try { 
					max = Integer.parseInt(args[1]);
				} catch (NumberFormatException lakad) {
					for (String str : lang.getMessage(LongMessage.NOT_NUMBER)) Utility.sendMsg(sender, str);
					return;
				}
				ArrayList<String> cashs = cashb.getTopBalance();
				if (max > cashs.size()) max = cashs.size();
				if (max < 10) max = 10;
				for (String str : lang.getMessage(LongMessage.CTOP_OPENING)) {
					if (str.contains("%r")) {
						str = str.replaceAll("%r", "" + (max - 9));
					}
					if (str.contains("%m")) {
						str = str.replaceAll("%m", "" + max);
					}
					Utility.sendMsg(sender, str);
				}

				for (int i = max - 10 ; i < max - 1; i++) {
					try {
						cashs.get(i);
					} catch (IndexOutOfBoundsException e) {
						break;
					}
					String thebal = cashs.get(i);
					String[] info = thebal.split("~");
					String msg = lang.getMessage(Message.CTOP_FORMAT).replaceAll("%RANK%", "" + (i + 1)).replaceAll("%p", info[0]).replaceAll("%cash%", info[1]);
					Utility.sendMsg(sender, msg);
				}
				for (String str : lang.getMessage(LongMessage.CTOP_CLOSING)) {
					if (str.contains("%r")) {
						str = str.replaceAll("%r", "" + (max - 9));
					}
					if (str.contains("%m")) {
						str = str.replaceAll("%m", "" + max);
					}
					Utility.sendMsg(sender, str);
				}
				return;
			}
		case "set":
		case "put":
			if (!sender.hasPermission("cash.admin")) {
				return;
			}
			if (length < 3) {
				Utility.sendMsg(sender, "&a&l| &7/cash set <Player> <Amount>");
				return;
			}
			Player p = Bukkit.getPlayer(args[1]);
			if (p == null) {
				Utility.sendMsg(sender, "&c&l| &7That person doesn't exist!");
				return;
			}
			int cashamount = 0;
			try {
			cashamount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				Utility.sendMsg(sender, "&c&l| &7That's not a number!");
				return;
			}
			UUID uuid = p.getUniqueId();
			Cash cash = cashb.getCash(uuid);
			if (cash == null) {
			cashb.setCash(p.getUniqueId(), new Cash(cashamount));
			Utility.sendMsg(sender, "&2&l| &7Successfuly changed the Cash amount for &f" + p.getName() + "&7 into &f" + cashamount);
			return;
			}
			cash.setCash(cashamount);
			Utility.sendMsg(sender, "&a&l| &7Successfuly changed the Cash amount for &f" + p.getName() + "&7 into &f" + cashamount);
			return;
		case "add":
		case "increase":
			if (!sender.hasPermission("cash.admin")) {
				return;
			}
			if (length < 3) {
				Utility.sendMsg(sender, "&a&l| &7/cash add <Player> <Amount>");
				return;
			}
			Player ap = Bukkit.getPlayer(args[1]);
			if (ap == null) {
				Utility.sendMsg(sender, "&c&l| &7That person doesn't exist!");
				return;
			}
			int addcashamount = 0;
			try {
			addcashamount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				Utility.sendMsg(sender, "&c&l| &7That's not a number!");
				return;
			}
			ACCashBank acb = ACCashBank.getInstance();
			ACWallet wallet = acb.getACWallet(ap.getUniqueId());
			if (wallet == null) {
				acb.setACWallet(ap.getUniqueId(), new ACWallet(0));
				wallet = acb.getACWallet(ap);
			}
			wallet.addAmount(addcashamount);
			UUID auuid = ap.getUniqueId();
			Cash acash = cashb.getCash(auuid);
			if (acash == null) {
			cashb.setCash(auuid, new Cash(addcashamount));
			Utility.sendMsg(sender, "&2&l| &7Successfuly &aincreased&7 the Cash amount for &f" + ap.getName() + "&7 into &f" + addcashamount);
			return;
			}
			int currcash = acash.getCashAmount();
			acash.setCash(addcashamount+currcash);
			Utility.sendMsg(sender, "&a&l| &7Successfuly &aincreased&7 the Cash amount for &f" + ap.getName() + "&7 into &f" + (addcashamount+currcash));
			return;
		case "remove":
		case "decrease":
		case "take":
			if (!sender.hasPermission("cash.admin")) {
				return;
			}
			if (length < 3) {
				Utility.sendMsg(sender, "&a&l| &7/cash take <Player> <Amount>");
				return;
			}
			Player rp = Bukkit.getPlayer(args[1]);
			if (rp == null) {
				Utility.sendMsg(sender, "&c&l| &7That person doesn't exist!");
				return;
			}
			int rcashamount = 0;
			try {
			rcashamount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				Utility.sendMsg(sender, "&c&l| &7That's not a number!");
				return;
			}
			UUID ruuid = rp.getUniqueId();
			Cash rcash = cashb.getCash(ruuid);
			if (rcash == null) {
			cashb.setCash(ruuid, new Cash(0));
			Utility.sendMsg(sender, "&2&l| &7Successfuly &cdecreased&7 the Cash amount for &f" + rp.getName() + "&7 into &f" + 0);
			return;
			}
			int currdcash = rcash.getCashAmount();
			int fina = currdcash - rcashamount;
			if (fina < 0) {
				fina = 0;
			}
			rcash.setCash(fina);
			Utility.sendMsg(sender, "&a&l| &7Successfuly &cdecreased&7 the Cash amount for &f" + rp.getName() + "&7 into &f" + fina);
			return;
		default:
			Player play = Bukkit.getPlayer(args[0]);
			if (play == null) {
				Utility.sendMsg(sender, "&c&l| &7That person doesn't exist!");
				return;
			}
			Cash cashe = null;
			if (cashb.hasCash(play)) {
			cashe = cashb.getCash(play);
			}
			int money = 0;
			if (cashe != null) {
				money = cashe.getCashAmount();
			}
			for (String str : lang.getMessage(LongMessage.SHOW_CASH_OTHERS)) {
				if (str.contains("%p")) {
					str = str.replaceAll("%p", play.getName());
				}
				if (str.contains("%CASH%")) {
					str = str.replaceAll("%CASH%", "" + money);
				}
				Utility.sendMsg(sender, str);
			}
			return;
		}
	}
}