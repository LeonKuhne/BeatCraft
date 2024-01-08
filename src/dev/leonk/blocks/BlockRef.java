package dev.leonk.blocks;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockRef {
  public Block block;
  public String type;
  public BlockFace orientation;
  public BlockRef(Block block, String type) { this(block, type, null); }
  public BlockRef(Block block, String type, BlockFace orientation) {
    this.block = block;
    this.type = type;
    this.orientation = orientation;
  }
}
