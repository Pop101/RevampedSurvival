package com.SketchyPlugins.AdvancedCombat.libraries;

import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeManager implements Listener{
	public static LinkedList<Recipe> registeredRecipes = new LinkedList<Recipe>();
	private static LinkedList<ExtendedRecipe> registeredRecipesDetailed = new LinkedList<ExtendedRecipe>();
	
	private JavaPlugin plugin;
	public RecipeManager(JavaPlugin main) {
		plugin = main;
		main.getServer().getPluginManager().registerEvents(this, main);
	}
	@EventHandler
	public void playerClick(InventoryClickEvent e) {
		updatePlayerRecipes(e.getWhoClicked());
		for(HumanEntity he : e.getViewers())
				updatePlayerRecipes(he);
	}
	@EventHandler
	public void pickupItem(InventoryPickupItemEvent e) {
		if (e.getInventory().getHolder() instanceof HumanEntity)
			updatePlayerRecipes((HumanEntity) e.getInventory().getHolder());
	}
	
	public void updatePlayerRecipes(HumanEntity plr) {
		for(ItemStack is : plr.getInventory().getContents()) {
			if(is == null) continue;
			for(ExtendedRecipe rc : registeredRecipesDetailed) {
				for(Material m : rc.inputs)
					if(is.getType() == m)
						plr.discoverRecipe(rc.key);
				if(is.getType() == rc.result)
					plr.discoverRecipe(rc.key);
			}
		}
	}
	
	public static void registerRecipe(NamespacedKey key, Recipe rec) {
		Bukkit.getServer().addRecipe(rec);
		RecipeManager.registeredRecipes.add(rec);
		RecipeManager.registeredRecipesDetailed.add(new ExtendedRecipe(key, rec));
	}
	
	private static class ExtendedRecipe {
		public final Recipe recipe;
		public final NamespacedKey key;
		public final Material[] inputs;
		public final Material result;
		
		public ExtendedRecipe(NamespacedKey key, Recipe recipe) {
			this.recipe = recipe;
			this.result = recipe.getResult().getType();
			this.key = key;
			
			ArrayList<Material> inputs = new ArrayList<Material>();
			
			if(recipe instanceof FurnaceRecipe)
				inputs.add(((FurnaceRecipe) recipe).getInput().getType());
			
			if(recipe instanceof ShapedRecipe)
				for(Material m : Material.values())
					for(RecipeChoice rc : ((ShapedRecipe) recipe).getChoiceMap().values())
						if(rc != null && m != null && !inputs.contains(m) && rc.test(new ItemStack(m)))
							inputs.add(m);
			
			if(recipe instanceof ShapelessRecipe)
				for(Material m : Material.values())
					for(RecipeChoice rc : ((ShapelessRecipe) recipe).getChoiceList())
						if(rc != null && m != null && !inputs.contains(m) && rc.test(new ItemStack(m)))
							inputs.add(m);
			
			this.inputs = inputs.toArray(new Material[0]);
		}
	}
}
