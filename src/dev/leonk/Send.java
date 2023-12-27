package dev.leonk;

import java.util.function.BiConsumer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import dev.leonk.blocks.BeatBlock;

public class Send extends BeatBlock {
  public static String BASE_NAME = "Send";
  static {
    BeatCraft.todo.add("rotate sends to change direction");
  }

  public Send(Block block, BiConsumer<Block, BlockFace> forwardSignal) { super(block, BASE_NAME, forwardSignal); }

  @Override
  public void tick() {
    //block.getWorld().playSound(block.getLocation(), Sound.BLOCK_AZALEA_STEP, 1, 1);
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
