package dev.leonk.blocks;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import dev.leonk.BeatCraft;
import dev.leonk.Sequencer;
import dev.leonk.blocks.BeatGraph.Node;
import dev.leonk.Send;

public class BlockManager {
  private BlockListener blockUpdates;
  private BlockStore blockStorage;
  private BeatGraph graph;

  public BlockManager() {
    blockUpdates = new BlockListener(this::transmute, this::destroy, this::saveWorld);
    blockStorage = new BlockStore(this::transmute);
    graph = new BeatGraph();
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
    graph.propogate();
  }

  public void saveWorld() {
    blockStorage.save(graph.blocks());
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
    BeatCraft.debug(String.format("graph state: %s", graph));
    graph.connect(beat);
  }

  private void destroy(Block block, String type) {
    BeatCraft.debug(String.format("breaking %s", type));
    Node node = graph.find(n -> n.beat.getBlock().equals(block));
    if (node == null) return;
    BeatCraft.debug(String.format("breaking %s", node));
    dropItem(type, block.getLocation());
    graph.disconnect(node);
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
}