package dev.leonk.blocks;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import dev.leonk.BeatCraft;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Texture extends MapRenderer {
  private final BufferedImage texture;

  public Texture(String type) {
    this.texture = loadTexture(type);
  }

  private BufferedImage loadTexture(String type) {
    try {
      // generate a texture using a canvas and a circle
      return ImageIO.read(BeatCraft.plugin.getClass().getResourceAsStream("/sequencer.png"));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void render(MapView map, MapCanvas canvas, Player player) {
    canvas.drawImage(0, 0, texture);
  }
}
