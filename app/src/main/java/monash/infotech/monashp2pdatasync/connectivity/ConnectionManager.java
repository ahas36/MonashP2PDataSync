package monash.infotech.monashp2pdatasync.connectivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.TextView;

import com.google.gson.Gson;

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
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized ConnectionManager getSync() {
        if (instance == null) instance = new ConnectionManager();
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

    //Local Peer info
    private Peer localDevice;

    //connected peer info
    private Peer connectedPeer;

    private Activity mActivity;

    public void init(Activity context) {
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
        localDevice = new Peer(android.os.Build.MODEL, LocalIPFinder.getMacAddress(context));
        startFileServer();

        mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                for (WifiP2pDevice p : peers.getDeviceList()) {
                    if (p.status == 0) {
//                        if (connectedPeer == null) {
//                            connectedPeer = new Peer(p.deviceName, p.deviceAddress, "", true);
//                        } else {
//                            connectedPeer.setIsConnected(true);
//                            connectedPeer.setDeviceName(p.deviceName);
//                            connectedPeer.setMacAddress(p.deviceAddress);
//                        }
                        restorePeers();
                    }
                }
            }
        });
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

    public void connect(final WifiP2pDevice p) {
        if (p.status == 0) {
            return;
        }
        //  if (connectedPeer != null && connectedPeer.isConnected()) {
        //     disconnect();
        // }
        connectedPeer = new Peer(p.deviceName, p.deviceAddress, "", false);

        WifiP2pConfig config = new WifiP2pConfig();

        config.deviceAddress = p.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                connectedPeer.setIsConnected(true);
                localDevice.setIPAddress(LocalIPFinder.getLocalDottedDecimalIPAddress());
            }

            @Override
            public void onFailure(int reason) {
                //Failure logic
                connectedPeer.setIsConnected(false);
            }
        });
    }

    public void sendFile(final WifiP2pDevice peer, final String file) {
        if (peer.status != 0 || connectedPeer.getIPAddress().isEmpty())
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ClientFileSender.send(connectedPeer.getIPAddress(), 8888, file.getBytes());

            }
        }).start();
    }

    public void sendFile(final Peer peer, final String file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO your background code

                ClientFileSender.send(peer.getIPAddress(), 8888, file.getBytes());

            }
        }).start();
    }


    public void setIpAddress(String hostAddress, String macAddress) {

        if (connectedPeer != null && connectedPeer.getMacAddress().equalsIgnoreCase(macAddress)) {
            connectedPeer.setIsConnected(true);
            connectedPeer.setIPAddress(hostAddress);
        } else {
            connectedPeer = new Peer("", macAddress, hostAddress, true);
        }
        storePeers();
    }


    public void sendHandshakeMsg() {
        localDevice.setIPAddress(LocalIPFinder.getLocalDottedDecimalIPAddress());
        storePeers();
        Message msg = new Message(0, MessageType.handshake, localDevice, connectedPeer, "handshake");
        sendFile(connectedPeer, msg.toJson());
    }

    public void disconnect() {
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {

            }

            @Override
            public void onSuccess() {
                ConnectionManager.getManager().clearInfo();
            }
        });
    }

    public Peer getLocalDevice() {
        return localDevice;
    }

    public Peer getConnectedDevice() {
        return connectedPeer;
    }

    public void updateLocalPeer() {
        localDevice.setIPAddress(LocalIPFinder.getLocalDottedDecimalIPAddress());
    }

    private void storePeers() {
        Gson gson = new Gson();
        SharedPreferences.Editor sp = mActivity.getApplication().getSharedPreferences("P2P", Context.MODE_PRIVATE).edit();
        sp.putString("local", gson.toJson(localDevice));
        sp.putString("connected", gson.toJson(connectedPeer));
        sp.commit();
    }

    private void restorePeers() {
        Gson gson = new Gson();
        SharedPreferences sp = mActivity.getApplication().getSharedPreferences("P2P", Context.MODE_PRIVATE);
        localDevice = gson.fromJson(sp.getString("local", ""), Peer.class);
        connectedPeer = gson.fromJson(sp.getString("connected", ""), Peer.class);
    }
    private void clear()
    {
        mActivity.getApplication().getSharedPreferences("P2P", Context.MODE_PRIVATE).edit().clear().commit();
    }

    public  void clearInfo() {
        localDevice.setIPAddress("");
        connectedPeer=null;
        clear();
    }


}
