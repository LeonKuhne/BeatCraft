package dev.leonk;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import dev.leonk.blocks.BeatBlock;
import dev.leonk.blocks.BeatGraph.Edge;

public class Send extends BeatBlock {
  public static String BASE_NAME = "Send";

  public Send(Block block) { super(block, BASE_NAME); }

  @Override
  public void trigger(Edge edge) {
    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_BARREL_OPEN, 1, 1);
    super.trigger(edge);
  }

  public static ItemStack getItem(int amount) {
    return getItem("Send", "forward audio signals", Material.NOTE_BLOCK, amount);
  }

  public static Recipe craftRecipe() {
    NamespacedKey key = new NamespacedKey(BeatCraft.plugin, "craft-send");
    ShapedRecipe recipe = new ShapedRecipe(key, getItem(1)); 
    recipe.shape("###", "#o#", "###");
    recipe.setIngredient('#', Material.NOTE_BLOCK);
    recipe.setIngredient('o', Material.ENDER_PEARL);
    return recipe;
  }
}
