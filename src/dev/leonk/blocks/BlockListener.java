package dev.leonk.blocks;

import java.util.function.BiConsumer;
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

  private BiConsumer<Block, String> onPlace;
  private BiConsumer<Block, String> onBreak;
  private BiConsumer<Block, String> onPunch;
  private BiConsumer<Block, String> onInteract;
  private Consumer<Block> onUpdate;
  private Runnable onSave;

  public BlockListener(
    BiConsumer<Block, String> onPlace, 
    BiConsumer<Block, String> onBreak, 
    BiConsumer<Block, String> onPunch, 
    BiConsumer<Block, String> onInteract,
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
    onInteract.accept(block, type);
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    BeatCraft.debug(String.format("onBlockPlace, event %s", event));
    ItemStack item = event.getItemInHand();
    Block block = event.getBlockPlaced();
    submitBlock(block, item, onPlace);
    //onUpdate.accept(block);
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
    if (submitBlock(event, onBreak)) event.setDropItems(false);
    //onUpdate.accept(event.getBlock());
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

  private boolean submitBlock(BlockEvent event, BiConsumer<Block, String> submit) {
    Block block = event.getBlock();
    return submitBlock(block, submit);
  }
  private boolean submitBlock(Block block, BiConsumer<Block, String> submit) {
    return submitBlock(block, null, submit);
  }
  private boolean submitBlock(Block block, ItemStack item, BiConsumer<Block, String> submit) {
    String type = item == null ? BeatBlock.getType(block) : BeatBlock.getType(item);
    if (type == null) return false;
    submit.accept(block, type);
    return true;
  }
}
