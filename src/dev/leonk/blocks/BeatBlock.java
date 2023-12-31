package dev.leonk.blocks;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.leonk.BeatCraft;
import dev.leonk.blocks.graph.Edge;

@DatabaseTable(tableName = "send_blocks")
public class BeatBlock {
  protected static String BASE_TYPE = "beat_block";
  protected static NamespacedKey itemId = new NamespacedKey(BeatCraft.plugin, BASE_TYPE);
  protected Block block;
  private int blockModelId;

  @DatabaseField(generatedId = true)
  private int id;
  @DatabaseField
  public String world;
  @DatabaseField
  public int x;
  @DatabaseField
  public int y;
  @DatabaseField
  public int z;
  @DatabaseField
  protected String type;

  public BeatBlock() {}
  public BeatBlock(Block block, String type, int blockModelId) {
    this.block = block;
    this.type = type;
    this.blockModelId = blockModelId;
    this.world = block.getWorld().getName();
    this.x = block.getX();
    this.y = block.getY();
    this.z = block.getZ();
    replace(block);
  }

  public String getName() { return type; }
  public Block getBlock() { return block; }
  public void stimulate(Edge edge) {}
  public void trigger() {
    block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().add(0.5, 1.5, 0.5), 1, 0, 0, 0, 0);
  }

  public static Block getBlockAt(BeatBlock block) {
    return BeatCraft.plugin.getServer().getWorld(block.world).getBlockAt(block.x, block.y, block.z);
  }

  public static void noteParticle(Note note, Location pos) {
    pos = pos.add(.5, 1.5, .5);
    @SuppressWarnings("deprecation")
    double noteColor = note.getId() / 24D;
    pos.getWorld().spawnParticle(Particle.NOTE, pos, 0, noteColor, 0, 0, 1);
  }

  public void replace(Block block) {
    block.setType(Material.NOTE_BLOCK);
    block.setMetadata(BASE_TYPE, new FixedMetadataValue(BeatCraft.plugin, type));
    rerender();
  }

  @SuppressWarnings("deprecation")
  public void rerender() {
    BeatCraft.debug(String.format("rerendering %s with note id %d", this, blockModelId));
    NoteBlock note = (NoteBlock) block.getBlockData();
    boolean needsUpdate = (note.getInstrument() != Instrument.CUSTOM_HEAD || blockModelId != note.getNote().getId());
    if (!needsUpdate) return;
    note.setInstrument(Instrument.CUSTOM_HEAD);
    note.setNote(new Note(blockModelId));
    block.setBlockData(note);
    BeatCraft.debug(String.format("rerendered %s with note %s", this, note));
  }

  //
  // accessors

  public static ItemStack getItem(String type, String description, int amount) {
    ItemStack item = new ItemStack(Material.NOTE_BLOCK, amount);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.GOLD + type);
    meta.setLore(new ArrayList<String>() {{ add(description); }});
    meta.getPersistentDataContainer().set(itemId, PersistentDataType.STRING, type);
    // use the designated material
    meta.setCustomModelData(modelKey(type));
    item.setItemMeta(meta);
    return item;
  }

  public static String getType(ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    if (meta == null) return null;
    PersistentDataContainer metadata = meta.getPersistentDataContainer();
    if (!metadata.has(itemId, PersistentDataType.STRING)) return null;
    return metadata.get(itemId, PersistentDataType.STRING);
  }

  public static String getType(Block block) {
    if (block.hasMetadata("mock")) return null;
    List<MetadataValue> meta = block.getMetadata(BASE_TYPE);
    if (meta.isEmpty()) return null;
    return meta.get(0).asString();
  }

  //
  // helpers

  @Override
  public int hashCode() { 
    return block.getLocation().hashCode() + type.hashCode();
  }

  @Override
  public boolean equals(Object other) { 
    if (other instanceof Block) return block.equals(other);
    if (!(other instanceof BeatBlock)) return false;
    return block.getLocation().equals(((BeatBlock) other).block.getLocation()) && type.equals(((BeatBlock) other).type);
  }

  @Override
  public String toString() {
    return String.format("%s(%d,%d,%d)", type, x, y, z);
  }
  protected static BlockFace[] directions() {
    return new BlockFace[] {
      BlockFace.NORTH, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.WEST,
    };
  }

  private static int modelKey(String name) {
    return Math.abs(name.hashCode()) % 1000;
  }
}