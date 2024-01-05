package dev.leonk.blocks.graph;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import dev.leonk.BeatCraft;

public class Edge {
  public Node from;
  public Node to;
  public BlockFace direction;
  public int distance;
  public int totalDistance;

  public Edge(Node from, Node to, BlockFace direction) {
    this(from, to, direction,
      (int) from.beat.getBlock().getLocation().distance(to.beat.getBlock().getLocation())
    );
  }
  public Edge(Node from, Node to, BlockFace direction, int distance) { this(from, to, direction, distance, distance); }
  public Edge(Node from, Node to, BlockFace direction, int distance, int totalDistance) {
    this.from = from;
    this.to = to;
    this.direction = direction;
    this.distance = distance;
    this.totalDistance = totalDistance;
  }
  public void connect() {
    from.connections.put(direction, this);
    to.connections.put(direction.getOppositeFace(), this.reverse());
    BeatCraft.debug(String.format("connected %s", this));
  }
  public void disconnect() {
    from.connections.remove(direction);
    to.connections.remove(direction.getOppositeFace());
  }
  public void reset() {
    this.distance = this.totalDistance;
  }
  public Edge reverse() { return new Edge(to, from, direction.getOppositeFace(), distance, totalDistance); }
  public Block cursor() { return to.beat.getBlock().getRelative(direction.getOppositeFace(), distance); }
  public Edge clone() { return new Edge(from, to, direction, distance, totalDistance); }
  @Override
  public String toString() { 
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("Edge(%d @ %s) %s --> %s", distance, direction, from.beat, to.beat));
    return builder.toString();
  }
  @Override
  public boolean equals(Object other) {
    if (other == null) return false;
    if (!(other instanceof Edge)) return false;
    Edge otherEdge = (Edge) other;
    return otherEdge.from.equals(from) && otherEdge.to.equals(to) && otherEdge.direction.equals(direction);
  }
  @Override 
  public int hashCode() {
    return from.hashCode() + to.hashCode() + direction.hashCode();
  }
}