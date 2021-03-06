package monash.infotech.monashp2pdatasync.entities;

import monash.infotech.monashp2pdatasync.entities.context.UserContext;

/**
 * Created by john on 11/26/2015.
 */
public class Peer {

    private String IPAddress;
    private String deviceName;
    private String macAddress;
    private boolean isConnected;
    private UserContext userContext;
    private long lastSync;
    public Peer(String deviceName, String macAddress,String IPAddress, boolean isConnected) {
        this.IPAddress = IPAddress;
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.isConnected=isConnected;
    }

    public Peer(String deviceName, String macAddress) {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        IPAddress="";
    }

    public Peer() {
        isConnected=false;
    }


    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public long getLastSync() {
        return lastSync;
    }

    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Peer)) return false;

        Peer peer = (Peer) o;

        return macAddress.equals(peer.macAddress);

    }

    @Override
    public int hashCode() {
        return macAddress != null ? macAddress.hashCode() : 0;
    }
}
