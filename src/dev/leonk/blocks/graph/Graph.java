package dev.leonk.blocks.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import dev.leonk.BeatCraft;
import dev.leonk.blocks.BeatBlock;

public class Graph {
  public Set<Group> groups;

  public Graph() {
    groups = new HashSet<>();
  }

  public void trigger(Node node) {
    for (Group group : groups) {
      if (!group.contains(node)) continue;
      group.trigger(node);
      return;
    }
  }

  public void propogate() {
    for (Group group : groups) {
      group.propogate();
      if (group.state.isEmpty()) continue;
      BeatCraft.debug(String.format("state: %s", group.state));
    }
  }

  public void connect(BeatBlock beat) {
    Node node = new Node(beat);
    Map<BlockFace, Edge> connections = sameAxis(node);
    // connect all edges / find involved groups
    Set<Group> involvedGroups = new HashSet<>();
    for (Edge edge : connections.values()) {
      edge.connect();
      involvedGroups.add(findGroup(edge.to));
    }
    // add node to merged group
    merge(involvedGroups).add(node);
    BeatCraft.debug(String.format("state after connect: %s", this));
  }

  public void disconnect(Node node) {
    // disconnect all edges
    for (Edge edge : node.connections.values()) edge.disconnect();

    // remove node's group
    remove(findGroup(node));

    for (Edge edge : node.connections.values()) {
      // connect edges passing back
      Edge edgePassingBack = sameAxis(edge.to).get(edge.direction.getOppositeFace());
      if (edgePassingBack == null) continue;
      edgePassingBack.connect();
      // collect new groups
      Group newGroup = new Group();
      for (Node newNode : Group.collectNodes(edge.to)) newGroup.add(newNode);
      groups.add(newGroup);
    }

    BeatCraft.debug(String.format("state after disconnect: %s", this));
  }

  public BeatBlock find(Block block) { 
    Node node = find(n -> n.beat.getBlock().equals(block)); 
    if (node == null) return null;
    return node.beat;
  }
  public Node find(Function<Node, Boolean> filter) {
    for (Node node : nodes()) if (filter.apply(node)) return node;
    return null;
  }

  private Group findGroup(Node node) { return findGroup(g -> g.contains(node)); } 
  private Group findGroup(Function<Group, Boolean> filter) {
    for (Group group : groups) if (filter.apply(group)) return group;
    return null;
  }

  public Set<BeatBlock> blocks() {
    Set<BeatBlock> blocks = new HashSet<>();
    for (Node node : nodes()) blocks.add(node.beat);
    return blocks;
  }

  private Set<Node> nodes() {
    Set<Node> nodes = new HashSet<>();
    for (Set<Node> group : this.groups) nodes.addAll(group);
    return nodes;
  }

  private Set<Node> each(Node sourceNode, Function<Node, Boolean> filter) {
    Set<Node> collected = new HashSet<>();
    for (Node targetNode : nodes()) {
      if (filter.apply(targetNode)) collected.add(targetNode);
    } 
    return collected;
  }

  private Map<BlockFace, Edge> sameAxis(Node node) {
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
      BlockFace direction = direction(block, targetNode.beat.getBlock());
      Edge edge = new Edge(node, targetNode, direction);
      Edge closestEdge = closest.get(edge.direction);
      if (closestEdge != null && closestEdge.distance < edge.distance) continue;
      closest.put(edge.direction, edge);
      BeatCraft.debug(String.format("blocks on same axis %s", edge));
    }
    return closest;
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

  private Group merge(Set<Group> involvedGroups) {
    Group group;
    switch (involvedGroups.size()) {
      // no connections -> create a new group for it
      case 0:
        group = new Group();
        groups.add(group);
        break;
      // one connection -> add to the only group
      case 1:
        // add to group
        group = involvedGroups.iterator().next();
        break;
      // two or more connections -> merge groups
      default:
        // merge groups
        Iterator<Group> iterator = involvedGroups.iterator();
        group = iterator.next();
        while (iterator.hasNext()) {
          Group mergedGroup = iterator.next();
          group.merge(mergedGroup);
          groups.remove(mergedGroup);
        }
        break;
    }
    return group;
  }

  private void remove(Group target) {
    Set<Group> newGroups = new HashSet<>();
    for (Group group : groups) {
      if (group.equals(target)) continue;
      newGroups.add(group);
    }
    groups = newGroups;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("BeatGraph #%s:\n", hashCode()));
    for (Set<Node> group : groups) {
      builder.append(String.format("%s\n", group));
    }
    return builder.toString();
  }
}
