package index;

import base.IpTool;
import base.PortEnum;
import index.service.impl.IndexOpsServiceImpl;
import index.service.impl.IndexOpsServiceImplV2;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by Mr-yang on 16-2-17.
 */
public class IndexServer {
    private static final int PORT = PortEnum.INDEX_PORT;

    public static void bindRemoteCall() throws RemoteException, MalformedURLException {
        LocateRegistry.createRegistry(PORT);
        String ip = IpTool.getMachineIP();
        Naming.rebind("//" + ip + ":" + PORT + "/INDEX", new IndexOpsServiceImpl());
//        Naming.rebind("//" + ip + ":" + PORT + "/INDEX", new IndexOpsServiceImplV2());
        System.out.println("IndexServer is ready." + ip);
    }

    public static void main(String[] args) {
        try {
            bindRemoteCall();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
