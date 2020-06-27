package com.SketchyPlugins.AdvancedCombat.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.plugin.java.JavaPlugin;

import com.SketchyPlugins.AdvancedCombat.Main;
import com.SketchyPlugins.AdvancedCombat.libraries.ConfigManager;

public class BedListener implements Listener{
	private JavaPlugin plugin;
	public BedListener() {
		plugin = Main.instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler
	public void trySleep(PlayerBedEnterEvent e) {
		if(!ConfigManager.bedsExplode) return; //return if beds shoudn't explode
		if(e.getBedEnterResult() != BedEnterResult.OK) return; //return if you didn't enter the bed
		
		//Explode the bed
		e.getBed().getWorld().createExplosion(e.getBed().getLocation().add(0.5, 0.5, 0.5), (float) ConfigManager.bedExplosionPower, ConfigManager.bedExplosionNapalm);
		e.getBed().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, e.getBed().getLocation().add(0.5, 1.5, 0.5), 8, 2, 2, 2);
		e.getBed().getWorld().playSound(e.getBed().getLocation().add(0.5, 0.5, 0.5), Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 1.1f);
		
		//add lightning for bonus fun
		e.getBed().getWorld().strikeLightningEffect(e.getBed().getLocation().add(0.5, 0.5, 0.5));
		
		//set spawnpoint
		if(Con)
		e.getPlayer().setBedSpawnLocation(e.getBed().getLocation().add(0.5, 0.5, 0.5), true);
		e.getPlayer().sendMessage("Spawn Point Set");
		
		//cancel entering the bed
		e.setUseBed(Result.DENY);
	}
}
