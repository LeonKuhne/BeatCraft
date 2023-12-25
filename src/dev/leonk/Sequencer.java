package dev.leonk;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Sequencer {
  static String itemName = Sequencer.class.getSimpleName();
  static NamespacedKey itemId = new NamespacedKey(BeatCraft.plugin, itemName); 

  static public Recipe getRecipe() {
    ItemStack item = getItem(1); 

    // define recipe
    ShapedRecipe recipe = new ShapedRecipe(itemId, item); 
    recipe.shape("###", "#*#", "###");
    recipe.setIngredient('#', Material.NOTE_BLOCK);
    recipe.setIngredient('*', Material.BEETROOT);
    return recipe;
  }

  static public ItemStack getItem(int amount) {
    ItemStack item = new ItemStack(Material.NOTE_BLOCK, amount);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.GOLD + itemName);
    meta.setLore(new ArrayList<String>() {{ add("right click to change speed"); }});
    meta.getPersistentDataContainer().set(itemId, PersistentDataType.STRING, itemName);
    item.setItemMeta(meta);
    return item;
  }

  static public void asSequencer(Block block) {
    block.setMetadata(itemName, new FixedMetadataValue(BeatCraft.plugin, true));
  }

  static public boolean isSequencer(ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    if (meta == null) return false;
    PersistentDataContainer metadata = meta.getPersistentDataContainer();
    if (!metadata.has(itemId, PersistentDataType.STRING)) return false;
    return metadata.get(itemId, PersistentDataType.STRING).equals(itemName);
  }

  static public boolean isSequencer(Block block) {
    return block.hasMetadata(itemName);
  }
}
