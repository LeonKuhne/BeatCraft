package dev.leonk;

import java.util.function.Consumer;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {

  private Consumer<BeatBlock> onPlace;
  private Consumer<Block> onBreak;
  private Runnable onSave;
  public BlockListener(Consumer<BeatBlock> onPlace, Consumer<Block> onBreak, Runnable onSave) {
    this.onPlace = onPlace;
    this.onBreak = onBreak;
    this.onSave = onSave;
  }
  
  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    BeatBlock block = mapBlock(event.getItemInHand(), event.getBlockPlaced());
    if (block == null) return;
    // place beat
    BeatCraft.debug(String.format("beat block placed in world %s at %d, %d, %d", block.world, block.x, block.y, block.z));
    onPlace.accept(block);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    ItemStack item = mapItem(block);
    if (item == null) return;
    // drop beat
    BeatCraft.debug("beat block broken");
    event.setDropItems(false);
    block.getWorld().dropItemNaturally(block.getLocation(), item);
    onBreak.accept(block);
  }

  @EventHandler
  public void onWorldSave(WorldSaveEvent event) {
    onSave.run();
  }

  // item -> beatblock
  private BeatBlock mapBlock(ItemStack item, Block block) {
    if (item == null) return null;
    String type = BeatBlock.getType(item);
    if (type == null) return null;
    switch(type) {
      case "Sequencer":
        return new Sequencer(block);
      default:
        BeatCraft.debug("unknown item type: " + type);
    }
    return null;
  }

  // block -> beatblock item
  private ItemStack mapItem(Block block) {
    String type = BeatBlock.getType(block);
    if (type == null) return null;
    switch (type) {
      case "Sequencer":
        return Sequencer.getItem(1);
      default:
        BeatCraft.debug("unknown block type: " + type);
    }
    return null;
  }
}
