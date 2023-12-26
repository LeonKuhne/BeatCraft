package dev.leonk.blocks;

import java.util.function.BiConsumer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {

  private BiConsumer<Block, String> onPlace;
  private BiConsumer<Block, String> onBreak;
  private Runnable onSave;
  public BlockListener(BiConsumer<Block, String> onPlace, BiConsumer<Block, String> onBreak, Runnable onSave) {
    this.onPlace = onPlace;
    this.onBreak = onBreak;
    this.onSave = onSave;
  }
  
  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    ItemStack item = event.getItemInHand();
    Block block = event.getBlockPlaced();
    if (item == null || block == null) return;
    String type = BeatBlock.getType(item);
    if (type == null) return;
    onPlace.accept(block, type);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    if (block == null) return;
    String type = BeatBlock.getType(block);
    if (type == null) return;
    event.setDropItems(false);
    onBreak.accept(block, type);
  }

  @EventHandler
  public void onWorldSave(WorldSaveEvent event) {
    onSave.run();
  }
}
