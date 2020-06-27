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

public class Spear{
	public final String name;
	public final ItemStack spear;
	private final String[] spearRecipe = {"  *"," s ","s  "}; //S is string
	
	/**
	 * Constructs a spear with a crafting material
	 * @param spearMat - What material the "spear" appears to be
	 * @param craftingMat - What material the spear is based off of
	 */
	public Spear(Material spearMat, Material craftingMat) {
		this(ToolUtils.formatName(craftingMat.toString()), spearMat, craftingMat);
		addMaterial(craftingMat);
	}
	/**
	 * Constructs a spear without creating its recipe
	 * @param name - Name of the spear
	 * @param spearMat - What material the "spear" appears to be
	 * @param referenceMat - What material the spear is based off of
	 */
	public Spear(String name, Material spearMat, Material referenceMat) {;
		this.name = name;			
		
		ItemStack spearRef = new ItemStack(spearMat);
		ItemMeta spearMeta = spearRef.getItemMeta();
		
		//add name
		spearMeta.setLocalizedName(name+" Spear");
		spearMeta.setDisplayName(ChatColor.ITALIC+name+" Spear");
		
		//calculate and add damage mod
		double damage = Math.max(ToolUtils.getDefaultItemDamage(ToolUtils.getReference(referenceMat)), ToolUtils.getDefaultItemDamage(referenceMat));
		damage = 1 + (damage-1) * ConfigManager.spearDamage;
		
		spearMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
		spearMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "SPEAR_DMG", damage, Operation.ADD_NUMBER,EquipmentSlot.HAND));
		
		//calculate and add speed mod
		double speed = Math.max(ToolUtils.getDefaultItemSpeed(ToolUtils.getReference(referenceMat)), ToolUtils.getDefaultItemSpeed(referenceMat));
		speed = speed*ConfigManager.spearSpeed-4;
				
		spearMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
		spearMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "SPEAR_SPD", speed, Operation.ADD_NUMBER,EquipmentSlot.HAND));
				
		
		//add lore
		List<String> lore = new ArrayList<String>();
		for(String line : ConfigManager.spearLore.split("<br>"))
			lore.add(ChatColor.ITALIC+""+ChatColor.GRAY+line);
		spearMeta.setLore(lore);
		
		//apply meta and set class var
		spearRef.setItemMeta(spearMeta);
		this.spear = spearRef;
	}
	public void addMaterial(Material recipeMat) {
		NamespacedKey key = new NamespacedKey(Main.instance,recipeMat.toString()+"_SPEAR");
		ShapedRecipe recipe = new ShapedRecipe(key, spear);
		recipe.shape(spearRecipe);
		recipe.setIngredient('*', recipeMat);
		recipe.setIngredient('s', Material.STICK);
		//recipe.setIngredient('S', Material.STRING);
		RecipeManager.registerRecipe(key, recipe);
	}
	
	public boolean isSpear(ItemStack is) {
		//avoid checking display name, as anvils mess that up
		boolean basicCheck =  is.getType() == spear.getType() && spear.getItemMeta().hasLore();
		if(!basicCheck) return false;
		if(is.getItemMeta().hasLocalizedName() && is.getItemMeta().getLocalizedName().equalsIgnoreCase(spear.getItemMeta().getLocalizedName()))
			return true;
		
		//if any line of lore matches, it's correct
		/*for(String line : is.getItemMeta().getLore())
			for(String ogLore : spear.getItemMeta().getLore())
				if(ChatColor.stripColor(line).equalsIgnoreCase(ChatColor.stripColor(ogLore)))
					return true;*/
		return false;
	}
}
