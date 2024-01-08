package dev.leonk;

import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.metadata.FixedMetadataValue;

import dev.leonk.blocks.BeatBlock;
import dev.leonk.blocks.graph.Edge;

public class Sequencer extends BeatBlock {
  public static String BASE_NAME = "Sequencer";
  private static int BLOCK_MODEL_ID = 0;
  public Instrument instrument;
  public Note note;

  public Sequencer(Block block) {
    super(block, BASE_NAME, BLOCK_MODEL_ID);
  }

  @Override
  public void replace(Block block) {
    NoteBlock noteBlock = (NoteBlock) block.getBlockData();
    note = noteBlock.getNote();
    instrument = noteBlock.getInstrument();
    // fetch instrument 
    if (instrument.equals(Instrument.CUSTOM_HEAD)) {
      instrument = (Instrument) block.getMetadata("instrument").get(0).value();
    } else {
      instrument = noteBlock.getInstrument();
    }
    // save instrument in block
    block.setMetadata("instrument", new FixedMetadataValue(BeatCraft.plugin, instrument));
    super.replace(block);
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
    if (cursor.getType() == Material.AIR) return;
    noteParticle(note, cursor.getLocation());
  }

  @SuppressWarnings("deprecation")
  public void changePitchBy(int delta) {
    note = new Note((byte) ((note.getId() + delta + 24) % 24));
  }

  public void play(Block step) { play(step, block.getLocation()); }
  public void play(Block step, Location at) { play(at); }
  public void play(Location at) {
    BeatCraft.debug(String.format("playing %s at %s, instrument: %s, note: %d", this, at, instrument, note.getId()));
    block.getWorld().playNote(at, instrument, note);
  }

  public static ItemStack getItem(int amount) {
    return BeatBlock.getItem(BASE_NAME, "play blocks in line", amount);
  }

  public static ShapedRecipe getRecipe() {
    return new ShapedRecipe(new NamespacedKey(BeatCraft.plugin, BASE_NAME), getItem(1))
      .shape("###", "#x#", "###")
      .setIngredient('#', Material.NOTE_BLOCK)
      .setIngredient('x', Material.BEETROOT);
  }

  private int blockHeight(Block cursor) {
    if (cursor.getType() == Material.AIR) return 0;
    return blockHeight(cursor.getRelative(BlockFace.UP)) + 1;
  }
}