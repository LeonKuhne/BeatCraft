package dev.leonk.blocks;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import dev.leonk.BeatCraft;
import dev.leonk.Sequencer;
import dev.leonk.Send;

public class BlockManager {
  
  private BlockListener blockUpdates;
  private BlockStore blockStorage;
  private Set<BeatBlock> blocks;

  public BlockManager() {
    blockUpdates = new BlockListener(this::transmute, this::destroy, this::saveWorld);
    blockStorage = new BlockStore(this::transmute);
    blocks = new HashSet<>();
  }

  public void register(Plugin plugin) {
    Server server = plugin.getServer();
    blockStorage.load();
    server.addRecipe(Sequencer.craftRecipe());
    server.addRecipe(Send.craftRecipe());
    server.getPluginManager().registerEvents(blockUpdates, plugin);
    server.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0, 15);
  }

  public void tick() {
    for (BeatBlock block : blocks) {
      block.tick();
    }
  }

  public void saveWorld() {
    blockStorage.save(blocks);
  }

  //
  // world actions

  private void transmute(Block block, String type) {
    BeatCraft.debug(String.format("transmuting %s", type));
    BeatBlock beat;
    switch (type) {
      case "Sequencer": beat = new Sequencer(block); break;
      case "Send": beat = new Send(block); break;
      default:
        BeatCraft.debug(String.format("unknown transmutation: %s", type));
        return;
    }
    blocks.add(beat);
  }

  private void destroy(Block block, String type) {
    BeatCraft.debug(String.format("breaking %s", type));
    if (!remove(block)) return;
    dropItem(type, block.getLocation());
  }

  private void dropItem(String type, Location pos) {
    ItemStack item;
    switch(type) {
      case "Sequencer": item = Sequencer.getItem(1); break;
      case "Send": item = Send.getItem(1); break;
      default:
        BeatCraft.debug(String.format("unknown block broken: %s", type));
        return;
    }
    pos.getWorld().dropItemNaturally(pos, item);
  }

  // 
  // helpers

  private boolean remove(Block block) {
    // find the block in the list and remove it
    for (BeatBlock beat : blocks) {
      if (beat.getBlock().equals(block)) {
        blocks.remove(beat);
        return true;
      }
    }
    return false;
  }
}
