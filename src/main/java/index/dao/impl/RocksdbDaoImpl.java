package index.dao.impl;

import base.md.MdIndex;
import com.alibaba.fastjson.JSON;
import index.dao.IndexDao;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Mr-yang on 16-2-21.
 */
public class RocksdbDaoImpl implements IndexDao {

    private static Logger logger = LoggerFactory.getLogger(RocksdbDaoImpl.class);

    public static final String DB_PATH = "/data/backend";
    public static Options options = new Options().setCreateIfMissing(true);
    public static RocksDB db = null;
    public static final String RDB_DECODE = "UTF8";

    static {
        RocksDB.loadLibrary();
        try {
            db = RocksDB.open(options, DB_PATH);
        } catch (RocksDBException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public boolean insertMdIndex(String key, MdIndex mdIndex) {
        try {
            db.put(key.getBytes(RDB_DECODE), JSON.toJSONString(mdIndex).getBytes());
            return true;
        } catch (Exception e) {
            logger.error(String.format("[ERROR] caught the unexpected exception -- %s\n", e));
        }
        return false;
    }

    @Override
    public MdIndex findMdIndex(String key) {
        try {
            byte[] indexBytes = db.get(key.getBytes(RDB_DECODE));
            if (indexBytes != null) {
                return JSON.parseObject(new String(indexBytes, RDB_DECODE), MdIndex.class);
            }
        } catch (Exception e) {
            logger.error(String.format("[ERROR] caught the unexpected exception -- %s\n", e));
        }
        return null;
    }

    @Override
    public boolean removeMdIndex(String key) {
        try {
            db.remove(key.getBytes());
            return true;
        } catch (Exception e) {
            logger.error(String.format("[ERROR] caught the unexpected exception -- %s\n", e));
        }
        return false;
    }

    @Override
    public Map<String, MdIndex> getIndexMap() {
        return null;
    }
}
