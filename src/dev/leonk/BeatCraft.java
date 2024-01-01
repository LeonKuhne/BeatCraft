package dev.leonk;

import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import dev.leonk.blocks.BlockManager;

public class BeatCraft extends JavaPlugin {

  public static Logger log;
  public static JavaPlugin plugin;
  public static BlockManager blockManager;

  // 
  // lifecycle

  @Override
  public void onLoad() {
    plugin = this;
    log = getLogger();
    blockManager = new BlockManager();
  }

  @Override
  public void onEnable() {
    blockManager.register(this);
  }

  @Override
  public void onDisable() {
    blockManager.saveWorld();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length == 0) return false;
    switch (args[0]) {
      // clear the graph
      case "clear":
        BeatCraft.debug("clearing sound networks");
        blockManager.graph.groups.clear();
        return true;

      // show graph
      case "graph":
        BeatCraft.debug(String.format("Graph:\n%s", blockManager.graph));
        return true;

      // reset plugin
      case "reset":
        BeatCraft.debug("resetting blocks");
        blockManager = new BlockManager();
        return true;

      // get the code of a string
      case "code":
        if (args.length < 2) return false;
        BeatCraft.debug(String.format("code of %s is %s", args[1], args[1].hashCode() % 1000));
    }
    return false;
  }

  // 
  // helpers

  public static void debug(String string) {
    log.info(string);
    for (Player player : plugin.getServer().getOnlinePlayers()) {
      player.sendMessage(string);
    }
  }
}
