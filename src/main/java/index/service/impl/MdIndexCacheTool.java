package index.service.impl;

import base.md.MdIndex;
import index.dao.model.MdIndexV2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mr-yang on 16-2-19.
 */
public class MdIndexCacheTool {
    private static final Map<String, MdIndex> indexMap = new ConcurrentHashMap<String, MdIndex>();
    private static final Map<String, MdIndexV2> indexMapV2 = new ConcurrentHashMap<String, MdIndexV2>();

    public static MdIndex getMdIndexFromCache(String path) {
        return indexMap.get(path);
    }

    public static void setMdIndextToCache(String path, MdIndex mdIndex) {
        indexMap.put(path, mdIndex);
    }

    public static MdIndexV2 getMdIndexV2FromCache(String path) {
        return indexMapV2.get(path);
    }

    public static void setMdIndexV2tToCache(String path, MdIndexV2 mdIndexV2) {
        indexMapV2.put(path, mdIndexV2);
    }

    public static void removeMdIndex(String path){
        indexMap.remove(path);
    }
}
