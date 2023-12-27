package dev.leonk.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import dev.leonk.BeatCraft;
import dev.leonk.Sequencer;
import dev.leonk.Send;

public class BlockManager {
  private BlockListener blockUpdates;
  private BlockStore blockStorage;
  private Set<BeatBlock> blocks;

  static {
    BeatCraft.todo.add("stop sequencers when connections are broken");
  }

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
    server.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0, 4);
  }

  public void tick() {
    for (BeatBlock block : blocks) {
      block.tick();
    }
  }

  private void forwardSignal(Block block, BlockFace direction) {
    BeatBlock beat = find(block);
    if (beat == null) return;
    beat.receiveSignal(direction);
  }

  public static Map<BlockFace, Block> searchCross(Block block, int maxDistance) {
    Map<BlockFace, Block> matches = new HashMap<>();
    for (BlockFace direction : directions()) {
      Block found = searchDirection(block, direction, maxDistance);
      if (found == null) continue;
      matches.put(direction, found);
    }
    return matches;
  }

  public static Block searchDirection(Block block, BlockFace direction, int distance) {
    if (distance < 1) return null;
    Block neighbor = block.getRelative(direction);
    if (BeatBlock.interruptSignal(neighbor)) return block;
    return searchDirection(neighbor, direction, distance - 1);
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
      case "Sequencer": beat = new Sequencer(block, this::forwardSignal); break;
      case "Send": beat = new Send(block,this::forwardSignal); break;
      default:
        BeatCraft.debug(String.format("unknown transmutation: %s", type));
        return;
    }
    blocks.add(beat);
  }

  private void destroy(Block block, String type) {
    BeatCraft.debug(String.format("breaking %s", type));
    BeatBlock removedBeat = find(block);
    if (!blocks.remove(removedBeat)) return;
    dropItem(type, block.getLocation());
    block.removeMetadata(BeatBlock.BASE_TYPE, BeatCraft.plugin);
    // disconnect any sequencers that may have been connected
    for (BeatBlock beat : blocks) {
      if (beat instanceof Sequencer) {
        Sequencer sequencer = (Sequencer) beat;
        sequencer.disconnect(removedBeat);
      }
    }
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

  private BeatBlock find(Block block) {
    for (BeatBlock beat : blocks) {
      if (beat.getBlock().equals(block)) {
        return beat;
      }
    }
    return null;
  }

  public static Set<BlockFace> directions() {
    return new HashSet<BlockFace>() {{
      add(BlockFace.EAST);
      add(BlockFace.WEST);
      add(BlockFace.UP);
      add(BlockFace.DOWN);
      add(BlockFace.NORTH);
      add(BlockFace.SOUTH);
    }};
  }
}
