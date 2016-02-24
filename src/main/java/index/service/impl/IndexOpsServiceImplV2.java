package index.service.impl;

import base.md.MdIndex;
import base.md.MdPos;
import base.rmiapi.index.IndexOpsService;
import com.mongodb.MongoClient;
import index.common.CommonModule;
import index.common.CommonModuleImpl;
import index.dao.model.MdIndexV2;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateOperations;
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
public class IndexOpsServiceImplV2 extends UnicastRemoteObject implements IndexOpsService {
    private static Logger logger = LoggerFactory.getLogger("IndexOpsServiceImplV2");

    private volatile boolean isInit = false;

    private CommonModule commonModule = new CommonModuleImpl();

    private Datastore datastore;

    public IndexOpsServiceImplV2() throws RemoteException {
        super();
        MongoClient mongo = new MongoClient();
        Morphia morphia = new Morphia();
        morphia.mapPackage("index.dao.model");
        datastore = morphia.createDatastore(mongo, "mdIndexManager");
        datastore.ensureIndexes();
        if (!isInit) {
            initRootDir();
        }
    }

    private void initRootDir() {
        long parentCode = -1;
        long fCode = -1;
        long dCode = 0;
        String name = "/";
        MdIndexV2 mdIndexV2 = genDirIndex(parentCode, name, fCode, dCode);
        if (datastore.createQuery(MdIndexV2.class).filter("fName = ", "/").countAll() == 0) {
            datastore.save(mdIndexV2);
            isInit = true;
            logger.info("root dir init ...");
        }
    }

    private MdIndexV2 genDirIndex(long parentCode, String name, long fCode, long dCode) {
        List<Long> dCodes = new ArrayList<Long>();
        dCodes.add(dCode);
        return new MdIndexV2(parentCode, name, fCode, dCodes);
    }

    @Override
    public MdPos createDirIndex(String parentPath, String dirName) throws RemoteException {
        MdIndexV2 parentIndex = getMdIndexByPath(parentPath);
        MdIndexV2 dirIndex = genDirIndex(parentIndex.getfCode(), dirName,
                commonModule.genFCode(), commonModule.genDCode());
        datastore.save(dirIndex);
        return getMdAttrPos(parentIndex);
    }

    private MdPos getMdAttrPos(MdIndexV2 parentIndex) {
        List<Long> dCodeList = parentIndex.getdCodeList();
        long dCode = dCodeList.get(dCodeList.size() - 1);
        boolean isFit = commonModule.isDCodeFit(dCode);
        if (!isFit) {
            dCode = commonModule.genDCode();
            updateDCodeListWithNewCode(parentIndex, dCode);
        }
        return commonModule.buildMdPos(dCode);
    }

    //先要得到保存父目录的键，再更新节点信息
    private boolean updateDCodeListWithNewCode(MdIndexV2 mdIndexV2, long newDCode) {
        List<Long> dCodeList = mdIndexV2.getdCodeList();
        dCodeList.add(newDCode);
        mdIndexV2.setdCodeList(dCodeList);
        UpdateOperations<MdIndexV2> ops = datastore.createUpdateOperations(MdIndexV2.class);
        ops.set("dCodeList", dCodeList);
        return datastore.update(mdIndexV2, ops).getUpdatedExisting();
    }

    @Override
    public MdPos getMdPosForCreateFile(String path) throws RemoteException {
        MdIndexV2 parentIndex = getMdIndexByPath(path);
        return getMdAttrPos(parentIndex);
    }

    @Override
    public List<MdPos> getMdPosList(String path) throws RemoteException {
        MdIndexV2 mdIndexV2 = getMdIndexByPath(path);
        return commonModule.buildMdPosList(mdIndexV2.getdCodeList());
    }

    @Override
    public List<MdPos> renameDirIndex(String parentPath, String oldName, String newName) throws RemoteException {
        MdIndexV2 parentIndex = getMdIndexByPath(parentPath);
        MdIndexV2 dirIndex = datastore.createQuery(MdIndexV2.class)
                .filter("pCode = ", parentIndex.getpCode())
                .filter("fName = ", oldName).get();
        UpdateOperations<MdIndexV2> ops = datastore.createUpdateOperations(MdIndexV2.class);
        ops.set("fName", newName);
        datastore.update(dirIndex, ops).getUpdatedExisting();
        return commonModule.buildMdPosList(parentIndex.getdCodeList());
    }

    @Override
    public Map<String, MdIndex> getIndexMap() throws RemoteException {
        return null;
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

    public MdIndexV2 getMdIndexByPath(String path) {
        MdIndexV2 mdIndexV2 = MdIndexCacheTool.getMdIndexV2FromCache(path);
        if (mdIndexV2 != null) {
            return mdIndexV2;
        }
        String[] nameArray = splitPath(path);
        long code = -1;
        for (String name : nameArray) {
            mdIndexV2 = datastore.createQuery(MdIndexV2.class)
                    .filter("pCode = ", code).filter("fName = ", name).get();
            if (mdIndexV2 == null) {
                throw new IllegalArgumentException(String.format("path %s not exist.", path));
            }
            code = mdIndexV2.getfCode();
        }
        MdIndexCacheTool.setMdIndexV2tToCache(path, mdIndexV2);
        return mdIndexV2;
    }
}
