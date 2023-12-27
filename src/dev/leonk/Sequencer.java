package dev.leonk;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
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

  private Map<BlockFace, Block> playAll;
  private Map<BlockFace, Block> playNext;

  public Sequencer(Block block, BiConsumer<Block, BlockFace> activate) {
    super(block, BASE_NAME, activate); 
    playNext = new HashMap<>();
    playAll = new HashMap<>();
  }

  @Override
  public void tick() {
    // when sequencers are placed
    if (active) {
      // search for neighboring sends or sequencers
      int maxDistance = 50;
      Map<BlockFace, Block> matches = BlockManager.searchCross(block, maxDistance);
      for (Entry<BlockFace, Block> entry : matches.entrySet()) {
        BeatCraft.debug(String.format("sequencer found %s in line", entry.getKey()));
        playAll.put(entry.getKey(), block.getRelative(entry.getKey()));
        playNext = new HashMap<>(playAll);
      }
      active = false;
      return;
    }

    // play the next sequence
    Map<BlockFace, Block> nextBlocks = new HashMap<>();
    for (Entry<BlockFace, Block> entry : playNext.entrySet()) {
      Block prev = entry.getValue();
      // stop
      if (interruptSignal(prev)) { 
        sendSignal(prev);
        // reset sequence
        BlockFace direction =  entry.getKey();
        nextBlocks.put(direction, playAll.get(direction));
        continue; 
      }
      // play
      play(prev);
      // next
      BlockFace direction =  entry.getKey();
      Block next = prev.getRelative(direction);
      nextBlocks.put(direction, next);
    }
    // continue
    playNext.clear();
    playNext.putAll(nextBlocks);
    super.tick();
  }

  public void play(Block step) {
    if (step.getType() == Material.AIR) return;
    // play the note of the noteblock 
    NoteBlock note = (NoteBlock) block.getBlockData(); 
    block.getWorld().playNote(block.getLocation(), note.getInstrument(), note.getNote());
    // spawn a particle effect over the block, the same particle effect as the note block note
    noteParticle(step.getLocation(), note.getNote());
  }

  private void noteParticle(Location pos, Note note) {
    pos = pos.add(.5, 1.5, .5);
    double noteColor = note.getId() / 24D;
    pos.getWorld().spawnParticle(Particle.NOTE, pos, 0, noteColor, 0, 0, 1);
  }

  public void disconnect(BeatBlock removedBeat) {
    Block removedBlock = removedBeat.block;
    if (playAll.containsValue(removedBlock)) {
      // remove it
      for (Entry<BlockFace, Block> entry : playAll.entrySet()) {
        if (entry.getValue() != removedBlock) continue;
        playAll.remove(entry.getKey());
        playNext.remove(entry.getKey());
        break;
      }
    }
  }

  public void connect(BeatBlock addedBeat) {
    // check if two of the axis are the same


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