package backend.service.impl;

import base.rmiapi.backend.BackendOpsService;
import base.md.MdAttr;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class BackendOpsServiceImpl extends UnicastRemoteObject implements BackendOpsService {

    private static Logger logger = Logger.getLogger("BackendOpsServiceImpl");

    private final Map<Long, ArrayList<String>> nameMap =
            new ConcurrentHashMap<Long, ArrayList<String>>();

    private final Map<String, MdAttr> mdAttrMap =
            new ConcurrentHashMap<String, MdAttr>();

    public BackendOpsServiceImpl() throws RemoteException {
        super();
    }

    public Map<Long, ArrayList<String>> getNameMap() {
        return nameMap;
    }

    public Map<String, MdAttr> getMdAttrMap() {
        return mdAttrMap;
    }

    @Override
    public boolean insertMd(long dCode, String name, MdAttr mdAttr) throws RemoteException {
        ArrayList<String> nameList = nameMap.get(dCode);
        synchronized (this) {
            if (nameList == null) {
                nameList = new ArrayList();
            }
            nameList.add(name);
        }
        nameMap.put(dCode, nameList);
        return mdAttrMap.put(buildKey(dCode, name), mdAttr) == null ? false : true;
    }

    @Override
    public MdAttr findFileMd(long dCode, String name) throws RemoteException {
        return mdAttrMap.get(buildKey(dCode, name));
    }

    @Override
    public List<MdAttr> listDir(long dCode) throws RemoteException {
        List<MdAttr> mdAttrs = MdAttrListCacheTool.getMdAttrListFromCache(dCode);
        if (mdAttrs != null) {
            return mdAttrs;
        }
        mdAttrs = new ArrayList<MdAttr>();
        ArrayList<String> nameList = nameMap.get(dCode);
        for (String name : nameList) {
            mdAttrs.add(findFileMd(dCode, name));
        }
        MdAttrListCacheTool.setMdIndextToCache(dCode, mdAttrs);
        return mdAttrs;
    }

    @Override
    public boolean renameMd(long dCode, String oldName, String newName) throws RemoteException {
        ArrayList<String> nameList = nameMap.get(dCode);
        if (!nameList.remove(oldName)) {
            throw new IllegalArgumentException("no such fileName :" + oldName);
        }
        nameList.add(newName);
        MdAttr mdAttr = mdAttrMap.get(buildKey(dCode, oldName));
        if (mdAttr == null) {
            throw new IllegalArgumentException("no such mdAttr:" + oldName);
        }
        mdAttr.setName(newName);
        mdAttrMap.put(buildKey(dCode, newName), mdAttr);
        mdAttrMap.remove(buildKey(dCode, oldName));
        return true;
    }

    public String buildKey(long dCode, String fileName) {
        return dCode + ":" + fileName;
    }
}
