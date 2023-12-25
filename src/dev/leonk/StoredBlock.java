package dev.leonk;

import org.bukkit.block.Block;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "beat_blocks")
public class StoredBlock {
  @DatabaseField(generatedId = true)
  private int id;
  @DatabaseField
  private String world;
  @DatabaseField
  private int x;
  @DatabaseField
  private int y;
  @DatabaseField
  private int z;

  public StoredBlock() {}
  public StoredBlock(Block block) {
    this.world = block.getWorld().getName();
    this.x = block.getX();
    this.y = block.getY();
    this.z = block.getZ();
  }

  //
  // accessors

  public Block getBlock() {
    return BeatCraft.plugin.getServer().getWorld(world).getBlockAt(x, y, z);
  }
}
