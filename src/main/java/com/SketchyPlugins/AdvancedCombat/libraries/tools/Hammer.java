package com.SketchyPlugins.AdvancedCombat.libraries.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.SketchyPlugins.AdvancedCombat.Main;
import com.SketchyPlugins.AdvancedCombat.libraries.ConfigManager;
import com.SketchyPlugins.AdvancedCombat.libraries.RecipeManager;

public class Hammer{
	public final String name;
	public final ItemStack hammer;
	private final String[] hammerRecipe = {"***"," * "," s "};
	
	/**
	 * Constructs a hammer with a crafting material
	 * @param hammerMat - What material the "hammer" appears to be
	 * @param craftingMat - What material the hammer is based off of
	 */
	public Hammer(Material hammerMat, Material craftingMat) {
		this(ToolUtils.formatName(craftingMat.toString()), hammerMat, craftingMat);
		addMaterial(craftingMat);
	}
	/**
	 * Constructs a hammer without creating its recipe
	 * @param name - Name of the hammer
	 * @param hammerMat - What material the "hammer" appears to be
	 * @param referenceMat - What material the hammer is based off of
	 */
	public Hammer(String name, Material hammerMat, Material referenceMat) {
		this.name = name;			
		
		ItemStack hammerRef = new ItemStack(hammerMat);
		ItemMeta hammerMeta = hammerRef.getItemMeta();
		
		//add name
		hammerMeta.setLocalizedName(name+" Hammer");
		hammerMeta.setDisplayName(ChatColor.ITALIC+name+" Hammer");
		
		//calculate and add damage mod
		double damage = Math.max(ToolUtils.getDefaultItemDamage(ToolUtils.getReference(referenceMat)), ToolUtils.getDefaultItemDamage(referenceMat));
		damage = 1 + (damage-1) * ConfigManager.hammerDamage;
		
		hammerMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
		hammerMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "HAMMER_DMG", damage, Operation.ADD_NUMBER,EquipmentSlot.HAND));
		
		//calculate and add speed mod
		double speed = Math.max(ToolUtils.getDefaultItemSpeed(ToolUtils.getReference(referenceMat)), ToolUtils.getDefaultItemSpeed(referenceMat));
		speed = speed*ConfigManager.hammerSpeed-4; //4 is the default swing speed
		
		hammerMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
		hammerMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "HAMMER_SPD", speed, Operation.ADD_NUMBER,EquipmentSlot.HAND));
				
		//add lore
		List<String> lore = new ArrayList<String>();
		for(String line : ConfigManager.hammerLore.split("<br>"))
			lore.add(ChatColor.ITALIC+""+ChatColor.GRAY+ChatColor.stripColor(line)+ChatColor.GRAY+""+ChatColor.ITALIC);
		hammerMeta.setLore(lore);
		
		//apply meta and set class var
		hammerRef.setItemMeta(hammerMeta);
		this.hammer = hammerRef;
	}
	public void addMaterial(Material recipeMat) {
		NamespacedKey key = new NamespacedKey(Main.instance,recipeMat.toString()+"_HAMMER");
		ShapedRecipe recipe = new ShapedRecipe(key, hammer);
		recipe.shape(hammerRecipe);
		recipe.setIngredient('*', recipeMat);
		recipe.setIngredient('s', Material.STICK);
		RecipeManager.registerRecipe(key, recipe);
	}
	
	public boolean isHammer(ItemStack is) {
		//avoid checking display name, as anvils mess that up
		boolean basicCheck =  is.getType() == hammer.getType() && hammer.getItemMeta().hasLore();
		if(!basicCheck) return false;
		if(is.getItemMeta().hasLocalizedName() && is.getItemMeta().getLocalizedName().equalsIgnoreCase(hammer.getItemMeta().getLocalizedName()))
			return true;
		//if any line of lore matches, it's correct
		/*for(String line : is.getItemMeta().getLore())
			for(String ogLore : hammer.getItemMeta().getLore())
				if(ChatColor.stripColor(line).equalsIgnoreCase(ChatColor.stripColor(ogLore)))
					return true;*/
		return false;
	}
}
