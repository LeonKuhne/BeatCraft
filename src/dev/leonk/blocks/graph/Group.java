package dev.leonk.blocks.graph;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.block.Block;
import dev.leonk.BeatCraft;
import dev.leonk.Send;
import dev.leonk.Sequencer;
import dev.leonk.Speaker;
import dev.leonk.blocks.BeatBlock;

public class Group extends HashSet<Node> {

  public Set<Edge> state;
  private HashSet<Speaker> speakers;
  private HashSet<Sequencer> sequencers;
  
  public Group() {
    super();
    state = new HashSet<>();
    speakers = new HashSet<>();
    sequencers = new HashSet<>();
  }

  @Override
  public boolean add(Node node) {
    // track types
    if (node.beat instanceof Speaker) {
      speakers.add((Speaker) node.beat);
    } else if (node.beat instanceof Sequencer) {
      sequencers.add((Sequencer) node.beat);
    }
    // add to group
    return super.add(node);
  }

  @Override
  public boolean remove(Object obj) {
    if (!(obj instanceof Node)) return false;
    Node node = (Node) obj;

    // remove from state
    // NOTE: you could transfer the signal if the edge has been replaced
    state.removeIf(edge -> edge.to.equals(node));

    // track types
    if (node instanceof Node) {
      Node nodeCast = (Node) node;
      if (nodeCast.beat instanceof Speaker) {
        speakers.remove((Speaker) nodeCast.beat);
      } else if (nodeCast.beat instanceof Sequencer) {
        sequencers.remove((Sequencer) nodeCast.beat);
      }
    }

    // remove from group
    return super.remove(node);
  }

  public void merge(Group mergedGroup) {
    for (Node node : mergedGroup) this.add(node);
    state.addAll(mergedGroup.state);
  }

  public void trigger(Node node) {
    // decide if you should add-to or replace the state?
    for (Edge edge : node.connections.values()) {
      state.add(edge.clone());
    }
  }

  public void propogate() {
    Set<Edge> nextState = new HashSet<>();
    if (state.size() == 0) return;
    BeatCraft.debug(this.toString());

    // propogate each edge
    for (Edge edge : state) {
      nextState.addAll(propogate(edge));
    }

    // find active sequencers
    Set<Runnable> playback = new HashSet<>();
    for (Edge edge : nextState) {
      if (edge.from.beat instanceof Sequencer) {
        Sequencer sequencer = (Sequencer) edge.from.beat;
        Block cursor = edge.cursor();
        Location playAt;
        if (speakers.isEmpty()) {
          playback.add(() -> sequencer.play(cursor, cursor.getLocation()));
        } else {
          for (Speaker speaker : speakers) {
            Block speakerBlock = speaker.getBlock();
            if (speakerBlock.equals(cursor)) {
              playAt = speakerBlock.getLocation();
              playback.add(() -> {
                sequencer.play(cursor, playAt);
                // spawn a particle above the speaker
                BeatBlock.noteParticle(sequencer.getNote(cursor).getNote(), playAt);
              });
              break;
            }
          }
        } 
      }
    }

    // play notes (NOTE try running this async)
    for (Runnable playNote : playback) {
      playNote.run();
    }

    state = nextState;
  }

  public Set<Edge> propogate(Edge edge) { return propogate(edge, new HashSet<>()); }
  public Set<Edge> propogate(Edge edge, Set<Edge> visited) {
    if (visited.contains(edge)) return new HashSet<>();
    visited.add(edge);

    // activate if signal arrived
    Set<Edge> activated = new HashSet<>();
    if (edge.distance == 1 || edge.from.beat instanceof Send) {
      edge.to.beat.trigger();

      // forward signal if send
      for (Edge nextEdge : edge.to.connections.values()) {
        // dont return signal
        if (nextEdge.to.equals(edge.from)) continue;
        // add connections
        activated.addAll(propogate(nextEdge.clone(), visited));
      }
      return activated;
    }

    // decrease the distance
    edge.distance--;
    edge.from.beat.stimulate(edge);

    activated.add(edge);
    return activated;
  }

  public static Set<Node> collectNodes(Node node) { return collectNodes(node, n -> true); }
  public static Set<Node> collectNodes(Node node, Function<Node, Boolean> filter) { 
    Set<Edge> edges = collectNext(node, filter, new HashSet<>()); 
    return new HashSet<Node>() {{ for (Edge edge : edges) add(edge.to); }};
  }
  public static Set<Edge> collectNext(Node node, Function<Node, Boolean> filter) { return collectNext(node, filter, new HashSet<>()); }
  public static Set<Edge> collectNext(Node node, Function<Node, Boolean> filter, Set<Node> visited) {
    if (visited.contains(node) || !(filter.apply(node))) return new HashSet<>();
    visited.add(node);
    // collect new edges
    Set<Edge> collected = new HashSet<>();
    for (Edge edge : node.connections.values()) {
      collected.add(edge);
      collected.addAll(collectNext(edge.to, filter, visited));
    }
    return collected;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("Group(%d): ", this.size()));
    for (Node node : this) {
      builder.append(String.format("- %s, ", node));
    }
    return builder.toString();
  }
}
