package dev.leonk.blocks;

import java.io.File;
import java.util.Set;
import java.util.function.Consumer;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import dev.leonk.BeatCraft;
import org.bukkit.block.Block;

public class BlockStore { 
  private ConnectionSource db; 
  private Dao<BeatBlock, Integer> blockTable;
  private Consumer<BlockRef> onPlace;

  public BlockStore(Consumer<BlockRef> onPlace) {
    super();
    connect();
    this.onPlace = onPlace;
  }

  // 
  // db

  public void load() {
    try {
      BeatCraft.debug(String.format("loading %d blocks", blockTable.countOf()));
      for (BeatBlock beat : blockTable.queryForAll()) {
        Block block = BeatBlock.getBlockAt(beat);
        BlockRef ref = new BlockRef(block, beat.type);
        onPlace.accept(ref);
      }
    } catch (Exception e) {
      BeatCraft.debug("could not load blocks");
      e.printStackTrace();
    }
  }

  public void save(Set<BeatBlock> blocks) {
    try {
      BeatCraft.debug(String.format("saving %d blocks", blocks.size()));
      TableUtils.clearTable(db, BeatBlock.class);
      for (BeatBlock block : blocks) {
        blockTable.create(block);
      }
    } catch (Exception e) {
      BeatCraft.debug("could not save blocks");
      e.printStackTrace();
    }
  }

  private void connect() {
    try {
      File path = BeatCraft.plugin.getDataFolder();
      // ensure that the data folder exists
      path.mkdirs();
      // connect to the database
      db = new JdbcConnectionSource(String.format("jdbc:sqlite:%s/%s.db", path, "beat-blocks"));
      blockTable = DaoManager.createDao(db, BeatBlock.class);
      TableUtils.createTableIfNotExists(db, BeatBlock.class);
      // print table contents
      BeatCraft.debug(String.format("loaded %d blocks:", blockTable.countOf()));
      for (BeatBlock block : blockTable.queryForAll()) {
        BeatCraft.debug(String.format("- loaded %s @ (%d, %d, %d) in %s", block.type, block.x, block.y, block.z, block.world));
      }
    } catch (Exception e) {
      BeatCraft.debug("could not connect to database");
      e.printStackTrace();
    }
  }
}
