package monash.infotech.monashp2pdatasync.connectivity.p2p;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by john on 11/25/2015.
 * Responsible for getting the device Ip address and mac address
 */
public class LocalIPFinder {
    private static byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) {
                            return inetAddress.getAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
        } catch (NullPointerException ex) {
        }
        return null;
    }

    public static String getLocalDottedDecimalIPAddress() {
        //convert to dotted decimal notation:
        byte[] ipAddr=getLocalIPAddress();
        if(ipAddr==null)
            return "";
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

    public static String getMacAddress(Context context) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ntwInterface : interfaces) {

                if (ntwInterface.getName().equalsIgnoreCase("p2p0")) {
                    byte[] byteMac = ntwInterface.getHardwareAddress();
                    if (byteMac==null){
                        return null;
                    }
                    StringBuilder strBuilder = new StringBuilder();
                    for (int i=0; i<byteMac.length; i++) {
                        strBuilder.append(String.format("%02X:", byteMac[i]));
                    }

                    if (strBuilder.length()>0){
                        strBuilder.deleteCharAt(strBuilder.length()-1);
                    }

                    return strBuilder.toString().toLowerCase();
                }

            }
        } catch (Exception e) {
            Log.d("ali", e.getMessage());
        }
        return null;
    }
}
