package dev.leonk;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import dev.leonk.blocks.BeatBlock;

public class Send extends BeatBlock {
  public static String BASE_NAME = "Send";
  private static Map<BlockFace, Integer> BLOCK_MODEL_IDS = new HashMap<BlockFace, Integer>() {{
    put(BlockFace.NORTH, 1);
    put(BlockFace.EAST, 2);
    put(BlockFace.SOUTH, 3);
    put(BlockFace.WEST, 4);
    put(BlockFace.UP, 5);
    put(BlockFace.DOWN, 6);
  }};

  public Send(Block block, BlockFace facing) { 
    super(block, BASE_NAME, orient(block, facing)); 
  }

  @SuppressWarnings("deprecation")
  private static int orient(Block block, BlockFace direction) {
    // used the saved note by default
    if (direction == null) {
      NoteBlock note = (NoteBlock) block.getBlockData();
      return note.getNote().getId();
    }
    // map direction to note
    return BLOCK_MODEL_IDS.get(direction);
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
