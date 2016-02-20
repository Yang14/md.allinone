package index.dao.impl;

import base.md.MdIndex;
import index.dao.IndexDao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class IndexDaoImpl implements IndexDao {
    private static Logger logger = Logger.getLogger("IndexDaoImpl");

    private final Map<String, MdIndex> indexMap = new ConcurrentHashMap<String, MdIndex>();

    public Map<String, MdIndex> getIndexMap() {
        return indexMap;
    }

    @Override
    public boolean insertMdIndex(String key, MdIndex mdIndex) {
        return indexMap.put(key, mdIndex) == null ? true : false;
    }

    @Override
    public MdIndex findMdIndex(String key) {
        return indexMap.get(key);
    }

    @Override
    public boolean removeMdIndex(String key) {
        return indexMap.remove(key) == null ? true : false;
    }
}
