package dev.leonk;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import dev.leonk.blocks.BeatBlock;


public class Sequencer extends BeatBlock {

  static {
    base = "Sequencer";
    description = "right click to change speed";
    material = Material.NOTE_BLOCK;

    BeatCraft.todo.add("right click sequencer to change speed, indicate using color/pitch/something");
  }

  public Sequencer(Block block) {
    super(block);
  }

  public static Recipe getRecipe() {
    ItemStack item = getItem(1); 

    // define recipe
    ShapedRecipe recipe = new ShapedRecipe(itemId, item); 
    recipe.shape("###", "#*#", "###");
    recipe.setIngredient('#', Material.NOTE_BLOCK);
    recipe.setIngredient('*', Material.BEETROOT);
    return recipe;
  }
}
