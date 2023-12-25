package dev.leonk;

import java.util.HashSet;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.bukkit.block.Block;

public class BlockStore extends HashSet<Block> { 
  private ConnectionSource db; 
  private Dao<StoredBlock, Integer> blockTable;

  public BlockStore() {
    super();
    connect();
    load();

    // TODO add block place/destroy listeners
  }

  public void save() {
    try {
      for (Block block : this) {
        blockTable.create(new StoredBlock(block));
      }
    } catch (Exception e) {
      BeatCraft.log.warning("could not save blocks");
      e.printStackTrace();
    }
  }

  private void load() {
    try {
      for (StoredBlock storedBlock : blockTable.queryForAll()) {
        this.add(storedBlock.getBlock());
      }
    } catch (Exception e) {
      BeatCraft.log.warning("could not load blocks");
      e.printStackTrace();
    }
  }

  private void connect() {
    try {
      String url = "jdbc:sqlite:" + BeatCraft.plugin.getDataFolder() + "/blocks.db";
      db = new JdbcConnectionSource(url);
      blockTable = DaoManager.createDao(db, StoredBlock.class);
      TableUtils.createTableIfNotExists(db, StoredBlock.class);
    } catch (Exception e) {
      BeatCraft.log.warning("could not load blocks");
      e.printStackTrace();
    }
  }
}
