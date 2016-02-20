package backend.service.impl;

import base.md.MdAttr;
import base.md.MdIndex;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mr-yang on 16-2-19.
 */
public class MdAttrListCacheTool {
    private static final Map<Long, List<MdAttr>> mdAttrMap = new ConcurrentHashMap<Long, List<MdAttr>>();

    public static List<MdAttr> getMdAttrListFromCache(Long dCode) {
        return mdAttrMap.get(dCode);
    }

    public static void setMdIndextToCache(Long dCode, List<MdAttr> mdIndex) {
        mdAttrMap.put(dCode, mdIndex);
    }

    public static void removeMdIndex(Long dCode){
        mdAttrMap.remove(dCode);
    }
}
