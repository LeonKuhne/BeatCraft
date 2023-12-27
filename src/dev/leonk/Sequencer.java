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
    BeatCraft.todo.add("add delay based on the type of block that is put in the line (like weight or something)");
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
      int maxDistance = 50;
      Map<BlockFace, Block> matches = BlockManager.searchCross(block, maxDistance, this::interruptSignal);
      for (Entry<BlockFace, Block> entry : matches.entrySet()) {
        BeatCraft.debug(String.format("sequencer found %s in line", entry.getKey()));
        playNext.put(entry.getKey(), block.getRelative(entry.getKey()));
      }
      active = false;
      return;
    }

    // play the next sequence
    Map<BlockFace, Block> nextBlocks = new HashMap<>();
    for (Entry<BlockFace, Block> entry : playNext.entrySet()) {
      Block prev = entry.getValue();
      // stop
      if (interruptSignal(prev)) { continue; }
      // play
      play(prev);
      // next
      BlockFace direction =  entry.getKey();
      Block next = prev.getRelative(direction);
      // continue
      nextBlocks.put(direction, next);
    }
    playNext.clear();
    playNext.putAll(nextBlocks);
  }

  public void play(Block sample) {
    if (sample.getType() == Material.AIR) return;
    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_HIT, 1, 1);
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
