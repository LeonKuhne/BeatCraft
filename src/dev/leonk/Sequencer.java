package dev.leonk;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.inventory.ItemStack;
import dev.leonk.blocks.BeatBlock;
import dev.leonk.blocks.graph.Edge;

public class Sequencer extends BeatBlock {
  public static String BASE_NAME = "Sequencer";

  public Sequencer(Block block) {
    super(block, BASE_NAME); 
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
    return BeatBlock.getItem("Sequencer", "right click to change pitch", Material.NOTE_BLOCK, amount);
  }

  public static ItemStack craftShapeless(Set<ItemStack> ingredients) {
    return uncraft(ingredients, BASE_NAME, new ItemStack(Material.NOTE_BLOCK, 8));
  }

  public static ItemStack craftShaped(ItemStack[] ingredients) {
    Map<String, Material> map = new HashMap<String, Material>() {{
      put("#", Material.NOTE_BLOCK);
      put("x", Material.BEETROOT);
    }};

    // 8 note blocks surrounding 1 beetroot
    if (BeatBlock.recipeMatch("####x####", ingredients, map)) return getItem(1);
    return null;
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