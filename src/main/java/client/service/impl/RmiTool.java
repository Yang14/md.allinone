package client.service.impl;

import base.md.MdPos;
import base.rmiapi.backend.BackendOpsService;
import base.rmiapi.index.IndexOpsService;

import java.rmi.Naming;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class RmiTool {
    private static final int INDEX_PORT = 8888;
    private static final Map<String, BackendOpsService> backendOpsMap =
            new ConcurrentHashMap<String, BackendOpsService>();
    private static Logger logger = Logger.getLogger("RmiTool");

    public static IndexOpsService getIndexOpsService() {
        IndexOpsService indexOps = null;
        try {
            indexOps = (IndexOpsService) Naming.lookup("//192.168.0.13:" + INDEX_PORT + "/INDEX");
        } catch (Exception e) {
            logger.severe("error info:" + e.getMessage());
        }
        return indexOps;
    }

    public static BackendOpsService getBackendOpsService(MdPos mdPos) {
        BackendOpsService backendOps = null;
        String backendAddress = genBackendAddress(mdPos.getIp(), mdPos.getPort());
        backendOps = backendOpsMap.get(backendAddress);
        if (backendOps == null) {
            try {
                backendOps = (BackendOpsService) Naming.lookup(backendAddress);
                backendOpsMap.put(backendAddress, backendOps);
            } catch (Exception e) {
                logger.severe("error info:" + e.getMessage());
            }
        }
        return backendOps;
    }

    private static String genBackendAddress(String ip, int port) {
        return "//" + ip + ":" + port + "/BACKEND";
    }
}
