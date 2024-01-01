package dev.leonk.blocks;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import dev.leonk.BeatCraft;

public class BlockListener implements Listener {

  private BiConsumer<Block, String> onPlace;
  private BiConsumer<Block, String> onBreak;
  private BiConsumer<Block, String> onPunch;
  private Function<ItemStack[], ItemStack> onCraft;
  private Runnable onSave;

  public BlockListener(
    BiConsumer<Block, String> onPlace, 
    BiConsumer<Block, String> onBreak, 
    BiConsumer<Block, String> onPunch, 
    Function<ItemStack[], ItemStack> onCraft, 
    Runnable onSave
  ) {
    this.onPlace = onPlace;
    this.onBreak = onBreak;
    this.onPunch = onPunch;
    this.onCraft = onCraft;
    this.onSave = onSave;
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
  public void onCraft(PrepareItemCraftEvent event) {
    if (event.getRecipe() != null) return;
    CraftingInventory inv = event.getInventory();
    ItemStack result = onCraft.apply(inv.getMatrix());
    if (result == null) return;
    inv.setResult(result);
  }

  @EventHandler
  public void onCraftResult(CraftItemEvent event) {
    if (event.getRecipe() == null) return;
    if (BeatBlock.getType(event.getCurrentItem()) == null) return;
    // subtract one from all ingredients
    CraftingInventory inv = event.getInventory();
    for (ItemStack ingredient : inv.getMatrix()) {
      if (ingredient == null) continue;
      if (ingredient.getAmount() == 0) continue;
      ingredient.setAmount(ingredient.getAmount() - 1);
    }
    //event.setCancelled(true);
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
