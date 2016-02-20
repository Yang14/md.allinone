package backend;

import backend.service.impl.BackendOpsServiceImpl;

import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Enumeration;

/**
 * Created by Mr-yang on 16-2-17.
 */
public class BackendServer {
    private static final int PORT = 9999;

    public static String getMachineIP() {
        try {
            String hostIP = InetAddress.getLocalHost().getHostAddress();
            if (!hostIP.equals("127.0.0.1")) {
                return hostIP;
            }

        /*
         * Above method often returns "127.0.0.1", In this case we need to
         * check all the available network interfaces
         */
            Enumeration<NetworkInterface> nInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (nInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = nInterfaces
                        .nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    String address = inetAddresses.nextElement()
                            .getHostAddress();
                    if (!address.equals("192.168.0.255") && address.indexOf("192.168.0") != -1) {
                        return address;
                    }
                }
            }
        } catch (UnknownHostException e1) {
            System.err.println("Error = " + e1.getMessage());
        } catch (SocketException e1) {
            System.err.println("Error = " + e1.getMessage());
        }
        return null;
    }

    public static void bindRemoteCall() throws RemoteException, MalformedURLException, UnknownHostException {
        LocateRegistry.createRegistry(PORT);
        String localIp = getMachineIP();
        Naming.rebind("//" + localIp + ":" + PORT + "/BACKEND", new BackendOpsServiceImpl());
        System.out.println("BackendServer is ready." + localIp);
    }

    public static void main(String[] args) {
        try {
            bindRemoteCall();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
