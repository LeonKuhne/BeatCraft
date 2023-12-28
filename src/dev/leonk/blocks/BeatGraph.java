package dev.leonk.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import dev.leonk.BeatCraft;
import dev.leonk.Send;
import dev.leonk.Sequencer;

public class BeatGraph {

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
        builder.append(String.format("\n- %s", edge));
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

  public class Edge {
    public Node from;
    public Node to;
    public BlockFace direction;
    public int distance;

    public Edge(Node from, Node to, BlockFace direction) {
      this(from, to, direction,
        (int) from.beat.getBlock().getLocation().distance(to.beat.getBlock().getLocation())
      );
    }
    public Edge(Node from, Node to, BlockFace direction, int distance) {
      this.from = from;
      this.to = to;
      this.direction = direction;
      this.distance = distance;
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
    public Edge reverse() { return new Edge(to, from, direction.getOppositeFace(), distance); }
    public Block cursor() { return to.beat.getBlock().getRelative(direction.getOppositeFace(), distance); }
    public Edge clone() { return new Edge(from, to, direction, distance); }
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
  }
  
  public Set<Node> nodes;
  public Set<Edge> state;

  public BeatGraph() {
    nodes = new HashSet<>();
    state = new HashSet<>();
  }

  public void trigger(Node node) {
    // decide if you should add-to or replace the state?
    state.addAll(activate(node));
  }

  public void propogate() {
    Set<Edge> nextState = new HashSet<>();
    BeatCraft.debug(String.format("propogating %d edges", state.size()));
    for (Edge edge : state) {
      nextState.addAll(propogate(edge));
    }
    state = nextState;
  }

  public Set<Edge> propogate(Edge edge) {
    BeatCraft.debug(String.format("propogating %s", edge));
    // activate if signal arrived
    if (edge.distance == 1 || edge.from.beat instanceof Send) {
      return activate(edge);
    }
      // decrease the distance
    edge.distance--;
    edge.from.beat.stimulate(edge);
    return new HashSet<Edge>() {{ add(edge); }};
  }

  public void connect(BeatBlock newBlock) {
    Node newNode = new Node(newBlock);
    nodes.add(newNode);
    Map<BlockFace, Edge> connections = sameAxis(newNode);
    for (Edge edge : connections.values()) {
      edge.connect();
    }
  }

  public void disconnect(Node node) {
    BeatCraft.debug(String.format("disconnecing %s", node));
    // for the connection's connections opposite direction, remove this node as a connection
    nodes.remove(node);
    for (Edge edge : node.connections.values()) {
      // remove connections from nodes
      edge.disconnect();
      // search for next best edge 
      Edge edgePassingBack = sameAxis(edge.to).get(edge.direction.getOppositeFace());
      if (edgePassingBack == null) continue;
      edgePassingBack.connect();
    }
  }

  public Map<BlockFace, Edge> sameAxis(Node node) {
    Block block = node.beat.getBlock();
    // find blocks on axis
    Set<Node> aligned = each(node, otherNode -> {
      Block other = otherNode.beat.getBlock();
      int alignedAxis = 0;
      if (block.getX() == other.getX()) alignedAxis++; 
      if (block.getY() == other.getY()) alignedAxis++;
      if (block.getZ() == other.getZ()) alignedAxis++;
      return alignedAxis == 2;
    });

    // find shortest connections
    Map<BlockFace, Edge> closest = new HashMap<>();
    for (Node targetNode : aligned) {
      BlockFace direction = direction(block, node.beat.getBlock());
      Edge edge = new Edge(node, targetNode, direction);
      Edge closestEdge = closest.get(edge.direction);
      if (closestEdge != null && closestEdge.distance < edge.distance) continue;
      closest.put(edge.direction, edge);
      BeatCraft.debug(String.format("blocks on same axis %s", edge));
    }
    return closest;
  }

  public Set<Node> each(Node sourceNode, Function<Node, Boolean> filter) {
    Set<Node> collected = new HashSet<>();
    for (Node targetNode : nodes) {
      if (filter.apply(targetNode)) collected.add(targetNode);
    } 
    return collected;
  }

  public Node find(Function<Node, Boolean> filter) {
    for (Node node : nodes) {
      if (filter.apply(node)) return node;
    }
    return null;
  }

  public Set<Edge> activate(Edge edge) { 
    Set<Edge> connections = activate(edge.to); 
    connections.remove(edge.reverse());
    return connections;
  }
  public Set<Edge> activate(Node node) { 
    BeatBlock beat = node.beat;
    beat.trigger(node);
    Set<Edge> activated = new HashSet<>();
    // activate sequencer
    if (beat instanceof Sequencer) {
      for (Edge edge : node.connections.values()) activated.add(edge.clone());
    // activate send
    } else if (beat instanceof Send) {
      // find next edges
      for (Edge edge : collectNext(node, n -> !(n.beat instanceof Send))) {
        // tick next edges
        activated.addAll(propogate(edge));
      }
    }
    return activated;
  }

  public Set<Edge> collectNext(Node node, Function<Node, Boolean> stopSearch) { return collectNext(node, stopSearch, new HashSet<>()); }
  public Set<Edge> collectNext(Node node, Function<Node, Boolean> stopSearch, Set<Node> visited) {
    if (visited.contains(node) || stopSearch.apply(node)) return new HashSet<>();
    visited.add(node);
    // collect new edges
    Set<Edge> collected = new HashSet<>();
    for (Edge edge : node.connections.values()) {
      collected.add(edge.clone());
      collected.addAll(collectNext(edge.to, stopSearch, visited));
    }
    return collected;
  }

  public Set<BeatBlock> blocks() {
    Set<BeatBlock> blocks = new HashSet<>();
    for (Node node : this.nodes) {
      blocks.add(node.beat);
    }
    return blocks;
  }

  public BeatBlock find(Block block) {
    return find(block, beat -> beat.getBlock().equals(block));
  }
  private BeatBlock find(Block block, Function<BeatBlock, Boolean> match) {
    for (BeatBlock beat : blocks()) {
      if (match.apply(beat)) {
        return beat;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("BeatGraph(\n");
    for (Node node : nodes) {
      builder.append(String.format("  %s\n", node));
    }
    builder.append(")");
    return builder.toString();
  }

  private BlockFace direction(Block source, Block target) {
    // assume two axis are zero
    if (source.getX() - target.getX() != 0) {
      return source.getX() - target.getX() > 0 ? BlockFace.WEST : BlockFace.EAST;
    } else if (source.getY() - target.getY() != 0) {
      return source.getY() - target.getY() > 0 ? BlockFace.DOWN : BlockFace.UP;
    } else {
      return source.getZ() - target.getZ() > 0 ? BlockFace.NORTH : BlockFace.SOUTH;
    }
  }
}
