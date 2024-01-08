package dev.leonk.blocks;

import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import dev.leonk.BeatCraft;


public class BlockListener implements Listener {
  private Consumer<BlockRef> onPlace;
  private Consumer<BlockRef> onBreak;
  private Consumer<BlockRef> onPunch;
  private Consumer<BlockRef> onInteract;
  private Consumer<Block> onUpdate;
  private Runnable onSave;

  public BlockListener(
    Consumer<BlockRef> onPlace, 
    Consumer<BlockRef> onBreak, 
    Consumer<BlockRef> onPunch, 
    Consumer<BlockRef> onInteract,
    Consumer<Block> onUpdate,
    Runnable onSave
  ) {
    this.onPlace = onPlace;
    this.onBreak = onBreak;
    this.onPunch = onPunch;
    this.onInteract = onInteract;
    this.onUpdate = onUpdate;
    this.onSave = onSave;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    BeatCraft.debug(String.format("onPlayerInteract, event %s", event));
    // right click interactions
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    // filter beatblocks
    Block block = event.getClickedBlock();
    if (block == null) return;
    String type = BeatBlock.getType(block);
    if (type == null) return;
    // reject on item use
    ItemStack item = event.getItem();
    if (item != null && event.getPlayer().isSneaking()) return;
    event.setCancelled(true);
    onInteract.accept(new BlockRef(block, type));
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    BeatCraft.debug(String.format("onBlockPlace, event %s", event));
    ItemStack item = event.getItemInHand();
    Block block = event.getBlockPlaced();
    BlockRef ref = submitBlock(block, item);
    if (ref == null) return;
    ref.orientation = event.getBlockAgainst().getFace(block);
    BeatCraft.debug(String.format("placing with orientation: %s", ref.orientation));
    onPlace.accept(ref);
  }

  @EventHandler
  public void onBlockPhysics(BlockPhysicsEvent event) {
    if (!event.getChangedType().equals(Material.NOTE_BLOCK)) return;
    BeatCraft.debug(String.format("onBlockPhysics called on note block", event));
    onUpdate.accept(event.getBlock());
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    BeatCraft.debug(String.format("onBlockBreak, event %s", event));
    BlockRef ref = submitBlock(event);
    if (ref == null) return;
    onBreak.accept(ref);
    event.setDropItems(false);
  }

  @EventHandler
  public void onBlockDamage(BlockDamageEvent event) {
    BlockRef ref = submitBlock(event);
    if (ref == null) return;
    onPunch.accept(ref);
  }

  @EventHandler
  public void onWorldSave(WorldSaveEvent event) {
    onSave.run();
  }

  // 
  // helpers

  private BlockRef submitBlock(BlockEvent event) { return submitBlock(event.getBlock()); }
  private BlockRef submitBlock(Block block) { return submitBlock(block, null); }
  private BlockRef submitBlock(Block block, ItemStack item) {
    String type = item == null ? BeatBlock.getType(block) : BeatBlock.getType(item);
    if (type == null) return null;
    return new BlockRef(block, type);
  }
}
