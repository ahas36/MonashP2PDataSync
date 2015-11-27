package monash.infotech.monashp2pdatasync.connectivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import java.util.ArrayList;
import java.util.List;
import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.connectivity.transfer.ClientFileSender;
import monash.infotech.monashp2pdatasync.connectivity.transfer.FileServerAsyncTask;
import monash.infotech.monashp2pdatasync.entities.Peer;
import monash.infotech.monashp2pdatasync.messaging.Message;
import monash.infotech.monashp2pdatasync.messaging.MessageType;

/**
 * Created by john on 11/26/2015.
 */
public class ConnectionManager {

    private static ConnectionManager instance;

    public static ConnectionManager getManager() {
        if(instance == null) instance = getSync();
        return instance;
    }

    private static synchronized ConnectionManager getSync() {
        if(instance == null) instance = new ConnectionManager();
        return instance;
    }

    //wifi direct manager
    WifiP2pManager mManager;
    //wifi direct Channel
    WifiP2pManager.Channel mChannel;
    //WiFiDirectBroadcastReceiver
    BroadcastReceiver mReceiver;

    //intent filter to add WifiP2pManager actions
    IntentFilter mIntentFilter;

    //device IP address
    private Peer localDevice;

    //connected peer Ip address
    private List<Peer> peers;

    private Activity mActivity;
    private Peer groupOwner;

    public void init(Activity context) {
        peers = new ArrayList<>();
        this.mActivity = context;
        //init wifi direct
        mManager = (WifiP2pManager) mActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mActivity, mActivity.getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, mActivity);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        localDevice =new Peer(android.os.Build.MODEL,LocalIPFinder.getMacAddress(context));
        groupOwner=new Peer();
        startFileServer();

    }

    public boolean isDeviceConnected(WifiP2pDevice device) {
        for (Peer p : peers) {
            if (p.getMacAddress().equals(device.deviceAddress)) {
                if (p.isConnected()) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }

    public void startFileServer() {
        //start file receiver service
        (new FileServerAsyncTask(mActivity, mActivity.findViewById(R.id.textView))).execute();
    }

    public void startPeerDiscovery(WifiP2pManager.ActionListener actionListener) {
        mManager.discoverPeers(mChannel, actionListener);
    }

    public void stopPeerDiscovery(WifiP2pManager.ActionListener actionListener) {
        mManager.stopPeerDiscovery(mChannel, actionListener);
    }

    public void registerReceiver() {
        mActivity.registerReceiver(mReceiver, mIntentFilter);
    }

    public void unregisterReceiver() {
        mActivity.unregisterReceiver(mReceiver);
    }

    public void connect(final WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();

        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        Peer mPeer = findPeerByMacAddress(device.deviceAddress);
        final Peer peer;

        if (mPeer == null) {
            peer = new Peer(device.deviceAddress, device.deviceName);
            peers.add(mPeer);
        } else {
            peer = mPeer;
        }
        if(device.isGroupOwner())
        {
            groupOwner=peer;
        }

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                peer.setIsConnected(true);

            }

            @Override
            public void onFailure(int reason) {
                //Failure logic
                peer.setIsConnected(false);
            }
        });
    }

    public void sendFile(final WifiP2pDevice peer,final String file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                String ipAddress;
                if(peer.isGroupOwner())
                {
                    ipAddress=groupOwner.getIPAddress();
                }
                else {
                    Peer p = findPeerByMacAddress(peer.deviceAddress);
                    if (p == null)
                        return;
                    ipAddress=p.getIPAddress();
                }
                ClientFileSender.send(ipAddress, 8888, file.getBytes());
                /*mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onFailure(int reasonCode) {

                    }

                    @Override
                    public void onSuccess() {

                    }
                });*/
            }
        }).start();
    }

    public void sendFile(final Peer peer,final String file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO your background code

                ClientFileSender.send(peer.getIPAddress(), 8888, file.getBytes());

            }
        }).start();
    }

    private Peer findPeerByMacAddress(String address) {
        for (Peer p : peers) {
            if (p.getMacAddress().equals(address)) {
                return p;
            }
        }
        return null;
    }

    private Peer findPeerByHostName(String name) {
        for (Peer p : peers) {
            if (p.getDeviceName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public void setIpAddress(String hostAddress, String macAddress) {
        Peer p=findPeerByMacAddress(macAddress);
        if(p!=null)
        {
            p.setIsConnected(true);
            p.setIPAddress(hostAddress);
        }
    }

    public void setGroupOwnerIPAddress(String groupOwnerIPAddress) {
        this.groupOwner.setIPAddress(groupOwnerIPAddress);
    }

    public void sendHandshakeMsg()
    {
        localDevice.setIPAddress(LocalIPFinder.getLocalDottedDecimalIPAddress());
        Message msg=new Message(0, MessageType.handshake, localDevice, groupOwner, "handshake");
        sendFile(groupOwner,msg.toJson());
    }
}
