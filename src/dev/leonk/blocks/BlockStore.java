package dev.leonk.blocks;

import java.io.File;
import java.util.HashSet;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import dev.leonk.BeatCraft;

import org.bukkit.Sound;
import org.bukkit.block.Block;

public class BlockStore extends HashSet<BeatBlock> { 
  private ConnectionSource db; 
  private Dao<BeatBlock, Integer> blockTable;

  public BlockStore() {
    super();
    connect();
  }

  public void tick() {
    for (BeatBlock beat : this) {
      // play a sound at the block
      beat.block.getWorld().playSound(beat.block.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
    }
  }

  //
  // accessors

  public boolean add(Block block) { return add(new BeatBlock(block)); }

  // 
  // db

  public void save() {
    try {
      BeatCraft.debug(String.format("saving %d blocks", size()));
      TableUtils.clearTable(db, BeatBlock.class);
      for (BeatBlock beat : this) {
        blockTable.create(beat);
      }
    } catch (Exception e) {
      BeatCraft.debug("could not save blocks");
      e.printStackTrace();
    }
  }

  public void load() {
    try {
      BeatCraft.debug(String.format("loading %d blocks", blockTable.countOf()));
      for (BeatBlock beat : blockTable.queryForAll()) {
        add(beat.init());
      }
    } catch (Exception e) {
      BeatCraft.debug("could not load blocks");
      e.printStackTrace();
    }
  }

  private void connect() {
    try {
      File path = BeatCraft.plugin.getDataFolder();
      // ensure that the data folder exists
      path.mkdirs();
      // connect to the database
      db = new JdbcConnectionSource("jdbc:sqlite:" + path + "/beatcraft.db");
      blockTable = DaoManager.createDao(db, BeatBlock.class);
      TableUtils.createTableIfNotExists(db, BeatBlock.class);
    } catch (Exception e) {
      BeatCraft.debug("could not connect");
      e.printStackTrace();
    }
  }

}
