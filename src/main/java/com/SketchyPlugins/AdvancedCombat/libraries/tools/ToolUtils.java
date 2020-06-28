package com.SketchyPlugins.AdvancedCombat.libraries.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ToolUtils {
	private static HashMap<Material, Material> referenceSwords;
	private static HashMap<Material, HashMap<Attribute, Double>> defaultItemStats;
	private static void initRefSwords() {
		referenceSwords = new HashMap<Material, Material>();
		referenceSwords.put(Material.DIAMOND, Material.DIAMOND_SWORD);
		referenceSwords.put(Material.GOLD_INGOT, Material.GOLDEN_SWORD);
		referenceSwords.put(Material.IRON_INGOT, Material.IRON_SWORD);
		referenceSwords.put(Material.COBBLESTONE, Material.STONE_SWORD);
		
		referenceSwords.put(Material.ACACIA_PLANKS, Material.WOODEN_SWORD);
		referenceSwords.put(Material.BIRCH_PLANKS, Material.WOODEN_SWORD);
		referenceSwords.put(Material.DARK_OAK_PLANKS, Material.WOODEN_SWORD);
		referenceSwords.put(Material.JUNGLE_PLANKS, Material.WOODEN_SWORD);
		referenceSwords.put(Material.OAK_PLANKS, Material.WOODEN_SWORD);
		referenceSwords.put(Material.SPRUCE_PLANKS, Material.WOODEN_SWORD);
		
		//sadly, default items must be hard-coded in from the base game
		//this seems to be because they're not attribute modifiers and are nbt data instead
		defaultItemStats = new HashMap<Material, HashMap<Attribute, Double>>();
		setDefaultStat(Material.WOODEN_SWORD, 1.6-4, 4);
		setDefaultStat(Material.GOLDEN_SWORD, 1.6-4, 4);
		setDefaultStat(Material.STONE_SWORD, 1.6-4, 5);
		setDefaultStat(Material.IRON_SWORD, 1.6-4, 6);
		setDefaultStat(Material.DIAMOND_SWORD, 1.6-4, 7);
		
		setDefaultStat(Material.WOODEN_AXE, 0.8-4, 7);
		setDefaultStat(Material.GOLDEN_AXE, 1.0-4, 7);
		setDefaultStat(Material.STONE_AXE, 0.8-4, 9);
		setDefaultStat(Material.IRON_AXE, 0.9-4, 9);
		setDefaultStat(Material.DIAMOND_AXE, 1.0-4, 9);
		
		setDefaultStat(Material.WOODEN_HOE, -3, 1);
		setDefaultStat(Material.GOLDEN_HOE, -3, 1);
		setDefaultStat(Material.STONE_HOE,-2, 1);
		setDefaultStat(Material.IRON_HOE, -1, 1);
		setDefaultStat(Material.DIAMOND_HOE, 0, 1);
		
		setDefaultStat(Material.WOODEN_PICKAXE, -2.8, 2);
		setDefaultStat(Material.GOLDEN_PICKAXE, -2.8, 2);
		setDefaultStat(Material.STONE_PICKAXE,-2.8, 3);
		setDefaultStat(Material.IRON_PICKAXE, -2.8, 4);
		setDefaultStat(Material.DIAMOND_PICKAXE, -2.8, 5);
		
		setDefaultStat(Material.WOODEN_SHOVEL, -3, 1);
		setDefaultStat(Material.GOLDEN_SHOVEL, -3, 1);
		setDefaultStat(Material.STONE_SHOVEL,-3, 1);
		setDefaultStat(Material.IRON_SHOVEL, -3, 1);
		setDefaultStat(Material.DIAMOND_SHOVEL, -3, 1);
		
		setDefaultStat(Material.TRIDENT, 1.1, 9);
	}
	private static void setDefaultStat(Material m, double speed, double damage) {
		HashMap<Attribute, Double> stats = new HashMap<Attribute, Double>();
		stats.put(Attribute.GENERIC_ATTACK_SPEED, speed);
		stats.put(Attribute.GENERIC_ATTACK_DAMAGE, damage);
		defaultItemStats.put(m, stats);
	}
	public static Material getReference(Material m) {
		if(referenceSwords == null || referenceSwords.size() == 0)
			initRefSwords();
		return referenceSwords.getOrDefault(m, Material.AIR);
	}
	/**
	 * Formats a material name into a readable name
	 * @param name
	 * @return
	 */
	public static String formatName(String name) {
		name = name.toLowerCase();
		
		//remove "legacy" from string
		if(name.startsWith("legacy_")) {
			name=name.substring("legacy_".length(),name.length());
		}
		//remove "ingot" from string
		if(name.endsWith("_ingot")) {
			name=name.substring(0,name.length()-"_ingot".length());
		}
		
		String[] nameParts = name.split("_");
		String out = "";
		for(String str : nameParts) {
			out += capitalizeFirst(str)+" ";
		}
		if(out.endsWith(" "))
			out = out.substring(0,out.length()-1);
		return capitalizeFirst(out);
	}
	public static String capitalizeFirst(String str) {
		if(str.length() > 1) {
			return str.substring(0,1).toUpperCase()+str.substring(1,str.length());
		}else {
			return str.toUpperCase();
		}
	}
	
	public static double getDefaultItemSpeed(Material m) {
		return getItemSpeed(new ItemStack(m));
	}
	public static double getDefaultItemDamage(Material m) {
		return getItemDamage(new ItemStack(m));
	}
	public static double getItemSpeed(ItemStack is) {
		return getItemStat(is, Attribute.GENERIC_ATTACK_SPEED);
	}
	public static double getItemDamage(ItemStack is) {
		return getItemStat(is, Attribute.GENERIC_ATTACK_DAMAGE);
	}
	public static HashMap<Attribute, Double> getItemStats(ItemStack is) {
		HashMap<Attribute, Double> toReturn = new HashMap<Attribute, Double>();
		for(Attribute a : is.getItemMeta().getAttributeModifiers().keySet())
			toReturn.put(a, getItemStat(is, a));
		return toReturn;
	}
	/**
	 * Returns an approximation of an item's statistics. Multiplicative attributes are mostly lost
	 * @param is - The item stack
	 * @param at - The attribute to look at
	 * @return - An approximation of the item's statistics
	 */
	public static double getItemStat(ItemStack is, Attribute at) {
		ItemMeta im = is.getItemMeta();
		
		//if it has no attribute modifiers, use the base values (if stored)
		if(im.getAttributeModifiers() == null) {
			if(defaultItemStats.containsKey(is.getType()))
				if(defaultItemStats.get(is.getType()).containsKey(at))
					return defaultItemStats.get(is.getType()).get(at);
			return at == Attribute.GENERIC_ATTACK_SPEED ? 4.0 : 1.0;
		}
		
		double atVal = 0.0;
		for(AttributeModifier am : im.getAttributeModifiers(at)) {
			if(am.getOperation() == Operation.ADD_NUMBER)
				atVal += am.getAmount();
			else if (am.getOperation() == Operation.ADD_SCALAR)
				atVal *= am.getAmount();
			else if (am.getOperation() == Operation.MULTIPLY_SCALAR_1)
				atVal = (atVal)*(am.getAmount()+1);
		}
		return atVal;
	}
}
