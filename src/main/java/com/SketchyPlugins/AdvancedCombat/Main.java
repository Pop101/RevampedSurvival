package com.SketchyPlugins.AdvancedCombat;

import org.bukkit.plugin.java.JavaPlugin;

import com.SketchyPlugins.AdvancedCombat.Listeners.BedListener;
import com.SketchyPlugins.AdvancedCombat.Listeners.HammerListener;
import com.SketchyPlugins.AdvancedCombat.Listeners.HitGraceTimeListener;
import com.SketchyPlugins.AdvancedCombat.Listeners.HungerListener;
import com.SketchyPlugins.AdvancedCombat.Listeners.SpearListener;
import com.SketchyPlugins.AdvancedCombat.libraries.ConfigManager;

public final class Main extends JavaPlugin{
	public static JavaPlugin instance;
	
	@Override
	public void onEnable() {
		Main.instance = this;
		
		ConfigManager.setPlugin(this);
		ConfigManager.setupConfig();
		
		if(ConfigManager.hitgraceMod)
			new HitGraceTimeListener();
		
		if(ConfigManager.doHammers)
			new HammerListener();
		
		if(ConfigManager.doSpears)
			new SpearListener();
		
		if(ConfigManager.bedsExplode)
			new BedListener();
		
		if(ConfigManager.disableHunger)
			new HungerListener();
	}
	@Override
	public void onDisable() {
	}
}
