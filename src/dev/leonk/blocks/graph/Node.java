package dev.leonk.blocks.graph;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.block.BlockFace;
import dev.leonk.blocks.BeatBlock;

public class Node {
  public BeatBlock beat;
  public Map<BlockFace, Edge> connections;
  public Node(BeatBlock beat) {
    this.beat = beat;
    connections = new HashMap<>();
  }
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("Node(%s)", beat));
    for (Edge edge : connections.values()) {
      builder.append(String.format("\n  > %s", edge));
    }
    return builder.toString();
  }
  @Override
  public boolean equals(Object other) {
    if (other == null) return false;
    if (!(other instanceof Node)) return false;
    Node otherNode = (Node) other;
    return otherNode.beat.equals(beat);
  }
}