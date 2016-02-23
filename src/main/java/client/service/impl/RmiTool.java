package client.service.impl;

import base.md.MdPos;
import base.rmiapi.backend.BackendOpsService;
import base.rmiapi.index.IndexOpsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class RmiTool {
    private static final int INDEX_PORT = 8888;
    private static final String INDEX_IP = "rmi://192.168.0.13:";
    private static final Map<String, BackendOpsService> backendOpsMap =
            new ConcurrentHashMap<String, BackendOpsService>();
    private static Logger logger = LoggerFactory.getLogger("RmiTool");

    public static IndexOpsService getIndexOpsService() {
        IndexOpsService indexOps = null;
        try {
            indexOps = (IndexOpsService) Naming.lookup(INDEX_IP + INDEX_PORT + "/INDEX");
        } catch (Exception e) {
            logger.error("error info:" + e.getMessage());
        }
        return indexOps;
    }

    public static BackendOpsService getBackendOpsService(MdPos mdPos) {
        BackendOpsService backendOps;
        String backendAddress = genBackendAddress(mdPos.getIp(), mdPos.getPort());
        backendOps = backendOpsMap.get(backendAddress);
        if (backendOps == null) {
            try {
                backendOps = (BackendOpsService) Naming.lookup(backendAddress);
                backendOpsMap.put(backendAddress, backendOps);
            } catch (Exception e) {
                logger.error("error info:" + e.getMessage());
            }
        }
        return backendOps;
    }

    private static String genBackendAddress(String ip, int port) {
        return "rmi://" + ip + ":" + port + "/BACKEND";
    }
}
