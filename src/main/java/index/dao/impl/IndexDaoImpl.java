package index.dao.impl;

import base.md.MdIndex;
import index.dao.IndexDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class IndexDaoImpl implements IndexDao {
    private static Logger logger = LoggerFactory.getLogger("IndexDaoImpl");

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
