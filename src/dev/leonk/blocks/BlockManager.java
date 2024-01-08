package dev.leonk.blocks;

import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import dev.leonk.BeatCraft;
import dev.leonk.Sequencer;
import dev.leonk.Speaker;
import dev.leonk.blocks.graph.Graph;
import dev.leonk.blocks.graph.Node;
import dev.leonk.Send;

public class BlockManager {
  public Graph graph;
  private BlockListener blockUpdates;
  private BlockStore blockStorage;

  public BlockManager() {
    blockUpdates = new BlockListener(
      this::transmute, 
      this::destroy, 
      this::triggerSignal, 
      this::interact,
      this::update,
      this::saveWorld);
    blockStorage = new BlockStore(this::transmute);
    graph = new Graph();
  }

  public void register(Plugin plugin) {
    Server server = plugin.getServer();
    server.addRecipe(Send.getRecipe());
    server.addRecipe(Sequencer.getRecipe());
    server.addRecipe(Speaker.getRecipe());
    blockStorage.load();
    server.getPluginManager().registerEvents(blockUpdates, plugin);
    server.getScheduler().scheduleSyncRepeatingTask(plugin, graph::propogate, 0, 4);
    //server.getScheduler().scheduleSyncRepeatingTask(plugin, graph::inspect, 0, 10);
  }

  private void triggerSignal(BlockRef ref) {
    Node node = graph.find(ref.block);
    if (node == null) return;
    BeatCraft.debug(String.format("triggering %s", node));
    graph.trigger(node);
  }

  public void saveWorld() {
    blockStorage.save(graph.beats());
  }

  //
  // world actions

  private void transmute(BlockRef ref) {
    if (graph.find(ref.block) != null) return;
    BeatBlock beat;
    BeatCraft.debug(String.format("transmuting %s", ref.type));
    switch (ref.type) {
      case "Sequencer": beat = new Sequencer(ref.block); break;
      case "Send": beat = new Send(ref.block, ref.orientation); break;
      case "Speaker": beat = new Speaker(ref.block); break;
      default:
        BeatCraft.debug(String.format("unknown transmutation: %s", ref.type));
        return;
    }
    BeatCraft.debug(String.format("graph state: %s", graph));
    graph.connect(beat);
  }

  private void update(Block block) {
    Node node = graph.find(block);
    if (node == null) return;
    update(node.beat);
  }
  private void update(BeatBlock beat) {
    BeatCraft.debug(String.format("updating type: %s", beat.type));
    if (beat instanceof Sequencer) {
      update((Sequencer) beat);
    } else {
      BeatCraft.debug("rerendering beat on update");
      beat.rerender();
    }
  }
  private void update(Sequencer sequencer) {
    BeatCraft.debug(String.format("updating sequencer %s", sequencer));
    Instrument prev = sequencer.instrument;
    NoteBlock noteBlock = (NoteBlock) sequencer.block.getBlockData();
    Instrument next = noteBlock.getInstrument();
    if (next == Instrument.CUSTOM_HEAD) return;
    sequencer.instrument = next;
    BeatCraft.debug(String.format("updating sequencers instrument %s -> %s", prev, sequencer.instrument));
    sequencer.rerender();
  }

  private void destroy(BlockRef ref) {
    BeatCraft.debug(String.format("breaking %s", ref.type));
    Node node = graph.find(ref.block);
    if (node == null) return;
    BeatCraft.debug(String.format("breaking %s", node));
    dropItem(ref.type, ref.block.getLocation());
    graph.disconnect(node);
    ref.block.removeMetadata(BeatBlock.BASE_TYPE, BeatCraft.plugin);
  }

  private void interact(BlockRef ref) {
    if (ref.type != Sequencer.BASE_NAME) return;
    BeatCraft.debug(String.format("interacting with %s", ref.type));
    Node node = graph.find(ref.block);
    if (node == null) return;
    Sequencer sequencer = (Sequencer) node.beat;
    sequencer.changePitchBy(1);
  }

  private void dropItem(String type, Location pos) {
    ItemStack item;
    switch(type) {
      case "Sequencer": 
        item = Sequencer.getItem(1); 
        break;
      case "Send": 
        item = Send.getItem(1); 
        break;
      case "Speaker": 
        item = Speaker.getItem(1); 
        break;
      default:
        BeatCraft.debug(String.format("unknown block broken: %s", type));
        return;
    }
    pos.getWorld().dropItemNaturally(pos, item);
  }
}