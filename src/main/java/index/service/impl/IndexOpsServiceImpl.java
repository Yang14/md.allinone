package index.service.impl;

import base.md.MdIndex;
import base.md.MdPos;
import base.rmiapi.index.IndexOpsService;
import index.common.CommonModule;
import index.common.CommonModuleImpl;
import index.dao.IndexDao;
import index.dao.impl.IndexDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class IndexOpsServiceImpl extends UnicastRemoteObject implements IndexOpsService {
    private static Logger logger = LoggerFactory.getLogger("IndexOpsServiceImpl");

    private volatile boolean isInit = false;

    private IndexDao indexDao = new IndexDaoImpl();

    private CommonModule commonModule = new CommonModuleImpl();

    public IndexOpsServiceImpl() throws RemoteException {
        super();
        if (!isInit) {
            initRootDir();
        }
    }

    private void initRootDir() {
        long parentCode = -1;
        long fCode = -1;
        long dCode = 0;
        String name = "/";
        String key = buildKey(parentCode, name);
        MdIndex rootIndex = genDirIndex(fCode, dCode);
        boolean isInit = indexDao.insertMdIndex(key, rootIndex);
        if (isInit) {
            logger.info("init root dir...");
            isInit = true;
        } else {
            logger.info("init root dir failed...");
        }
    }

    @Override
    public MdPos createDirIndex(String parentPath, String dirName) throws RemoteException {
        MdIndex parentIndex = getMdIndexByPath(parentPath);
        indexDao.insertMdIndex(buildKey(parentIndex.getfCode(), dirName),
                genDirIndex(commonModule.genFCode(), commonModule.genDCode()));
        return getMdAttrPos(parentIndex, parentPath);
    }

    private MdIndex genDirIndex(long fCode, long dCode) {
        List<Long> dCodes = new ArrayList<Long>();
        dCodes.add(dCode);
        MdIndex mdIndex = new MdIndex();
        mdIndex.setfCode(fCode);
        mdIndex.setdCodeList(dCodes);
        return mdIndex;
    }

    private MdPos getMdAttrPos(MdIndex parentIndex, String path) {
        List<Long> dCodeList = parentIndex.getdCodeList();
        long dCode = dCodeList.get(dCodeList.size() - 1);
        boolean isFit = commonModule.isDCodeFit(dCode);
        if (!isFit) {
            dCode = commonModule.genDCode();
            updateDCodeListWithNewCode(parentIndex, path, dCode);
        }
        return commonModule.buildMdPos(dCode);
    }

    //先要得到保存父目录的键，再更新节点信息
    private boolean updateDCodeListWithNewCode(MdIndex mdIndex, String path, long newDCode) {
        int pos = path.lastIndexOf("/");
        String front = path.substring(0, pos);
        String end = path.substring(pos + 1);
        if (front.equals("")) {
            front = "/";
        }
        String parentKey = buildKey(getMdIndexByPath(front).getfCode(), end);
        List<Long> dCodeList = mdIndex.getdCodeList();
        dCodeList.add(newDCode);
        mdIndex.setdCodeList(dCodeList);
        indexDao.removeMdIndex(parentKey);
        return indexDao.insertMdIndex(parentKey, mdIndex);
    }

    @Override
    public MdPos getMdPosForCreateFile(String path) throws RemoteException {
        MdIndex parentIndex = getMdIndexByPath(path);
        return getMdAttrPos(parentIndex, path);
    }

    @Override
    public List<MdPos> getMdPosList(String path) throws RemoteException {
        MdIndex mdIndex = getMdIndexByPath(path);
        return commonModule.buildMdPosList(mdIndex.getdCodeList());
    }

    @Override
    //TODO
    public List<MdPos> renameDirIndex(String parentPath, String oldName, String newName) throws RemoteException {
        MdIndex parentIndex = getMdIndexByPath(parentPath);
        String oldKey = buildKey(parentIndex.getfCode(), oldName);
        MdIndex mdIndex = indexDao.findMdIndex(oldKey);
        String newKey = buildKey(parentIndex.getfCode(), newName);
        indexDao.insertMdIndex(newKey, mdIndex);
        indexDao.removeMdIndex(oldKey);
        return commonModule.buildMdPosList(parentIndex.getdCodeList());
    }

    @Override
    public Map<String, MdIndex> getIndexMap() throws RemoteException {
        return indexDao.getIndexMap();
    }

    public String[] splitPath(String path) {
        if (path == null || path.equals("") || path.charAt(0) != '/') {
            logger.info("splitPath params err: " + path);
            throw new IllegalArgumentException("splitPath params err: " + path);
        }
        if (path.equals("/")) {
            return new String[]{"/"};
        }
        String[] nameArray = path.split("/");
        nameArray[0] = "/";
        return nameArray;
    }

    public MdIndex getMdIndexByPath(String path) {
        MdIndex mdIndex = MdIndexCacheTool.getMdIndexFromCache(path);
        if (mdIndex != null){
            return mdIndex;
        }
        String[] nameArray = splitPath(path);
        long code = -1;
        for (String name : nameArray) {
            mdIndex = indexDao.findMdIndex(buildKey(code, name));
            if (mdIndex == null) {
                throw new IllegalArgumentException(String.format("path %s not exist.", path));
            }
            code = mdIndex.getfCode();
        }
        MdIndexCacheTool.setMdIndextToCache(path, mdIndex);
        return mdIndex;
    }

    private String buildKey(long dCode, String fileName) {
        return dCode + ":" + fileName;
    }

}
