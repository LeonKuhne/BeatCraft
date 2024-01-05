package dev.leonk;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import dev.leonk.blocks.BeatBlock;
import dev.leonk.blocks.graph.Edge;

public class Sequencer extends BeatBlock {
  public static String BASE_NAME = "Sequencer";
  private static int BLOCK_MODEL_ID = 0;

  public Sequencer(Block block) {
    super(block, BASE_NAME, BLOCK_MODEL_ID);
  }

  @Override
  public void stimulate(Edge edge) {
    Block cursor = edge.cursor();

    // turn pitch up on dirt
    if (cursor.getType() == Material.DIRT) {
      changePitchBy(blockHeight(cursor));

    // turn pitch down on netherrack
    } else if (cursor.getType() == Material.NETHERRACK) {
      changePitchBy(blockHeight(cursor) * -1);
    }

    // spawn a note particle over the cursor
    NoteBlock note = getNote(cursor);
    if (note == null) return;
    noteParticle(note.getNote(), cursor.getLocation());
  }

  public void play(Block step) { play(step, block.getLocation()); }
  public void play(Block step, Location at) { play(getNote(step), at); }
  public void play(NoteBlock note, Location at) {
    if (note == null) return;
    block.getWorld().playNote(at, note.getInstrument(), note.getNote());
  }

  public NoteBlock getNote(Block step) {
    if (step == null || step.getType() == Material.AIR) return null;
    return (NoteBlock) block.getBlockData(); 
  }

  public static ItemStack getItem(int amount) {
    return BeatBlock.getItem("Sequencer", "right click to change pitch", amount);
  }

  public static Recipe getRecipe() {
    return new ShapedRecipe(new NamespacedKey(BeatCraft.plugin, BASE_NAME), getItem(1))
      .shape("###", "#x#", "###")
      .setIngredient('#', Material.NOTE_BLOCK)
      .setIngredient('x', Material.BEETROOT);
  }

  @SuppressWarnings("deprecation")
  private void changePitchBy(int delta) {
    NoteBlock noteBlock = (NoteBlock) block.getBlockData();
    noteBlock.setNote(new Note((byte) ((noteBlock.getNote().getId() + delta + 24) % 24)));
    block.setBlockData(noteBlock);
  }

  private int blockHeight(Block cursor) {
    if (cursor.getType() == Material.AIR) return 0;
    return blockHeight(cursor.getRelative(BlockFace.UP)) + 1;
  }
}