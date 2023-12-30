package dev.leonk;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import dev.leonk.blocks.BeatBlock;

public class Speaker extends BeatBlock {
  public static String BASE_NAME = "Speaker";

  public Speaker(Block block) { super(block, BASE_NAME); }

  public static ItemStack getItem(int amount) {
    return getItem("Speaker", "play audio signals", Material.NOTE_BLOCK, amount);
  }

  public static ItemStack craftShapeless(Set<ItemStack> ingredients) {
    return uncraft(ingredients, BASE_NAME, new ItemStack(Material.NOTE_BLOCK, 8));
  }

  public static ItemStack craftShaped(ItemStack[] ingredients) {
    Map<String, Material> map = new HashMap<String, Material>() {{
      put("#", Material.NOTE_BLOCK);
      put("u", Material.BUCKET);
    }};

    // 8 note blocks surrounding 1 bucket
    if (BeatBlock.recipeMatch("####u####", ingredients, map)) return getItem(1);
    return null;
  }
}
