package backend;

import backend.service.impl.SSDBImpl;
import base.IpTool;
import base.PortEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by Mr-yang on 16-2-17.
 */
public class BackendServer {
    private static Logger logger = LoggerFactory.getLogger("BackendServer");
    private static final int PORT = PortEnum.BACKEND_PORT;

    public static void bindRemoteCall() throws RemoteException, MalformedURLException, UnknownHostException {
        LocateRegistry.createRegistry(PORT);
        String localIp = IpTool.getMachineIP();
        Naming.rebind("//" + localIp + ":" + PORT + "/BACKEND", new SSDBImpl());
        logger.info("BackendServer is ready." + localIp);
    }

    public static void main(String[] args) {
        try {
            logger.info("input args:" + args[0]);
            bindRemoteCall();
        } catch (RemoteException e) {
            logger.error(e.getMessage());
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
        } catch (UnknownHostException e) {
            logger.error(e.getMessage());
        }
    }
}
