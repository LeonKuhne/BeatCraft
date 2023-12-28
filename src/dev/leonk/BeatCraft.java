package dev.leonk;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import dev.leonk.blocks.BlockManager;

public class BeatCraft extends JavaPlugin {

  public static Logger log;
  public static JavaPlugin plugin;
  public static List<String> todo;
  public static BlockManager blockManager;

  // 
  // lifecycle

  @Override
  public void onLoad() {
    plugin = this;
    log = getLogger();
    blockManager = new BlockManager();
    todo = new ArrayList<>();

    todo.add("trigger signal on punch");
    todo.add("rotate sends to change their direction and make texture directional");
    todo.add("stop sequencers when connections are broken");
    todo.add("add delay based on the type of block that is put in the line");
    todo.add("change pitch based on the type of block that is put in the line (up or down)");
    todo.add("right click sequencer to change speed, indicate using color/pitch/something");
    todo.add("create special sequencers that only play on air and only delays through blocks");
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
    sender.sendMessage("Todo List:");
    if (cmd.getName().equalsIgnoreCase("todo")) {
      for (String item : todo) sender.sendMessage("- " + item);
    }
    return true;
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
