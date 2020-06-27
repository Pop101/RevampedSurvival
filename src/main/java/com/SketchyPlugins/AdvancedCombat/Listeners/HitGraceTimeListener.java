package com.SketchyPlugins.AdvancedCombat.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.SketchyPlugins.AdvancedCombat.Main;
import com.SketchyPlugins.AdvancedCombat.libraries.ConfigManager;

public class HitGraceTimeListener implements Listener{
	private JavaPlugin plugin;
	public HitGraceTimeListener() {
		plugin = Main.instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler
	public void hitEvent(EntityDamageEvent e) {
		Entity victim = e.getEntity();
		if(victim == null || !(victim instanceof LivingEntity)) return;
		
		final LivingEntity livingVictim = (LivingEntity) victim;
		final int time = Math.max(1, roundToInt(20*ConfigManager.hitgraceTime))-1;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new Runnable() {
            public void run() {
            	livingVictim.setNoDamageTicks(time);
            }
        },1);
	}
	
	private static int roundToInt(double d) {
		return (int) Math.round(d);
	}
}
