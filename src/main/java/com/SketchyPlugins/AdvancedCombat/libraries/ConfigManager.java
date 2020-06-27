package com.SketchyPlugins.AdvancedCombat.libraries;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
	private static JavaPlugin plugin;
	
	public static boolean bedsExplode = true;
	public static boolean bedsSetSpawn = true;
	public static double bedExplosionPower = 3.0;
	public static boolean bedExplosionNapalm = false;
	
	public static boolean disableHunger = true;
	public static boolean disablePassiveHunger = true;
	public static double healingMult = 0.6;
	public static double damageMult = 1.0;
	
	//public static boolean knockbackMod = true;
	//public static double knockbackAmount = 2.0;
	
	public static boolean hitgraceMod = true;
	public static double hitgraceTime = 0.1;
	
	public static boolean doSpears = true;
	public static double spearRange = 6.0;
	public static double spearDamage = 0.7;
	public static double spearSpeed = 0.7;
	public static String spearLore = "Strike at great range";
	
	public static boolean doHammers = true;
	public static double hammerDamage = 0.6;
	public static double hammerSpeed = 0.4;
	public static double hammerArmorDamage = 2.5;
	public static int hammerMisusePenalty = 3;
	public static String hammerLore = "Breaks down blocks\\ninto less advanced forms\\n\\nDeals additional damage to armor";
	
	public static void setPlugin(JavaPlugin plugin_) {
		plugin = plugin_;
	}
	public static void setupConfig() {
		if(!isCorrectlySetup()) return;
		
		File config = new File(plugin.getDataFolder(),"config.yml");
		if(!config.exists() || !plugin.getConfig().contains("Beds Explode")) {
			//reset config
			Bukkit.getLogger().info("Resetting config");
			plugin.getDataFolder().mkdirs();
			copyResource("config.yml", plugin.getDataFolder().getAbsolutePath()+File.separator+"config.yml");
		}
		
		//now, we load it into memory
		parseConfig();
	}
	public static void parseConfig() {
		if(!isCorrectlySetup()) return;
		
		bedsExplode = plugin.getConfig().getBoolean("Beds Explode",bedsExplode);
		bedsSetSpawn = plugin.getConfig().getBoolean("Exploding Beds Set Spawnpoint",bedsSetSpawn);
		bedExplosionPower = plugin.getConfig().getDouble("Bed Explosion Power",bedExplosionPower);
		bedExplosionNapalm = plugin.getConfig().getBoolean("Bed Explosion Napalm",bedExplosionNapalm);
		
		disableHunger = plugin.getConfig().getBoolean("Disable Hunger Mechanics",disableHunger);
		disablePassiveHunger = plugin.getConfig().getBoolean("Disable Passive Hunger Loss",disablePassiveHunger);
		healingMult = plugin.getConfig().getDouble("Healing Multiplier",healingMult);
		damageMult = plugin.getConfig().getDouble("Damage Multiplier",damageMult);
		
		//knockbackMod = plugin.getConfig().getBoolean("Enable Knockback Adjustment",knockbackMod);
		//knockbackAmount = plugin.getConfig().getDouble("Knockback Amount",knockbackAmount);
		
		hitgraceTime = plugin.getConfig().getDouble("Hit Grace Time",hitgraceTime);
		hitgraceMod = plugin.getConfig().getBoolean("Enable Hit Grace Adjustment",hitgraceMod);		
		
		doSpears = plugin.getConfig().getBoolean("Enable Spears",doSpears);
		spearRange = plugin.getConfig().getDouble("Spear Range",spearRange);
		spearDamage = plugin.getConfig().getDouble("Spear Damage Multiplier",spearDamage);
		spearSpeed = plugin.getConfig().getDouble("Spear Speed Multiplier",spearSpeed);
		spearLore = plugin.getConfig().getString("Spear Lore",spearLore).replaceAll("([ ]*<br>[ ]*)|(<br>[ ]*)|([ ]*<br>)+", "<br>");
		
		doHammers = plugin.getConfig().getBoolean("Enable Hammers",doHammers);
		hammerDamage = plugin.getConfig().getDouble("Hammer Damage Multiplier",hammerDamage);
		hammerSpeed = plugin.getConfig().getDouble("Hammer Speed Multiplier",hammerSpeed);
		hammerArmorDamage = plugin.getConfig().getDouble("Hammer Armor Damage Multiplier",hammerArmorDamage);
		hammerMisusePenalty = roundToInt(plugin.getConfig().getDouble("Hammer Misuse Penalty",hammerMisusePenalty));
		hammerLore = plugin.getConfig().getString("Hammer Lore",hammerLore).replaceAll("([ ]*<br>[ ]*)|(<br>[ ]*)|([ ]*<br>)+", "<br>");
		
		Map<String, Object> hammer = plugin.getConfig().getConfigurationSection("Hammer Recipes").getValues(false);
		for(String key : hammer.keySet()) {
			if(hammer.get(key) instanceof ConfigurationSection)
				HammerRecipe.fromConfigurationSection(key, (ConfigurationSection) hammer.get(key));
			else
				HammerRecipe.fromStrings(key, hammer.get(key).toString());
		}
		Bukkit.getLogger().log(Level.INFO, "Loaded "+HammerRecipe.getRecipes().length+" recipes");
	}
	public static void copyResource(String res, String dest) {
	    try {
	    	InputStream src = plugin.getResource(res);
			Files.copy(src, Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.WARNING, "Internal JAR file missing");
		}
	}
	private static boolean isCorrectlySetup() {
		return plugin != null;
	}
	private static int roundToInt(double d) {
		return (int) Math.round(d);
	}
}
