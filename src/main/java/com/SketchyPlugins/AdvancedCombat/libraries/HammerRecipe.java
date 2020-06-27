package com.SketchyPlugins.AdvancedCombat.libraries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class HammerRecipe {
	private final Material source;
	private final Material destination;
	private final ItemStack[] toDrop;
	
	public HammerRecipe(Material source) {
		this(source, Material.AIR, new ItemStack[0]);
	}
	public HammerRecipe(Material source, Material destination, ItemStack... toDrop) {
		this.source = source;
		this.destination = destination;
		
		if(toDrop == null) toDrop = new ItemStack[0];
		this.toDrop = toDrop;
		
		HammerRecipe.registerRecipe(this);
	}
	
	/**
	 * Returns a {@code true} if the block can be hammered, {@code false} otherwise
	 * @param bl - The block to test
	 * @return If the block can be hammered
	 */
	public boolean canApplyRecipe(Block bl) {
		return bl.getType() == source;
	}
	
	/**
	 * Applies the recipe after first checking if it can
	 * @param bl
	 * @return If the block was successfully hammered
	 */
	public boolean applyRecipe(Block bl) {
		if(!canApplyRecipe(bl)) return false;
		
		final BlockState state = bl.getState();
		try {
			state.setType(destination);
			state.update(true, true);
			
			for(ItemStack i : toDrop) //drop items
				if(i != null) bl.getWorld().dropItemNaturally(bl.getLocation().add(0.5, 0.5, 0.5), i);
			return true;
		}
		catch(Exception e) { //errors out for several reasons, including not having access to the block
			//attempt to undo it
			try {
				state.setType(source);
				state.update();
			}
			catch(Exception f) {}
		}
		
		return false;
	}
	
	private static List<HammerRecipe> recipes;
	
	/**
	 * Called internally on construction to register a hammer's recipe
	 * @param hr - The recipe to register
	 */
	public static void registerRecipe(HammerRecipe hr) {
		if(recipes == null) recipes = new LinkedList<HammerRecipe>();
		recipes.add(hr);
	}
	/**
	 * "Hammers" a block based on all registered recipes
	 * @param bl - The block to hammer
	 * @return If the block was successfully hammered
	 */
	public static boolean hammer(Block bl) {
		//get a list of all recipes that can be applied
		LinkedList<HammerRecipe> possible = new LinkedList<HammerRecipe>();
		for(HammerRecipe hr : recipes)
			if(hr.canApplyRecipe(bl)) possible.add(hr);
		
		//apply random recipes until one works
		Collections.shuffle(possible);
		for(HammerRecipe hr : recipes)
			if(hr.applyRecipe(bl)) return true;
		
		//no recipes worked
		return false;
	}
	/**
	 * Clears all hammer recipes. Should not be used except for configuration reloads!!!
	 */
	public static void clearRecipes() {
		recipes.clear();
	}
	/**
	 * Returns an array copy of all registered recipes
	 * @return The copy of all registered hammer recipes
	 */
	public static HammerRecipe[] getRecipes() {
		return recipes.toArray(new HammerRecipe[0]);
	}
	/**
	 * Constructs a new AnimalInfo based on a configuration section
	 * @param cs - The configuration section
	 * @return A new AnimalInfo
	 */
	public static HammerRecipe fromConfigurationSection(String title, ConfigurationSection cs) {
		if(!isMaterial(title)) return null;
		
		Material source = Material.valueOf(title), destination = Material.AIR;
		ArrayList<ItemStack> toDrop = new ArrayList<ItemStack>();
		
		Map<String, Object> config = cs.getValues(false);
		for(String key : config.keySet()) {
			//if it can parse the key, assume it's destination
			if(isMaterial(key)) destination = Material.valueOf(key);
			//if the key is 'becomes', the value is destination
			if(key.equalsIgnoreCase("becomes") && isMaterial(config.get(key).toString())) destination = Material.valueOf(config.get(key).toString());
			//if the key is 'drops', add to toDrop
			if(key.equalsIgnoreCase("drops")) {
				if(cs.getConfigurationSection(key) != null) {
					ConfigurationSection drops = cs.getConfigurationSection(key);
					for(String key2 : drops.getKeys(false)) {
						if(isMaterial(key2) && drops.getInt(key2, -1) > 0)
							toDrop.add(new ItemStack(Material.valueOf(key2),drops.getInt(key2)));
					}
				}
			}
		}
		
		//final check (toDrop can be empty)
		if(source == null || destination == null)
			return null;
		
		return new HammerRecipe(source, destination, toDrop.toArray(new ItemStack[0]));
	}
	public static HammerRecipe fromStrings(String title, String mat) {
		if(!isMaterial(title) || !isMaterial(mat)) return null;
		return new HammerRecipe(Material.valueOf(title), Material.valueOf(mat));
	}
	private static boolean isMaterial(String s) {
		try {
			Material.valueOf(s);
			return true;
		}
		catch(Exception e) {}
		return false;
	}
}
