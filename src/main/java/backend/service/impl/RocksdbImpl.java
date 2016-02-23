package backend.service.impl;

import base.md.MdAttr;
import base.PortEnum;
import base.rmiapi.backend.BackendOpsService;
import com.alibaba.fastjson.JSON;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class RocksdbImpl extends UnicastRemoteObject implements BackendOpsService {

    private static Logger logger = LoggerFactory.getLogger("RocksdbImpl");

    public RocksdbImpl() throws RemoteException {
        super();
    }

    private static Jedis jedis = new Jedis("192.168.0.13", PortEnum.REDIS_PORT);

    private static final String DB_PATH = "/data/backend";
    private static Options options = new Options().setCreateIfMissing(true);
    private static RocksDB db = null;
    private static final String RDB_DECODE = "UTF8";

    static {
        RocksDB.loadLibrary();
        try {
            db = RocksDB.open(options, DB_PATH);
        } catch (RocksDBException e) {
            logger.error(e.getMessage());
        }
    }

    private Object setMonitor = new Object();
    private Object getMonitor = new Object();

    @Override
    public boolean insertMd(long dCode, String name, MdAttr mdAttr) throws RemoteException {
        MdAttrListCacheTool.removeMdIndex(dCode);
        synchronized (setMonitor) {
            jedis.rpush(String.valueOf(dCode), name);
        }
        return putMdAttr(buildKey(dCode, name), mdAttr);
    }

    @Override
    public MdAttr findFileMd(long dCode, String name) throws RemoteException {
        return getMdAttr(buildKey(dCode, name));
    }

    @Override
    public List<MdAttr> listDir(long dCode) throws RemoteException {
        List<MdAttr> mdAttrs = MdAttrListCacheTool.getMdAttrListFromCache(dCode);
        if (mdAttrs != null) {
            return mdAttrs;
        }
        List<String> nameList;
        synchronized (setMonitor) {
            long len = jedis.llen(String.valueOf(dCode));
            nameList = jedis.lrange(String.valueOf(dCode), 0, len);
        }
        mdAttrs = new ArrayList<MdAttr>();
        for (String name : nameList) {
            mdAttrs.add(findFileMd(dCode, name));
        }
        MdAttrListCacheTool.setMdIndextToCache(dCode, mdAttrs);
        return mdAttrs;
    }

    @Override
    public boolean renameMd(long dCode, String oldName, String newName) throws RemoteException {
        if (jedis.lrem(String.valueOf(dCode),0,oldName) == 0) {
            throw new IllegalArgumentException("no such fileName :" + oldName);
        }
        synchronized (setMonitor) {
            jedis.rpush(String.valueOf(dCode), newName);
        }
        MdAttr mdAttr = getMdAttr(buildKey(dCode, oldName));
        if (mdAttr == null) {
            throw new IllegalArgumentException("no such mdAttr:" + oldName);
        }
        mdAttr.setName(newName);
        putMdAttr(buildKey(dCode, newName), mdAttr);
        removeMdAttr(buildKey(dCode, oldName));
        return true;
    }

    @Override
    public Map<Long, ArrayList<String>> getNameMap() throws RemoteException {
        return null;
    }

    @Override
    public Map<String, MdAttr> getMdAttrMap() throws RemoteException {
        return null;
    }

    public boolean removeMdAttr(String key) {
        try {
            db.remove(key.getBytes());
            return true;
        } catch (Exception e) {
            logger.error(String.format("[ERROR] caught the unexpected exception -- %s\n", e));
        }
        return false;
    }

    private boolean putMdAttr(String key, MdAttr mdAttr) {
        try {
            db.put(key.getBytes(RDB_DECODE), JSON.toJSONString(mdAttr).getBytes(RDB_DECODE));
            return true;
        } catch (Exception e) {
            logger.error(String.format("[ERROR] caught the unexpected exception -- %s\n", e));
        }
        return false;
    }

    private MdAttr getMdAttr(String key) {
        try {
            byte[] attrBytes = db.get(key.getBytes(RDB_DECODE));
            if (attrBytes != null) {
                return JSON.parseObject(new String(attrBytes, RDB_DECODE), MdAttr.class);
            }
        } catch (Exception e) {
            logger.error(String.format("[ERROR] caught the unexpected exception -- %s\n", e));
        }
        return null;
    }

    public String buildKey(long dCode, String fileName) {
        return dCode + ":" + fileName;
    }
}
