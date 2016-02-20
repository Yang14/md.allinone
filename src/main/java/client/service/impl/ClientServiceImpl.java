package client.service.impl;

import base.md.MdAttr;
import base.md.MdPos;
import base.rmiapi.backend.BackendOpsService;
import base.rmiapi.index.IndexOpsService;
import client.service.ClientService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class ClientServiceImpl implements ClientService {
    private static final AtomicInteger error = new AtomicInteger();
    private static Logger logger = Logger.getLogger("ClientServiceImpl");
    private static IndexOpsService indexOps = RmiTool.getIndexOpsService();

    @Override
    public boolean createFileMd(String parentDirPath, String fileName, MdAttr mdAttr) throws RemoteException {
        MdPosCacheTool.removeMdPosList(parentDirPath);
        MdPos mdPos = indexOps.getMdPosForCreateFile(parentDirPath);
        final BackendOpsService backendOpsService = RmiTool.getBackendOpsService(mdPos);
        return backendOpsService.insertMd(mdPos.getdCode(), fileName, mdAttr);
    }

    @Override
    public boolean createDirMd(String parentDirPath, String dirName, MdAttr mdAttr) throws RemoteException {
        MdPosCacheTool.removeMdPosList(parentDirPath);
        MdPos mdPos = indexOps.createDirIndex(parentDirPath, dirName);
        final BackendOpsService backendOpsService = RmiTool.getBackendOpsService(mdPos);
        return backendOpsService.insertMd(mdPos.getdCode(), dirName, mdAttr);
    }

    @Override
    public MdAttr findFileMd(String parentDirPath, String fileName) throws RemoteException {
        List<MdPos> mdPosList = getMdPosListByPath(parentDirPath);
        MdAttr mdAttr = null;
        BackendOpsService backendOpsService;
        for (MdPos mdPos : mdPosList) {
            backendOpsService = RmiTool.getBackendOpsService(mdPos);
            mdAttr = backendOpsService.findFileMd(mdPos.getdCode(), fileName);
            if (mdAttr != null) {
                break;
            }
        }
        return mdAttr;
    }

    @Override
    public List<MdAttr> listDir(String dirPath) throws RemoteException {
        List<MdPos> mdPosList = getMdPosListByPath(dirPath);
        List<MdAttr> mdAttrList = new ArrayList<MdAttr>();
        BackendOpsService backendOpsService;
        for (MdPos mdPos : mdPosList) {
            backendOpsService = RmiTool.getBackendOpsService(mdPos);
            List<MdAttr> partMdAttrList = backendOpsService.listDir(mdPos.getdCode());
            if (partMdAttrList != null) {
                mdAttrList.addAll(partMdAttrList);
            }
        }
        return mdAttrList;
    }

    @Override
    public boolean renameDir(String parentDirPath, String oldName, String newName) throws RemoteException {
        List<MdPos> mdPosList = getMdPosListFromRenameDir(parentDirPath, oldName, newName);
        boolean renameResult = false;
        BackendOpsService backendOpsService;
        for (MdPos mdPos : mdPosList) {
            backendOpsService = RmiTool.getBackendOpsService(mdPos);
            renameResult = backendOpsService.renameMd(mdPos.getdCode(), oldName, newName);
            if (renameResult) {
                break;
            }
        }
        return renameResult;
    }

    @Override
    public boolean renameFile(String parentDirPath, String oldName, String newName) throws RemoteException {
        List<MdPos> mdPosList = getMdPosListByPath(parentDirPath);
        boolean renameResult = false;
        BackendOpsService backendOpsService;
        for (MdPos mdPos : mdPosList) {
            backendOpsService = RmiTool.getBackendOpsService(mdPos);
            renameResult = backendOpsService.renameMd(mdPos.getdCode(), oldName, newName);
            if (renameResult) {
                break;
            }
        }
        return renameResult;
    }

    private List<MdPos> getMdPosListByPath(String path) throws RemoteException {
        List<MdPos> mdPosList = MdPosCacheTool.getMdPosListFromCache(path);
        if (mdPosList == null) {
            mdPosList = indexOps.getMdPosList(path);
            MdPosCacheTool.setMdPosListToCache(path, mdPosList);
        }
        return mdPosList;
    }

    private List<MdPos> getMdPosListFromRenameDir(String path, String oldName, String newName) throws RemoteException {
        List<MdPos> mdPosList = MdPosCacheTool.getMdPosListFromCache(path);
        if (mdPosList == null) {
            mdPosList = indexOps.renameDirIndex(path, oldName, newName);
            MdPosCacheTool.setMdPosListToCache(path, mdPosList);
        }
        return mdPosList;
    }

}
