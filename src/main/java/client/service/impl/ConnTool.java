package client.service.impl;

import base.PortEnum;
import base.md.MdPos;
import base.rmiapi.index.IndexOpsService;
import org.nutz.ssdb4j.SSDBs;
import org.nutz.ssdb4j.spi.SSDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class ConnTool {
    private static final int INDEX_PORT = PortEnum.INDEX_PORT;
    private static final String INDEX_IP = "rmi://192.168.0.13:";

    private static final Map<String, SSDB> ssdbHolder = new ConcurrentHashMap<String, SSDB>();

    private static Logger logger = LoggerFactory.getLogger("ConnTool");

    public static IndexOpsService getIndexOpsService() {
        IndexOpsService indexOps = null;
        try {
            indexOps = (IndexOpsService) Naming.lookup(INDEX_IP + INDEX_PORT + "/INDEX");
        } catch (Exception e) {
            logger.error("error info:" + e.getMessage());
        }
        return indexOps;
    }

    public static SSDB getSSDB(MdPos mdPos) {
        String cachedSSDBKey = getCachedSSDBKey(mdPos.getIp(), mdPos.getPort());
        SSDB ssdb = ssdbHolder.get(cachedSSDBKey);
        if (ssdb == null) {
            ssdb = SSDBs.pool(mdPos.getIp(), PortEnum.SSDB_PORT, 60000, null);
            ssdbHolder.put(cachedSSDBKey, ssdb);
        }
        return ssdb;
    }

    private static String getCachedSSDBKey(String ip, int port) {
        return ip + ":" + port;
    }
}
