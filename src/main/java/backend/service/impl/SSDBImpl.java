package backend.service.impl;

import base.PortEnum;
import base.md.MdAttr;
import base.rmiapi.backend.BackendOpsService;
import com.alibaba.fastjson.JSON;
import org.nutz.ssdb4j.SSDBs;
import org.nutz.ssdb4j.impl.SimpleClient;
import org.nutz.ssdb4j.spi.Response;
import org.nutz.ssdb4j.spi.SSDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr-yang on 16-2-23.
 */
public class SSDBImpl extends UnicastRemoteObject implements BackendOpsService {
    private static Logger logger = LoggerFactory.getLogger("SSDBImpl");

    private static SSDB ssdb = SSDBs.pool("127.0.0.1", PortEnum.SSDB_PORT, 10000,null);//new SimpleClient("127.0.0.1", PortEnum.SSDB_PORT, 10000);
    /*private static ThreadLocal<SSDB> ssdbHolder =
            new ThreadLocal<SSDB>(){
                public SSDB initiValue(){
                    return new SimpleClient("127.0.0.1", PortEnum.SSDB_PORT, 10000);
                }
            };*/

    public SSDBImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean insertMd(long dCode, String name, MdAttr mdAttr) throws RemoteException {
        Response response = ssdb.hset(dCode, name, JSON.toJSONString(mdAttr));
        return response.ok();
    }

    @Override
    public MdAttr findFileMd(long dCode, String name) throws RemoteException {
        return JSON.parseObject(ssdb.hget(dCode, name).asString(), MdAttr.class);
    }

    @Override
    public List<MdAttr> listDir(long dCode) throws RemoteException {
        Map<String, String> mdAttrMap = ssdb.hgetall(dCode).mapString();
        List<MdAttr> mdAttrs = new ArrayList<MdAttr>();
        for (String value : mdAttrMap.values()) {
            mdAttrs.add(JSON.parseObject(value, MdAttr.class));
        }
        return mdAttrs;
    }

    @Override
    public boolean renameMd(long dCode, String oldName, String newName) throws RemoteException {
        MdAttr mdAttr =JSON.parseObject(ssdb.hget(dCode, oldName).asString(), MdAttr.class);
        mdAttr.setName(newName);
        ssdb.hdel(dCode, oldName);
        Response response = ssdb.hset(dCode, newName, JSON.toJSONString(mdAttr));
        return response.ok();
    }

    @Override
    public Map<Long, ArrayList<String>> getNameMap() throws RemoteException {
        return null;
    }

    @Override
    public Map<String, MdAttr> getMdAttrMap() throws RemoteException {
        return null;
    }
}
