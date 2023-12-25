package dev.leonk;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BeatCraft extends JavaPlugin {

  public static Logger log;
  public static JavaPlugin plugin;
  private BlockStore blocks;
  private List<String> todo;

  // 
  // lifecycle

  @Override
  public void onLoad() {
    plugin = this;
    log = getLogger();
    blocks = new BlockStore();


    Listener blockListener = new Listener() {
      @EventHandler
      public void onBlockPlace(BlockPlaceEvent event) {
        log.info("player interacted");
        ItemStack item = event.getItemInHand();
        // check if sequencer
        if (item == null || !Sequencer.isSequencer(item)) return;
        log.info("sequencer placed");
        event.getPlayer().sendMessage("placed sequencer");
        // add metadata to block
        Block block = event.getBlockPlaced();
        Sequencer.asSequencer(block);
        blocks.add(block);
      }
      @EventHandler
      public void onBlockBreak(BlockBreakEvent event) {
        log.info("player broke");
        Block block = event.getBlock();
        // check if sequencer
        if (!Sequencer.isSequencer(block)) return;
        log.info("sequencer broken");
        // add metadata to received item
        ItemStack item = Sequencer.getItem(1);
        event.getPlayer().getInventory().addItem(item);
      }
    };
    getServer().getPluginManager().registerEvents(blockListener, this);

    // todo
    todo.add("right click sequencer to change speed, indicate using color/pitch/something");
  }

  @Override
  public void onEnable() {
    // load sequencer
    BeatCraft.plugin.getServer().addRecipe(Sequencer.getRecipe());
  }

  @Override
  public void onDisable() {
    blocks.save();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    sender.sendMessage("Todo List:");
    if (cmd.getName().equalsIgnoreCase("todo")) {
      for (String item : todo) sender.sendMessage("- " + item);
    }
    return true;
  }
}
