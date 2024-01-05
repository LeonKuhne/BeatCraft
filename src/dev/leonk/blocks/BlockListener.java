package dev.leonk.blocks;

import java.util.function.BiConsumer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import dev.leonk.BeatCraft;

public class BlockListener implements Listener {

  private BiConsumer<Block, String> onPlace;
  private BiConsumer<Block, String> onBreak;
  private BiConsumer<Block, String> onPunch;
  private Runnable onSave;

  public BlockListener(
    BiConsumer<Block, String> onPlace, 
    BiConsumer<Block, String> onBreak, 
    BiConsumer<Block, String> onPunch, 
    Runnable onSave
  ) {
    this.onPlace = onPlace;
    this.onBreak = onBreak;
    this.onPunch = onPunch;
    this.onSave = onSave;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    // block interactions
    Block block = event.getClickedBlock();
    if (block == null) return;
    // right click interaciotns
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (BeatBlock.getType(block) == null) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    BeatCraft.debug(String.format("onBlockPlace, event %s", event));
    ItemStack item = event.getItemInHand();
    Block block = event.getBlockPlaced();
    if (item == null || block == null) return;
    String type = BeatBlock.getType(item);
    if (type == null) return;
    onPlace.accept(block, type);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    if (!submitBlock(event, onBreak)) return;
    event.setDropItems(false);
  }

  @EventHandler
  public void onBlockDamage(BlockDamageEvent event) {
    submitBlock(event, onPunch);
  }

  @EventHandler
  public void onWorldSave(WorldSaveEvent event) {
    onSave.run();
  }

  // 
  // helpers

  private static boolean submitBlock(BlockEvent event, BiConsumer<Block, String> submit) {
    return submitBlock(event.getBlock(), submit);
  }
  private static boolean submitBlock(Block block, BiConsumer<Block, String> submit) {
    if (block == null) return false;
    String type = BeatBlock.getType(block);
    if (type == null) return false;
    submit.accept(block, type);
    return true;
  }
}
