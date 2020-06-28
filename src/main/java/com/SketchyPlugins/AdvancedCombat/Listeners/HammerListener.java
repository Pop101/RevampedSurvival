package com.SketchyPlugins.AdvancedCombat.Listeners;

import java.util.LinkedList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.SketchyPlugins.AdvancedCombat.Main;
import com.SketchyPlugins.AdvancedCombat.libraries.ConfigManager;
import com.SketchyPlugins.AdvancedCombat.libraries.HammerRecipe;
import com.SketchyPlugins.AdvancedCombat.libraries.tools.Hammer;

public class HammerListener implements Listener{
	private JavaPlugin plugin;
	private LinkedList<Hammer> hammers;
	public HammerListener() {
		plugin = Main.instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		hammers = new LinkedList<Hammer>();
		hammers.add(new Hammer(Material.DIAMOND_PICKAXE, Material.DIAMOND));
		hammers.add(new Hammer(Material.GOLDEN_PICKAXE, Material.GOLD_INGOT));
		hammers.add(new Hammer(Material.IRON_PICKAXE, Material.IRON_INGOT));
		
		Hammer stoneHammer = new Hammer("Stone", Material.STONE_PICKAXE, Material.COBBLESTONE);
		stoneHammer.addMaterial(Material.COBBLESTONE);
		hammers.add(stoneHammer);
		
		Hammer woodHammer = new Hammer("Wooden",Material.WOODEN_PICKAXE,Material.OAK_PLANKS);
		woodHammer.addMaterial(Material.ACACIA_PLANKS);
		woodHammer.addMaterial(Material.BIRCH_PLANKS);
		woodHammer.addMaterial(Material.DARK_OAK_PLANKS);
		woodHammer.addMaterial(Material.JUNGLE_PLANKS);
		woodHammer.addMaterial(Material.OAK_PLANKS);
		woodHammer.addMaterial(Material.SPRUCE_PLANKS);
		hammers.add(woodHammer);
	}
	@EventHandler
	public void hitEvent(EntityDamageByEntityEvent e) {
		//damager must be a living entity
		if(!(e.getDamager() instanceof LivingEntity) ) return;
		//damage cause must be a direct attack
		if(!(e.getCause() == DamageCause.ENTITY_ATTACK || e.getCause() == DamageCause.ENTITY_SWEEP_ATTACK)) return;
		
		//get item in hand used to attack
		LivingEntity attacker = (LivingEntity) e.getDamager();
		if(attacker.getEquipment() == null) return;
		ItemStack inHand = attacker.getEquipment().getItemInMainHand();
		
		//make sure inHand is a hammer
		boolean isHammer = false;
		for(Hammer h : hammers)
			if(h.isHammer(inHand)) {
				isHammer = true;
				break;
			}
		if(!isHammer) return;
		
		//make sure the target is a living entity with an inventory
		if(!(e.getEntity() instanceof LivingEntity)) return;
		if(((LivingEntity) e.getEntity()).getEquipment() == null) return;
		
		//make sure target isn't in creative mode
		if(isInCreative((LivingEntity) e.getEntity())) return;
		
		//damage the target's worn armor
		final int toDamage = (int) Math.round(ConfigManager.hammerArmorDamage * e.getOriginalDamage(DamageModifier.BASE));
		for(ItemStack is : ((LivingEntity) e.getEntity()).getEquipment().getArmorContents())
			damageItem(is, toDamage);
		//damage the target's held items
		damageItem(((LivingEntity) e.getEntity()).getEquipment().getItemInMainHand(), toDamage);
		damageItem(((LivingEntity) e.getEntity()).getEquipment().getItemInOffHand(), toDamage);
	}
	@EventHandler
	public void mineBlock(BlockBreakEvent e) {
		if(e.getPlayer().getEquipment() == null) return;
		ItemStack inHand = e.getPlayer().getEquipment().getItemInMainHand();
		
		//make sure inHand is a hammer
		boolean isHammer = false;
		for(Hammer h : hammers)
			if(h.isHammer(inHand)) {
				isHammer = true;
				break;
			}
		if(!isHammer) return;
		
		//attempt to hammer the block
		boolean appliedRecipe = HammerRecipe.hammer(e.getBlock(), inHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
		
		//if it succeeded, cancel the event and damage the hammer by 1 (take unbreaking into account)
		if(appliedRecipe) {
			//unbreaking 1: 50% to not lose, unbreaking 2: 66%, etc
			int unbreaking = 1+inHand.getEnchantmentLevel(Enchantment.DURABILITY);
			if(!isInCreative(e.getPlayer()) && Math.random() < 1.0/unbreaking) 
				damageItem(inHand, 1);
			e.setCancelled(true);
		}
		//if it failed, play a sound effect, damage the hammer, and return
		else {
			if(!isInCreative(e.getPlayer()))
				damageItem(inHand, ConfigManager.hammerMisusePenalty+1);
			
			Location loc = e.getBlock().getLocation().add(0.5,0.5,0.5);
			loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_BREAK, 1.0f, 0.8f);
			loc.getWorld().spawnParticle(Particle.CRIT, loc, 8);
			return;
		}
		
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
