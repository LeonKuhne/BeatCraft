package dev.leonk.blocks.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;
import dev.leonk.BeatCraft;
import dev.leonk.blocks.BeatBlock;

public class Graph {
  public Set<Group> groups;
  private Set<Edge> edges;
  public static double inspectSpeed = 0.1;

  public Graph() {
    groups = new HashSet<>();
    edges = new HashSet<>();
  }

  public void inspect() {
    for (Edge edge : this.edges) {
      spawnParticle(edge);
    }
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

  public void connect(BeatBlock beat) { connect(new Node(beat)); }
  private void connect(Node node) {
    Map<BlockFace, Edge> connections = sameAxis(node);
    if (connections.isEmpty()) {
      BeatCraft.debug(String.format("no connections for %s", node));
      insert(node);
      return;
    }
    // connect all edges / find involved groups
    Set<Group> involvedGroups = new HashSet<>();
    for (Edge edge : connections.values()) {
      edges.add(edge);
      edge.connect();
      involvedGroups.add(findGroup(edge.to));
    }
    // add node to merged group
    merge(involvedGroups).add(node);
    BeatCraft.debug(String.format("state after connect: %s", this));
  }

  private void insert(Node node) {
    Group group = new Group();
    group.add(node);
    groups.add(group);
  }

  public void disconnect(Node node) {
    // disconnect edges
    HashSet<Edge> toRemove = new HashSet<>(node.connections.values());
    for (Edge edge : toRemove) {
      edge.disconnect();
      edges.remove(edge);
    }
    // remove group
    remove(findGroup(node));
    // reconnect all edges
    for (Edge edge : toRemove) {
      connect(edge.to == node ? edge.from : edge.to);
    }
    BeatCraft.debug(String.format("state after disconnect: %s", this));
  }

  public Node find(Block block) {
    String type = BeatBlock.getType(block);
    if (type == null) return null;
    return find(n -> n.beat.getBlock().equals(block)); 
  }
  public Node find(Function<Node, Boolean> filter) {
    for (Node node : nodes()) {
      if (filter.apply(node)) {
        return node;
      }
    }
    return null;
  }

  private Group findGroup(Node node) { return findGroup(g -> g.contains(node)); } 
  private Group findGroup(Function<Group, Boolean> filter) {
    for (Group group : groups) if (filter.apply(group)) return group;
    return null;
  }

  public Set<BeatBlock> beats() {
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

  private void spawnParticle(Edge edge) {
      Block block = edge.from.beat.getBlock();
      World world = block.getWorld();
      Vector direction = edge.direction.getDirection();
      Location spawn = block.getLocation()
        .add(0.5, 0.5, 0.5)        // move to center
        .add(direction.multiply(0.5)); // move to face

      // spawn a snowball
      Snowball snowball = world.spawn(spawn, Snowball.class);
      snowball.setVelocity(direction.multiply(inspectSpeed));

      // spawn invisible armor stand
      ArmorStand armorStand = (ArmorStand) world.spawnEntity(spawn, EntityType.ARMOR_STAND);
      armorStand.setGravity(false);
      armorStand.setVisible(false);
      armorStand.setSmall(true);
      armorStand.setCollidable(false);
      armorStand.addPassenger(snowball);
  }
}
