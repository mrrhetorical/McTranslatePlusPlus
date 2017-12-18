package com.rhetorical.tpp;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class Main extends JavaPlugin implements Listener {

	public static String prefix = "#";
	public String version = "1.0";

	public static ConsoleCommandSender cs = Bukkit.getServer().getConsoleSender();
	public FileConfiguration config;

	public McLang consoleLang = McLang.EN;

	public HashMap<Player, McLang> langMap = new HashMap<Player, McLang>();

	@Override
	public void onEnable() {

		cs.sendMessage(prefix + "§aMcTranslate++ is up and running on version " + version + "!");

		config = getPlugin().getConfig();

		loadLangMap();

		consoleLang = McLang.valueOf(getPlugin().getConfig().getString("Console.lang"));

		Bukkit.getServer().getPluginManager().registerEvents(this, getPlugin());

		getPlugin().saveDefaultConfig();
		getPlugin().reloadConfig();
	}

	@Override
	public void onDisable() {

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {

			if (label.equalsIgnoreCase("translate")) {

				cs.sendMessage(prefix
						+ "§cYou must be a player to use other commands than '/ctranslate [EN, ES, FR, RU, ...]'");
				return true;

			} else if (label.equalsIgnoreCase("ctranslate")) {
				if (args.length >= 1) {
					McLang currentLang = consoleLang;

					String decodedLang = args[0];
					McLang lang;

					try {
						lang = McLang.valueOf(decodedLang.toUpperCase());
					} catch (Exception e) {
						try {
							cs.sendMessage(prefix + translate(
									"§cThat's not a valid language! Please reference the plugin docs for available languages and their abbreviations.",
									McLang.EN, currentLang));
							return true;
						} catch (Exception e1) {
							cs.sendMessage(prefix
									+ "§cThat's not a valid language! Please reference the plugin docs for available languages and their abbreviations.");
							return true;
						}
					}

					getPlugin().getConfig().set("Console.lang", lang.toString());

					getPlugin().saveConfig();
					getPlugin().reloadConfig();

					try {
						cs.sendMessage(prefix + translate("Successfully set the console language!", McLang.EN, lang));
						return true;
					} catch (Exception e) {
						return true;
					}

				}
			}
			return true;
		}

		if (label.equalsIgnoreCase("translate")) {

			Player p = (Player) sender;

			if (p.hasPermission("translate.changeLanguage") || p.hasPermission("translate.*") || p.isOp()) {
				if (args.length != 0) {

					String decodedLang = args[0];
					McLang lang;

					try {
						lang = McLang.valueOf(decodedLang.toUpperCase());
					} catch (Exception e) {
						try {
							p.sendMessage(prefix + translate(
									"§cThat's not a valid language! Please ask an adminiostrator for available languages and their abbreviations.",
									McLang.EN, langMap.get(p)));
							return true;
						} catch (Exception e1) {
							p.sendMessage(prefix
									+ "§cThat's not a valid language! Please ask an adminiostrator for available languages and their abbreviations.");
							return true;
						}
					}

					langMap.put(p, lang);
					saveLangMapToConfig();
				}
			}

		}

		return false;
	}

	// Listeners

	@EventHandler
	private void onPlayerChat(AsyncPlayerChatEvent e) {

		Player sender = e.getPlayer();

		String originalMessage = e.getMessage();

		e.setCancelled(true);

		if (originalMessage.startsWith(".>")) {
			e.setCancelled(false);
		}

		McLang senderLang = getLang(sender);

		for (Player p : Bukkit.getOnlinePlayers()) {

			McLang recieverLang = getLang(p);

			String translatedMessage = "";

			try {

				translatedMessage = translate(originalMessage, senderLang, recieverLang);

			} catch (Exception exeption) {

				translatedMessage = originalMessage;
			}

			p.sendMessage(translatedMessage);
		}

	}

	// Function

	public void loadLangMap() {

		langMap.clear();

		for (int k = 0; getPlugin().getConfig().contains("Players." + k); k++) {
			OfflinePlayer op = (OfflinePlayer) getPlugin().getConfig().get("Players." + k + ".profile");
			
			Player p = Bukkit.getPlayer(op.getUniqueId());
			
			String mcLangUndecoded = getPlugin().getConfig().getString("Players." + k + ".lang");

			McLang lang = McLang.valueOf(mcLangUndecoded);

			langMap.put(p, lang);
		}

	}

	public void saveLangMapToConfig() {

		playerLoop: for (Player p : langMap.keySet()) {
			int k = 0;
			while (getPlugin().getConfig().contains("Players." + k)) {
				Player pp = (Player) getPlugin().getConfig().get("Players." + k + ".profile");
				if (p.equals(pp)) {
					getPlugin().getConfig().set("Players." + k + ".lang", langMap.get(p).toString());
					continue playerLoop;
				}

				k++;
			}

			getPlugin().getConfig().set("Players." + k + ".profile", p);
			getPlugin().getConfig().set("Players." + k + ".lang", langMap.get(p).toString());
		}

		getPlugin().saveConfig();
		getPlugin().reloadConfig();
	}

	private Plugin getPlugin() {
		return Bukkit.getServer().getPluginManager().getPlugin("McTranslatePlusPlus");
	}

	public McLang getLang(Player p) {

		if (!langMap.containsKey(p)) {
			p.sendMessage(prefix + "§7Use '/translate [EN, ES, FR, RU, ...]' to set your language!");
			langMap.put(p, McLang.EN);
		}

		return langMap.get(p);
	}

	public static String translate(String text, McLang source, McLang target) throws Exception {

		Translate translate = TranslateOptions.getDefaultInstance().getService();

		Translation translation = translate.translate(text, TranslateOption.sourceLanguage(source.toString()),
				TranslateOption.targetLanguage(target.toString()));

		return translation.getTranslatedText();
	}
}
