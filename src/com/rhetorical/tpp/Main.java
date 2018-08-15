package com.rhetorical.tpp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
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

	/*
	 * This plugin is authored by Mr_Rhetorical.
	 * 
	 * 
	 * License And Agreement By downloading or using the plugin, you agree to the following, and hereby assume responsibility for any actions taken by you, or anyone who has access to the plugin. 1. Do not modify, edit, change, or alter this plugin's code* 2. Do not redistribute or claim this plugin as your work* 3. Do not use or copy this plugin's code as your own* 4. Do not decompile the plugin* 5. We, the creators of McTranslate++ will not refund any person(s) who have purchased the plugin under any circumstances 6. We, the creators of McTranslate++ have the right to change this agreement at any time. 7. We, the creators of McTranslate++ reserve the right to pursue any person(s) with legal action if they fail to adhere to this agreement 8. We, the creators of McTranslate++ have the right to revoke any person(s) access to the plugin * Unless otherwise instructed by a plugin moderator Updated 12/23/17
	 * 
	 * This is the final warning for anyone decompiling the plugin.
	 * 
	 * For Spigot Employees -------------------- To install proper dependencies for plugin to work:
	 * 
	 * 1. Download dependencies from: 'https://www.dropbox.com/s/6q8snsy6yrdjt25/mctranslateplusplus_lib.zip?dl=0'
	 * 
	 * 2. Place the dependencies in the folder '.../plugins/mctranslateplusplus_lib' for the plugin to work properly.
	 * 
	 */

	public static void main(String[] args) {
		// Java Application start (Ignore this, this is so I can use the 'Google
		// Translate API' on the server and run the plugin as a java
		// application)
	}

	public static String prefix = "#";
	private String version = "1.0";

	public static ConsoleCommandSender cs;
	public FileConfiguration config;

	private static Translate translateWithCredentials;

	private String googleApiKey;

	private static String mcTranslateApiKey;

	private boolean useAsJustApi;

	private McLang consoleLang = McLang.EN;

	public static HashMap<String, McLang> langMap = new HashMap<String, McLang>(); // UUiD,
	// Language

	@Override
	public void onEnable() {

		getPlugin().saveDefaultConfig();
		getPlugin().reloadConfig();

		cs = Bukkit.getServer().getConsoleSender();

		cs.sendMessage(prefix + "§aMcTranslate++ is up and running on version " + version + "!");

		loadLangMap();

		consoleLang = McLang.valueOf(getPlugin().getConfig().getString("Console.lang"));
		googleApiKey = getPlugin().getConfig().getString("Google.api_key");

		Bukkit.getServer().getPluginManager().registerEvents(this, getPlugin());

		try {
			getTranslationService(googleApiKey);
			cs.sendMessage(prefix + "§aSuccessfully connected to google translate service's API!");
		} catch (Exception e) {
			cs.sendMessage(prefix + "§cUnable to connect to the googleApiKey");
		}

		getPlugin().saveDefaultConfig();
		getPlugin().reloadConfig();

		if (!getPlugin().getConfig().contains("McTranslate.api.api_key") || getPlugin().getConfig().getString("McTranslate.api.api_key").equals("key")) {
			this.setMcTranslateApiKey(this.generateApiKey());
			getPlugin().getConfig().set("McTranslate.api.api_key", Main.getMcTranslateApiKey());
			getPlugin().saveConfig();
			getPlugin().reloadConfig();
		} else {
			this.setMcTranslateApiKey(getPlugin().getConfig().getString("McTranslate.api.api_key"));
		}

		this.setJustAsApi(getPlugin().getConfig().getBoolean("McTranslate.use_as_api"));
	}

	@Override
	public void onDisable() {

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {

			if (label.equalsIgnoreCase("translate") || label.equalsIgnoreCase("lang")) {

				cs.sendMessage(prefix + "§cYou must be a player to use other commands than '/ctranslate [EN, ES, FR, RU, ...]'");
				return true;

			} else if (label.equalsIgnoreCase("ctranslate") || label.equalsIgnoreCase("clang")) {
				if (args.length >= 1) {
					McLang currentLang = consoleLang;

					String decodedLang = args[0];
					McLang lang;

					try {
						lang = McLang.valueOf(decodedLang.toUpperCase());
					} catch (Exception e) {
						try {
							cs.sendMessage(prefix + translate("§cThat's not a valid language! Please reference the plugin docs for available languages and their abbreviations.", McLang.EN, currentLang));
							return true;
						} catch (Exception e1) {
							cs.sendMessage(prefix + "§cThat's not a valid language! Please reference the plugin docs for available languages and their abbreviations.");
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

		if (label.equalsIgnoreCase("translate") || label.equalsIgnoreCase("lang")) {

			Player p = (Player) sender;

			if (p.hasPermission("translate.changeLanguage") || p.hasPermission("translate.*") || p.isOp()) {
				if (args.length != 0) {

					String decodedLang = args[0];
					McLang lang;

					try {
						lang = McLang.valueOf(decodedLang.toUpperCase());
					} catch (Exception e) {
						try {
							p.sendMessage(prefix + translate("§cThat's not a valid language! Please ask an adminiostrator for available languages and their abbreviations.", McLang.EN, langMap.get(p.getUniqueId().toString())));
							return true;
						} catch (Exception e1) {
							p.sendMessage(prefix + "§cThat's not a valid language! Please ask an adminiostrator for available languages and their abbreviations.");
							return true;
						}
					}

					langMap.put(p.getUniqueId().toString(), lang);
					saveLangMapToConfig();
					p.sendMessage(prefix + "§aSuccessfully set language to " + lang.toString() + "!");
					return true;
				} else {
					p.sendMessage(prefix + "§cIncorrect usage! Correct usage: '/translate [EN, ES, FR, RU, ...]'");
					return true;
				}
			} else {
				p.sendMessage(prefix + "§cNo permission!");
				return true;
			}

		} else if (label.equalsIgnoreCase("ctranslate") || label.equalsIgnoreCase("clang")) {
			Player p = (Player) sender;

			p.sendMessage(prefix + "§cYou can't use that as a player! Only the console can use this command!");
			return true;
		} else if (label.equalsIgnoreCase("reloadTranslateConfig")) {

			Player p = (Player) sender;
			if (p.hasPermission("translate.reloadConfig") || p.hasPermission("translate.*") || p.isOp()) {

				getPlugin().reloadConfig();
				loadLangMap();
				googleApiKey = getPlugin().getConfig().getString("Google.api_key");
				cs.sendMessage(prefix + "§aSuccessfully reloaded the config!");
				return true;
			} else {
				p.sendMessage(prefix + "§cNo permission!");
				return true;
			}
		}

		return false;
	}

	// Listeners

	@EventHandler
	private void onPlayerChat(AsyncPlayerChatEvent e) {

		if (justApi()) {
			return;
		}

		Player sender = e.getPlayer();

		String originalMessage = e.getMessage();

		e.setCancelled(true);

		if (originalMessage.startsWith(".>")) {

			originalMessage = originalMessage.replaceFirst(".>", "");

			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage(sender.getDisplayName() + "» " + originalMessage);
			}

			return;
		}

		McLang senderLang = getLang(sender.getUniqueId().toString());

		for (Player p : Bukkit.getOnlinePlayers()) {

			McLang recieverLang = getLang(p.getUniqueId().toString());

			if (sender.equals(p)) {
				p.sendMessage(sender.getDisplayName() + "» " + originalMessage);
				continue;
			}

			String translatedMessage = "";

			if (recieverLang.equals(senderLang)) {
				p.sendMessage(sender.getDisplayName() + "» " + originalMessage);
				e.setCancelled(true);
				continue;
			}

			try {

				translatedMessage = translate(originalMessage, senderLang, recieverLang);

			} catch (Exception exception) {

				translatedMessage = originalMessage;
				exception.printStackTrace();
			}

			p.sendMessage(sender.getDisplayName() + "» " + translatedMessage);
		}

		String translatedMessageForConsole = "";

		if (senderLang.equals(consoleLang)) {
			cs.sendMessage(sender.getDisplayName() + "» " + originalMessage);
			return;
		}

		try {
			translatedMessageForConsole = translate(originalMessage, senderLang, consoleLang);
		} catch (Exception exception) {
			translatedMessageForConsole = originalMessage;
			exception.printStackTrace();
		}

		cs.sendMessage(sender.getDisplayName() + "» " + translatedMessageForConsole);
	}

	// Function

	@SuppressWarnings("deprecation")
	private boolean getTranslationService(String googleApiKey) {
		try {
			Main.translateWithCredentials = TranslateOptions.newBuilder().setApiKey(googleApiKey).build().getService();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void loadLangMap() {

		langMap.clear();

		for (int k = 0; getPlugin().getConfig().contains("Players." + k); k++) {

			String pId = getPlugin().getConfig().getString("Players." + k + ".uuid");

			String mcLangUndecoded = getPlugin().getConfig().getString("Players." + k + ".lang");

			McLang lang = McLang.valueOf(mcLangUndecoded);

			langMap.put(pId, lang);
		}

	}

	private void saveLangMapToConfig() {

		playerLoop: for (String key : langMap.keySet()) {
			int k = 0;
			while (getPlugin().getConfig().contains("Players." + k)) {
				if (key.equalsIgnoreCase(getPlugin().getConfig().getString("Players." + k + ".uuid"))) {
					getPlugin().getConfig().set("Players." + k + ".lang", langMap.get(key).toString());
					continue playerLoop;
				}

				k++;
			}

			getPlugin().getConfig().set("Players." + k + ".uuid", key);
			getPlugin().getConfig().set("Players." + k + ".lang", langMap.get(key).toString());
		}

		getPlugin().saveConfig();
		getPlugin().reloadConfig();
	}

	private Plugin getPlugin() {
		return Bukkit.getServer().getPluginManager().getPlugin("McTranslatePlusPlus");
	}

	private McLang getLang(String id) {

		if (!langMap.containsKey(id)) {
			UUID uid = UUID.fromString(id);
			Bukkit.getPlayer(uid).sendMessage(prefix + "§7Use '/translate [EN, ES, FR, RU, ...]' to set your language!");
			langMap.put(id, McLang.EN);
		}

		return langMap.get(id);
	}

	public static String translate(String text, McLang source, McLang target) {

		try {
			Translation translation = translateWithCredentials.translate(text, TranslateOption.sourceLanguage(source.toString().toLowerCase()), TranslateOption.targetLanguage(target.toString().toLowerCase()));

			String newText = translation.getTranslatedText();

			String translatedText = newText.replaceAll("&#39;", "'");

			return translatedText;
		} catch (Exception e) {
			cs.sendMessage(prefix + "§cTranslation service down! (Check your API Key?)");
			return text;
		}
	}

	private String generateApiKey() {

		Long timeInMilis = Calendar.getInstance().getTimeInMillis();

		String key = timeInMilis.toString();

		String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789!@#$%^&*_+-=";

		ArrayList<String> newKey = new ArrayList<String>();

		for (String s : key.split("")) {

			double random = Math.round(Math.random() + 1);

			if (random <= 1D) {

				int location = (int) Math.floor(Math.random() * charset.length());

				s += charset.charAt(location);
				newKey.add(s);
			}

		}

		key = "";

		for (String part : newKey) {
			key += part;
		}

		this.setMcTranslateApiKey(key);

		System.gc();

		return key;
	}

	private void setMcTranslateApiKey(String apiKey) {
		Main.mcTranslateApiKey = apiKey;
	}

	public static String getMcTranslateApiKey() {
		return Main.mcTranslateApiKey;
	}

	public boolean justApi() {
		return useAsJustApi;
	}

	private void setJustAsApi(boolean useAsJustApi) {
		this.useAsJustApi = useAsJustApi;
	}

}
