package com.SketchyPlugins.AdvancedCombat.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import com.SketchyPlugins.AdvancedCombat.Main;
import com.SketchyPlugins.AdvancedCombat.libraries.ConfigManager;

public class HungerListener implements Listener{
	private JavaPlugin plugin;
	private final int foodLevel = 10;
	
	public HungerListener() {
		plugin = Main.instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if(ConfigManager.disableHunger)
			e.getPlayer().setFoodLevel(foodLevel);
	}
	@EventHandler
	public void onPluginEnable(PluginEnableEvent e) {
		if(e.getPlugin().equals(Main.instance))
			if(ConfigManager.disableHunger)
				for(Player plr : Bukkit.getServer().getOnlinePlayers())
					plr.setFoodLevel(foodLevel);
	}
	@EventHandler
	public void onPlayerSpawn(PlayerRespawnEvent e) {
		e.getPlayer().setFoodLevel(foodLevel);
	}
	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent e) {
		if(!ConfigManager.disableHunger) return;
		if(!(e.getEntity() instanceof Player)) return;
		//get change
		int change = e.getFoodLevel() - ((Player) e.getEntity()).getFoodLevel();
		if(change == 0) return;
		
		//check for passive hunger loss: any half-hunger lost while not under hunger effect
		if(ConfigManager.disablePassiveHunger && change == -1 && ((Player) e.getEntity()).getPotionEffect(PotionEffectType.HUNGER) == null) {
			e.setFoodLevel(foodLevel);
			return;
		}
		
		//if it's positive, use healFactor, else use damageFactor
		double factor = change > 0 ? ConfigManager.healingMult : ConfigManager.damageMult;
		
		//apply (consider not using randomRound)
		double newHealth = randomRound(factor*change)+((Player) e.getEntity()).getHealth();
		((Player) e.getEntity()).setHealth(Math.min(newHealth, ((Player) e.getEntity()).getMaxHealth()));
		
		e.setFoodLevel(foodLevel);
		//((Player) e.getEntity()).setFoodLevel(foodLevel);
	}

	
	private int randomRound(double d) {
		boolean isNegative = d < 0;
		if(isNegative) d *= -1;
		
		int out = 0;
		while(d > 0) {
			if(d >= 1 || Math.random() < d)
				out++;
			d -= 1;
		}
		return isNegative ? -out : out;
	}
}
