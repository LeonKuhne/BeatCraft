package dev.leonk.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.leonk.BeatCraft;

@DatabaseTable(tableName = "send_blocks")
public class BeatBlock {
  protected static String BASE_TYPE = "beat_block";
  protected static NamespacedKey itemId = new NamespacedKey(BeatCraft.plugin, BASE_TYPE);
  public Block block;

  static {
    BeatCraft.todo.add("have blocks emit particles to indicate their type");
  }

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
  protected boolean active;
  private BlockFace activatedFrom;
  private BiConsumer<Block, BlockFace> activate;

  public BeatBlock() {}
  public BeatBlock(BeatBlock block, String type, BiConsumer<Block, BlockFace> activate) {
    this(BeatCraft.plugin.getServer().getWorld(block.world).getBlockAt(block.x, block.y, block.z), type, activate);
  }
  public BeatBlock(Block block, String type, BiConsumer<Block, BlockFace> activate) {
    this.type = type;
    this.block = block;
    this.world = block.getWorld().getName();
    this.x = block.getX();
    this.y = block.getY();
    this.z = block.getZ();
    this.activate = activate;
    this.active = true;
    this.activatedFrom = null;
    block.setMetadata(BASE_TYPE, new FixedMetadataValue(BeatCraft.plugin, type));
  }

  public String getName() { return type; }
  public Block getBlock() { return block; }
  public void receiveSignal(BlockFace direction) { 
    active = true;
    activatedFrom = direction.getOppositeFace();
  }
  public void sendSignal(Block target) { 
    BlockFace direction = block.getFace(target);
    if (activatedFrom == direction) return;
    activate.accept(target, direction); 
  }

  public void tick() {
    if (active) { active = false; }
  } 

  public static Block getBlockAt(BeatBlock block) {
    return BeatCraft.plugin.getServer().getWorld(block.world).getBlockAt(block.x, block.y, block.z);
  }

  //
  // accessors

  public static ItemStack getItem(String type, String description, Material material, int amount) {
    ItemStack item = new ItemStack(material, amount);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.GOLD + type);
    meta.setLore(new ArrayList<String>() {{ add(description); }});
    meta.getPersistentDataContainer().set(itemId, PersistentDataType.STRING, type);
    item.setItemMeta(meta);
    return item;
  }

  public String getType() {
    return getType(block);
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

  protected static boolean interruptSignal(Block block) {
    return BeatBlock.getType(block) != null;
  }

  //
  // helpers

  @Override
  public int hashCode() { return block.hashCode(); }
  @Override
  public boolean equals(Object other) { 
    if (other instanceof Block) return block.equals(other);
    if (!(other instanceof BeatBlock)) return false;
    return block.equals(((BeatBlock) other).block); 
  }
}
