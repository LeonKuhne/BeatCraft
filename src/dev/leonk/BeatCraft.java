package dev.leonk;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.leonk.blocks.BlockListener;
import dev.leonk.blocks.BlockStore;

public class BeatCraft extends JavaPlugin {

  public static Logger log;
  public static JavaPlugin plugin;
  public static List<String> todo = new ArrayList<>();
  private BlockStore blocks;
  private BlockListener blockUpdates;

  // 
  // lifecycle

  @Override
  public void onLoad() {
    plugin = this;
    log = getLogger();
    blocks = new BlockStore();
    blockUpdates = new BlockListener(
      block -> blocks.add(block), 
      block -> blocks.remove(block),
      () -> blocks.save()
    );
  }

  @Override
  public void onEnable() {
    Server server = getServer();
    blocks.load();
    server.addRecipe(Sequencer.getRecipe());
    server.getPluginManager().registerEvents(blockUpdates, this);
    server.getScheduler().scheduleSyncRepeatingTask(this, () -> {
      blocks.tick();
    }, 0, 1);
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

  public static void debug(String string) {
    log.info(string);
    for (Player player : plugin.getServer().getOnlinePlayers()) {
      player.sendMessage(string);
    }
  }
}
