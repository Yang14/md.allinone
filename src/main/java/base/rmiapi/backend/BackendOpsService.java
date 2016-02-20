package base.rmiapi.backend;


import base.md.MdAttr;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr-yang on 16-1-14.
 */
public interface BackendOpsService extends Remote {

    /**
     * 如果不存在哈希桶则创建桶号为dCode的哈希桶
     * 桶内插入name
     * 插入<{dCode,name},mdAttr>键值对
     */
    public boolean insertMd(long dCode, String name, MdAttr mdAttr) throws RemoteException;

    /**
     * 组合新键后查找并返回
     */
    public MdAttr findFileMd(long dCode, String name) throws RemoteException;

    /**
     * 查找得到桶内的所有name
     * 组合dCode和name得到新键
     * 根据新建查找并返回目录元数据列表
     */
    public List<MdAttr> listDir(long dCode) throws RemoteException;

    /**
     * 重命名文件和目录的方法一致
     * 删除桶内的oldName,插入newName
     * 更新oldName的键为newName
     */
    public boolean renameMd(long dCode, String oldName, String newName) throws RemoteException;

    public Map<Long, ArrayList<String>> getNameMap() throws RemoteException;

    public Map<String, MdAttr> getMdAttrMap() throws RemoteException;
}
