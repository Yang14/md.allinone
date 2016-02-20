package md;

import base.md.MdAttr;
import client.service.ClientService;
import client.service.impl.ClientServiceImpl;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.logging.Logger;
/**
 * Created by Mr-yang on 16-2-18.
 */
public class TestClientService {
    private static Logger logger = Logger.getLogger("TestClientService");

    private ClientService clientService = new ClientServiceImpl();

    @Test
    public void buildDirTree() throws RemoteException {
        long start = System.currentTimeMillis();
        String secondDir = "bin2";
        for (int i = 0; i < 100; i++) {
            clientService.createDirMd("/", secondDir + i, getMdAttr(secondDir + i, i, true));
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("time: %s", (end - start)));

        String thirdDir = "foo";
        String thirdFile = "a.t";
        end = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                clientService.createDirMd("/" + secondDir + i, thirdDir + j, getMdAttr(thirdDir + j, i, true));
                clientService.createFileMd("/" + secondDir + i, thirdFile + j, getMdAttr(thirdFile + j, i, false));
            }
        }
        long end2 = System.currentTimeMillis();
        logger.info(String.format("time: %s", (end2 - end)));
    }


    @Test
    public void testListDirTree() throws RemoteException {
        long start = System.currentTimeMillis();
        String secondDir = "bin2";
        for (int i = 0; i < 100; i++) {
            clientService.listDir("/" + secondDir + i);
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("time: %s", (end - start)));

        String thirdDir = "foo";
        String thirdFile = "a.t";
        end = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                clientService.listDir("/" + secondDir + i);
                clientService.findFileMd("/" + secondDir + i,thirdFile +j);
            }
        }
        long end2 = System.currentTimeMillis();
        logger.info(String.format("time: %s", (end2 - end)));
    }

    @Test
    public void testListDir() throws RemoteException {
        long start = System.currentTimeMillis();
        logger.info(clientService.listDir("/").toString());
        logger.info(clientService.listDir("/bin1").toString());
        long end = System.currentTimeMillis();
        logger.info(String.format("time: %s", (end - start)));
    }

    @Test
    public void testRenameFile() throws RemoteException {
//        logger.info(clientService.findFileMd("/bin0","a.t0").toString());
        logger.info(clientService.listDir("/bin0").toString());
        clientService.renameFile("/bin0", "a.t0", "renamed_a.t0");
        logger.info(clientService.listDir("/bin0").toString());
        logger.info(clientService.findFileMd("/bin0", "renamed_a.t0").toString());
    }

    @Test
    public void testRenameDir() throws RemoteException {
        logger.info(clientService.listDir("/bin0").toString());
        clientService.renameDir("/","bin0","rename_bin0");
        logger.info(clientService.listDir("/rename_bin0").toString());
    }


    private MdAttr getMdAttr(String name, int size, boolean isDir) {
        MdAttr mdAttr = new MdAttr();
        mdAttr.setName(name);
        mdAttr.setSize(size);
        mdAttr.setType(isDir);
        mdAttr.setCreateTime(System.currentTimeMillis());
        return mdAttr;
    }
}
