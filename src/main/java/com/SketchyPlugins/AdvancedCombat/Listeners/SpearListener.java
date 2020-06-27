package com.SketchyPlugins.AdvancedCombat.Listeners;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.SketchyPlugins.AdvancedCombat.Main;
import com.SketchyPlugins.AdvancedCombat.libraries.ConfigManager;
import com.SketchyPlugins.AdvancedCombat.libraries.tools.Hammer;
import com.SketchyPlugins.AdvancedCombat.libraries.tools.Spear;
import com.SketchyPlugins.AdvancedCombat.libraries.tools.ToolUtils;

public class SpearListener implements Listener{
	private JavaPlugin plugin;
	private LinkedList<Spear> spears;
	private HashMap<Player, Long> playerCooldowns; //we sadly need to keep track of player cooldowns
	public SpearListener() {
		plugin = Main.instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		playerCooldowns =  new HashMap<Player, Long>();
		
		
		spears = new LinkedList<Spear>();
		spears.add(new Spear(Material.DIAMOND_SHOVEL, Material.DIAMOND));
		spears.add(new Spear(Material.GOLDEN_SHOVEL, Material.GOLD_INGOT));
		spears.add(new Spear(Material.IRON_SHOVEL, Material.IRON_INGOT));
		
		Spear stoneSpear = new Spear("Stone", Material.STONE_SHOVEL, Material.COBBLESTONE);
		stoneSpear.addMaterial(Material.COBBLESTONE);
		spears.add(stoneSpear);
		
		Spear woodSpear = new Spear("Wooden",Material.WOODEN_SHOVEL,Material.OAK_PLANKS);
		woodSpear.addMaterial(Material.ACACIA_PLANKS);
		woodSpear.addMaterial(Material.BIRCH_PLANKS);
		woodSpear.addMaterial(Material.DARK_OAK_PLANKS);
		woodSpear.addMaterial(Material.JUNGLE_PLANKS);
		woodSpear.addMaterial(Material.OAK_PLANKS);
		woodSpear.addMaterial(Material.SPRUCE_PLANKS);
		spears.add(woodSpear);
	}
	@EventHandler
	public void interactEvent(PlayerInteractEvent e) {
		if(!(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) return;
		if(e.getHand() == null || e.getPlayer().getEquipment() == null) return;
		
		//make sure a spear is being held
		ItemStack inHand = e.getHand() == EquipmentSlot.OFF_HAND ? e.getPlayer().getEquipment().getItemInOffHand() : e.getPlayer().getEquipment().getItemInMainHand();
		boolean isSpear = false;
		for(Spear s : spears)
			if(s.isSpear(inHand)) {
				isSpear = true;
				break;
			}
		if(!isSpear) return;
		
		//loop through all locations in the ray to find an entity
		final double stepLength = 0.4;
		
		LivingEntity found = null;
		for(int step = 1; step < ConfigManager.spearRange/stepLength; step++) {
			//get next location
			Location loc = e.getPlayer().getEyeLocation().clone().add(e.getPlayer().getEyeLocation().getDirection().multiply(stepLength*step));
			//if it's air or passable
			if(loc.getBlock().isPassable() || loc.getBlock().isEmpty() || loc.getBlock().isLiquid()) {
				//get all nearby entities
				for(Entity ent : loc.getWorld().getNearbyEntities(loc, stepLength/1.5,stepLength/1.5,stepLength/1.5)) {
					//if it's a living entity and not the player, we found our match!
					if(ent != null && ent instanceof LivingEntity && !ent.equals(e.getPlayer())) {
						found = (LivingEntity) ent;
						break;
					}
				}
			}
			//if the block is solid, end the "raycast"
			else {
				break;
			}
			//loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK,loc, 5, 0,0,0, 0);
		}
		double damage = ToolUtils.getItemDamage(inHand);
		
		final long cooldownMillis = (long) Math.abs(1000.0/(4+ToolUtils.getItemSpeed(inHand)));
		
		if(playerCooldowns.containsKey(e.getPlayer())) {
			long millisDiff = playerCooldowns.get(e.getPlayer())-System.currentTimeMillis();
			//Bukkit.getLogger().info("Speed: "+ToolUtils.getItemSpeed(inHand)+"  Time left: "+millisDiff+"  Cooldown: "+cooldownMillis);
			if(millisDiff > 0) { //if on cooldown
				//Bukkit.getLogger().info("strike on cooldown");
				damage *= (double) millisDiff / cooldownMillis;
			}
		}
		playerCooldowns.put(e.getPlayer(), System.currentTimeMillis()+cooldownMillis);
		
		//damage the victim
		if(found == null) return;
		found.damage(damage, e.getPlayer());
		if(!isInCreative(e.getPlayer())) damageItem(inHand, 1);
	}
	@EventHandler
	public void hitEvent(EntityDamageByEntityEvent e) {
		//damager must be a player
		if(!(e.getDamager() instanceof Player) ) return;
		//damage cause must be a direct attack
		if(!(e.getCause() == DamageCause.ENTITY_ATTACK || e.getCause() == DamageCause.ENTITY_SWEEP_ATTACK)) return;
		
		//get item in hand used to attack
		Player attacker = (Player) e.getDamager();
		if(attacker.getEquipment() == null) return;
		ItemStack inHand = attacker.getEquipment().getItemInMainHand();
		
		//make sure inHand is a spear
		boolean isSpear = false;
		for(Spear h : spears)
			if(h.isSpear(inHand)) {
				isSpear = true;
				break;
			}
		if(!isSpear) return;
		
		//set the cooldown
		final long cooldownMillis = (long) Math.abs(1000.0/(4+ToolUtils.getItemSpeed(inHand)));
		playerCooldowns.put(attacker, System.currentTimeMillis()+cooldownMillis);
	}
	public void damageItem(ItemStack tool, int toDamage) {
		if(!(tool.getItemMeta() instanceof Damageable)) return;
		
        Damageable itemdmg = (Damageable) tool.getItemMeta();
        int damage = itemdmg.getDamage() + toDamage;
        itemdmg.setDamage((short) damage);
        tool.setItemMeta((ItemMeta) itemdmg);
     
        if(itemdmg.getDamage() <= 0) {
        	tool.setType(Material.AIR);
        }
    }
	public boolean isInCreative(LivingEntity plr) {
		if(!(plr instanceof Player)) return false;
		return ((Player) plr).getGameMode() == GameMode.CREATIVE;
	}
}
