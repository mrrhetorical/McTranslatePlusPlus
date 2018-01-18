package com.rhetorical.tpp.api;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.rhetorical.tpp.McLang;

public class TranslateFile {

	static Plugin plugin;

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}

		plugin = p;
	}

	public static File getLangFile(McLang lang) {
		File file = new File(plugin.getDataFolder(), "lang_" + lang.toString() + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create 'lang_"+ lang.toString() + ".yml'!");
			}
		}

		return file;
	}

	public static FileConfiguration getData(McLang lang) {

		File file = getLangFile(lang);

		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		return config;
	}

	public static void saveData(McLang lang) {

		File file = getLangFile(lang);
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		try {
			config.save(file);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save 'lang_" + lang.toString() + ".yml'!");
		}
	}
}
