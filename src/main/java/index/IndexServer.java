package index;

import index.service.impl.IndexOpsServiceImpl;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by Mr-yang on 16-2-17.
 */
public class IndexServer {
    private static final int PORT = 8888;

    public static void bindRemoteCall() throws RemoteException, MalformedURLException {
        LocateRegistry.createRegistry(PORT);
        Naming.rebind("//localhost:" + PORT + "/INDEX",  new IndexOpsServiceImpl());
        System.out.println("IndexServer is ready.");
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
