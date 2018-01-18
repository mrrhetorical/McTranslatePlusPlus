package com.rhetorical.tpp.api;

import org.bukkit.plugin.Plugin;

import com.rhetorical.tpp.Main;
import com.rhetorical.tpp.McLang;

public class McTranslate {


	private Plugin plugin;

	public McTranslate(Plugin plugin, String apiKey) {

		if (!apiKey.equals(Main.getMcTranslateApiKey())) {
			
			Main.cs.sendMessage(Main.prefix + "§cInvalid API key for McTranslate++ API!");
			
			return;
		}
		
		this.setPlugin(plugin);
		
		TranslateFile.setup(this.getPlugin());

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

	public String translateRuntime(String message, McLang source, McLang target) {

		String translation;
		
		translation = Main.translate(message, source, target);
		
		return translation;
		
	}

	private void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	private Plugin getPlugin() {
		return this.plugin;
	}

}
