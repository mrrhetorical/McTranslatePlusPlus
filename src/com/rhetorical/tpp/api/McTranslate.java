package com.rhetorical.tpp.api;

import org.bukkit.plugin.Plugin;

import com.rhetorical.tpp.Main;
import com.rhetorical.tpp.McLang;

@SuppressWarnings({"unused"})
public class McTranslate {

	private String api_key;
	private Plugin plugin;

	public McTranslate(Plugin plugin, String apiKey) {

		if (!apiKey.equals(Main.getMcTranslateApiKey())) {
			
			Main.cs.sendMessage(Main.prefix + "§cInvalid API key for McTranslate++ API!");
			
			return;
		}
		
		TranslateFile.setup(plugin);
		
		this.setPlugin(plugin);
		this.setApiKey(apiKey);

	}

	public boolean translateToFile(String location, String message, McLang source, McLang target) {

		String translatedMessage = Main.translate(message, source, target);
		
		TranslateFile.getData(target).set(location, translatedMessage);
		TranslateFile.saveData(target);
		
		return true;
	}

	public String getTranslationFromFile(String location, McLang lang) {

		String translation;
		
		if (!TranslateFile.getData(lang).contains(location)) {
			Main.cs.sendMessage(Main.prefix + "§cCannot load language that doesn't exist!");
			return null;
		}
		
		translation = TranslateFile.getData(lang).getString(location);
		
		return translation;
	}

	public String translateRealTime(String message, McLang source, McLang target) {

		String translation;
		
		translation = Main.translate(message, source, target);
		
		return translation;
		
	}

	/*
	 * Note: - Below contain functions used by this plugin, but not accessible
	 * to referencing plugins.
	 * 
	 * 
	 */

	private void setApiKey(String apiKey) {
		this.api_key = apiKey;
	}

	private String getApiKey() {
		return this.api_key;
	}

	private void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	private Plugin getPlugin() {
		return this.plugin;
	}

}
