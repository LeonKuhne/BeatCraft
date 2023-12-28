package dev.leonk.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
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
  private ArrayList<BiFunction<ItemStack[], Set<ItemStack>, ItemStack>> recipes;

  public BlockManager() {
    blockUpdates = new BlockListener(this::transmute, this::destroy, this::triggerSignal, this::craft, this::saveWorld);
    blockStorage = new BlockStore(this::transmute);
    graph = new BeatGraph();

    // arrange recipes
    recipes = new ArrayList<BiFunction<ItemStack[], Set<ItemStack>, ItemStack>>() {{
      add((matrix, items) -> Send.craftShaped(matrix));
      add((matrix, items) -> Send.craftShapeless(items));
      add((matrix, items) -> Sequencer.craftShaped(matrix));
      add((matrix, items) -> Sequencer.craftShapeless(items));
    }};
  }

  public void register(Plugin plugin) {
    Server server = plugin.getServer();
    blockStorage.load();
    server.getPluginManager().registerEvents(blockUpdates, plugin);
    server.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0, 4);
  }

  public void tick() {
    graph.propogate();
  }

  private void triggerSignal(Block block, String type) {
    Node node = graph.find(n -> n.beat.getBlock().equals(block));
    BeatCraft.debug(String.format("triggering %s", node));
    if (node == null) return;
    graph.trigger(node);
  }

  private ItemStack craft(ItemStack[] ingredients) {
    // assemble ingredients
    Set<ItemStack> unshapedIngredients = new HashSet<ItemStack>();
    for (ItemStack item : ingredients) if (item != null) unshapedIngredients.add(item);
    // apply recipes
    for (BiFunction<ItemStack[], Set<ItemStack>, ItemStack> recipe : recipes) {
      ItemStack result = recipe.apply(ingredients, unshapedIngredients);
      if (result != null) return result;
    }
    return null;
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