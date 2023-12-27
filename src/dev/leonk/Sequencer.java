package dev.leonk;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import dev.leonk.blocks.BeatBlock;
import dev.leonk.blocks.BlockManager;

public class Sequencer extends BeatBlock {
  public static String BASE_NAME = "Sequencer";

  static {
    BeatCraft.todo.add("right click sequencer to change speed, indicate using color/pitch/something");
  }

  private boolean active;
  private Map<BlockFace, Block> playNext; 

  public Sequencer(Block block) { 
    super(block, BASE_NAME); 
    active = true;
    playNext = new HashMap<>();
  }

  @Override
  public void tick() {
    // when sequencers are placed
    if (active) {
      // search for neighboring sends or sequencers
      int maxDistance = 10;
      Map<BlockFace, Block> matches = BlockManager.searchCross(block, maxDistance, this::interruptSignal);
      for (Entry<BlockFace, Block> entry : matches.entrySet()) {
        BeatCraft.debug("sequencer found " + entry.getValue().getType());
        playNext.put(entry.getKey(), block);
      }
      active = false;
      return;
    }

    // play the next sequence
    for (Entry<BlockFace, Block> entry : playNext.entrySet()) {
      BlockFace direction = (BlockFace) entry.getKey();
      Block block = entry.getValue().getRelative(direction);
      // play a note, if its not air
      if (block.getType() == Material.AIR) {
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
      }
      // stop/continue
      if (interruptSignal(block)) { playNext.remove(direction); continue; }
      playNext.put(direction, block);
    }
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
  
  private boolean interruptSignal(Block block) {
    return BeatBlock.getType(block) != null;
  }
}
