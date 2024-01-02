package dev.leonk.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
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
  public BeatBlock(BeatBlock block, String type, int blockModelId) {
    this(BeatCraft.plugin.getServer().getWorld(block.world).getBlockAt(block.x, block.y, block.z), type, blockModelId);
  }
  public BeatBlock(Block block, String type, int blockModelId) {
    this.type = type;
    this.block = block;
    this.world = block.getWorld().getName();
    this.x = block.getX();
    this.y = block.getY();
    this.z = block.getZ();
    place(block, type, blockModelId);
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

  public static void place(Block block, String type, int blockModelId) {
    block.setType(Material.NOTE_BLOCK);
    block.setMetadata(BASE_TYPE, new FixedMetadataValue(BeatCraft.plugin, type));
    // set instrument to custom head
    NoteBlock note = (NoteBlock) block.getBlockData();
    note.setInstrument(Instrument.CUSTOM_HEAD);
    note.setNote(new Note(blockModelId));
    block.setBlockData(note);
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
    List<MetadataValue> meta = block.getMetadata(BASE_TYPE);
    if (meta.isEmpty()) return null;
    return meta.get(0).asString();
  }

  public static ItemStack uncraft(Set<ItemStack> ingredients, String type, ItemStack result) {
    // 1 sequencer -> 8 note blocks
    if (ingredients.size() == 1) {
      ItemStack item = ingredients.iterator().next(); 
      if (type.equals(BeatBlock.getType(item))) {
        return result;
      }
    }
    return null;
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

  protected static boolean recipeMatch(String pattern, ItemStack[] ingredients, Map<String, Material> mapping) {
    if (pattern.length() != ingredients.length) return false;
    return eachMapping(pattern, ingredients, (symbol, item) -> {
      if (item == null) return false;
      Material material = mapping.get(String.valueOf(symbol));
      return material == item.getType();
    }, 0);
  }

  protected static boolean eachMapping(String pattern, ItemStack[] ingredients, BiFunction<String, ItemStack, Boolean> filter, int cursor) {
    if (cursor == pattern.length()) return true;
    // find next
    String symbol = pattern.substring(cursor, cursor + 1); 
    ItemStack ingredient = ingredients[cursor];
    // check filter
    if (!filter.apply(symbol, ingredient)) return false;
    return eachMapping(pattern, ingredients, filter, cursor + 1);
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