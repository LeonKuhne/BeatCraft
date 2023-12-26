package dev.leonk;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import dev.leonk.blocks.BeatBlock;

public class Sequencer extends BeatBlock {
  public static String BASE_NAME = "Sequencer";

  static {
    BeatCraft.todo.add("right click sequencer to change speed, indicate using color/pitch/something");
  }

  public Sequencer() {}
  public Sequencer(Block block) { super(block, BASE_NAME); }
  public Sequencer(BeatBlock block) { super(block, BASE_NAME); }

  @Override
  public void tick() {
    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
  }

  public static ItemStack getItem(int amount) {
    return BeatBlock.getItem("Sequencer", "right click to change speed", Material.NOTE_BLOCK, amount);
  }

  public static Recipe craftRecipe() {
    NamespacedKey key = new NamespacedKey(BeatCraft.plugin, "craft-sequencer");
    ShapedRecipe recipe = new ShapedRecipe(key, getItem(1)); 
    recipe.shape("###", "#*#", "###");
    recipe.setIngredient('#', Material.NOTE_BLOCK);
    recipe.setIngredient('*', Material.BEETROOT);
    return recipe;
  }
}
