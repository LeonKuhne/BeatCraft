package dev.leonk;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import dev.leonk.blocks.BeatBlock;

public class Send extends BeatBlock {
  public static String BASE_NAME = "Send";
  private static int BLOCK_MODEL_ID = 1;

  public Send(Block block) { 
    super(block, BASE_NAME, BLOCK_MODEL_ID); 
  }

  public static ItemStack getItem(int amount) {
    return BeatBlock.getItem(BASE_NAME, "forward audio signals", amount);
  }

  public static ShapedRecipe getRecipe() {
    return new ShapedRecipe(new NamespacedKey(BeatCraft.plugin, BASE_NAME), getItem(1))
      .shape("###", "#o#", "###")
      .setIngredient('#', Material.NOTE_BLOCK)
      .setIngredient('o', Material.ENDER_PEARL);
  }
}
