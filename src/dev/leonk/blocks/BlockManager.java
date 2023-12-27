package dev.leonk.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

  public static Map<BlockFace, Block> searchCross(Block block, int maxDistance, Function<Block, Boolean> match) {
    Map<BlockFace, Block> matches = new HashMap<>();
    for (BlockFace direction : directions()) {
      Block found = searchDirection(block, direction, maxDistance, match);
      if (found == null) continue;
      matches.put(direction, found);
    }
    return matches;
  }

  public static Block searchDirection(Block block, BlockFace direction, int distance, Function<Block, Boolean> match) {
    if (distance < 1) return null;
    Block neighbor = block.getRelative(direction);
    if (match.apply(neighbor)) return block;
    return searchDirection(neighbor, direction, distance - 1, match);
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
    block.removeMetadata(BeatBlock.BASE_TYPE, BeatCraft.plugin);
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

  private static Set<BlockFace> directions() {
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
