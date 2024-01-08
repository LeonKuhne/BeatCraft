package dev.leonk;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import dev.leonk.blocks.BeatBlock;
import dev.leonk.blocks.BlockManager;
import dev.leonk.blocks.graph.Graph;

public class BeatCraft extends JavaPlugin {

  public static Logger log;
  public static JavaPlugin plugin;
  public static BlockManager blockManager;
  private static Set<Player> debugPlayers;

  // 
  // lifecycle

  @Override
  public void onLoad() {
    plugin = this;
    log = getLogger();
    debugPlayers = new HashSet<>();
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
        BeatCraft.debug(blockManager.graph.toString());
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
        return true;

      // get the code of a string
      case "inspectSpeed":
        if (args.length < 2) return false;
        try {
          Graph.inspectSpeed = Double.parseDouble(args[1]);
          BeatCraft.debug(String.format("inspectSpeed set to %s", Graph.inspectSpeed));
        } catch (NumberFormatException e) {
          BeatCraft.debug(String.format("inspectSpeed must be a number, got %s", args[1]));
        }
        return true;

      case "inspect":
        BeatCraft.debug("inspecting graph");
        for (BeatBlock beat : blockManager.graph.beats()) debug(beat.toString());
        return true;

      case "rerender":
        for (BeatBlock beat : blockManager.graph.beats()) beat.rerender();
        return true;

      case "debug":
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (debugPlayers.contains(player)) {
          player.sendMessage("debug mode disabled");
          debugPlayers.remove(player);
        } else {
          player.sendMessage("debug mode enabled");
          debugPlayers.add(player);
        }
        return true;
    }
    return false;
  }

  // 
  // helpers

  public static void debug(String string) {
    log.info(string);
    for (Player player : debugPlayers) {
      player.sendMessage(string);
    }
  }
}
