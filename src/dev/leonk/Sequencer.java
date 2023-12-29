package dev.leonk;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.inventory.ItemStack;
import dev.leonk.blocks.BeatBlock;
import dev.leonk.blocks.BeatGraph.Edge;

public class Sequencer extends BeatBlock {
  public static String BASE_NAME = "Sequencer";

  public Sequencer(Block block) {
    super(block, BASE_NAME); 
  }

  @Override
  public void stimulate(Edge edge) {
    Block cursor = edge.cursor();

    play(cursor);

    // turn pitch up on netherrack
    if (cursor.getType() == Material.NETHERRACK) {
      changePitchBy(blockHeight(cursor));

    // turn pitch down on cobblestone
    } else if (cursor.getType() == Material.DIRT) {
      changePitchBy(blockHeight(cursor) * -1);
    }
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
    @SuppressWarnings("deprecation")
    double noteColor = note.getId() / 24D;
    pos.getWorld().spawnParticle(Particle.NOTE, pos, 0, noteColor, 0, 0, 1);
  }

  public static ItemStack getItem(int amount) {
    return BeatBlock.getItem("Sequencer", "right click to change speed", Material.NOTE_BLOCK, amount);
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