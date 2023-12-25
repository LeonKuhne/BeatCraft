package dev.leonk.blocks;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

@DatabaseTable(tableName = "beat_blocks")
public class BeatBlock {
  protected static String BASE_TYPE = "beat_block";
  protected static String base;
  protected static String description;
  protected static Material material;
  protected static NamespacedKey itemId = new NamespacedKey(BeatCraft.plugin, BASE_TYPE);

  public Block block;

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
  public String type;

  public BeatBlock(Block block) {
    this.block = block;
    this.world = block.getWorld().getName();
    this.x = block.getX();
    this.y = block.getY();
    this.z = block.getZ();
    this.setType(base);
  }

  // db constructor
  public BeatBlock() {}
  public BeatBlock init() {
    this.block = BeatCraft.plugin.getServer().getWorld(world).getBlockAt(x, y, z);
    BeatBlock.setType(this.block, type);
    return this;
  }

  //
  // accessors

  public static ItemStack getItem(int amount) {
    ItemStack item = new ItemStack(material, amount);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.GOLD + base);
    meta.setLore(new ArrayList<String>() {{ add(description); }});
    meta.getPersistentDataContainer().set(itemId, PersistentDataType.STRING, base);
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

  public void setType(String type) {
    this.type = type;
    setType(block, type);
  }

  public static void setType(Block block, String type) {
    block.setMetadata(BASE_TYPE, new FixedMetadataValue(BeatCraft.plugin, type));
  }
}
