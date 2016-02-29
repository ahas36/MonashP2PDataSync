package monash.infotech.monashp2pdatasync.connectivity.p2p;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.google.gson.Gson;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.List;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.app.P2PApplicationContext;
import monash.infotech.monashp2pdatasync.connectivity.p2p.transfer.ClientFileSender;
import monash.infotech.monashp2pdatasync.connectivity.p2p.transfer.FileServerAsyncTask;
import monash.infotech.monashp2pdatasync.data.db.DatabaseHelper;
import monash.infotech.monashp2pdatasync.entities.MyMsg;
import monash.infotech.monashp2pdatasync.entities.Peer;
import monash.infotech.monashp2pdatasync.messaging.Message;

/**
 * Created by john on 11/26/2015.
 * A main class that handel connectivity
 */
public class ConnectionManager {
    //A singleton object
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

    //reference to the main activity
    private Activity mActivity;

    public void init(Activity context, Fragment f) {
        this.mActivity = context;
        //init wifi direct
        mManager = (WifiP2pManager) mActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mActivity, mActivity.getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, f);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //initial the local device
        localDevice = new Peer(android.os.Build.MODEL, LocalIPFinder.getMacAddress(context));

        //start the file server
        startFileServer();

        //get all the currently available peers

    }

    //start file receiver service
    public void startFileServer() {

        (new FileServerAsyncTask(mActivity, mActivity.findViewById(R.id.lastSyncLogId))).execute();
    }

    //start Peer Discovery
    public void startPeerDiscovery(WifiP2pManager.ActionListener actionListener) {
        mManager.discoverPeers(mChannel, actionListener);
    }

    //stop Peer Discovery
    public void stopPeerDiscovery(WifiP2pManager.ActionListener actionListener) {
        mManager.stopPeerDiscovery(mChannel, actionListener);
    }

    //register WiFiDirect service
    public void registerReceiver() {
        mActivity.registerReceiver(mReceiver, mIntentFilter);
    }

    //unregister WiFiDirect service
    public void unregisterReceiver() {
        mActivity.unregisterReceiver(mReceiver);
    }


    //connect to device p
    public void connect(final WifiP2pDevice p) {
        //if its already connected, return
        if (p.status == 0) {
            return;
        }
        //init the connected peer
        connectedPeer = new Peer(p.deviceName, p.deviceAddress, "", false);

        WifiP2pConfig config = new WifiP2pConfig();

        config.deviceAddress = p.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        //connect
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                connectedPeer.setIsConnected(true);
                //update the local IP address

            }

            @Override
            public void onFailure(int reason) {
                //Failure logic
                connectedPeer.setIsConnected(false);
            }
        });
    }

    //send a string to peer (peer)
    public void sendFile(final String file) {
        Gson gson = new Gson();
        logMsg(gson.fromJson(file, Message.class));
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                ClientFileSender.send(connectedPeer.getIPAddress(), 8888, file.getBytes());

            }
        }).start();
    }

    //send a file to peer (peer)
    public void sendFile(final byte[] file) {
        Gson gson = new Gson();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                ClientFileSender.send(connectedPeer.getIPAddress(), 8888, file);

            }
        }).start();
    }

    //send a list of files to peer
    public void sendFile(final List<byte[]> files) {
        Gson gson = new Gson();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                for (byte[] f : files) {
                    ClientFileSender.send(connectedPeer.getIPAddress(), 8888, f);
                }

            }
        }).start();
    }

    //set the network address of the connected peer
    public void setConnectedPeerIpAddress(String hostAddress, String macAddress) {

        if (connectedPeer != null) {
            if (macAddress != null)
                connectedPeer.setMacAddress(macAddress);
            connectedPeer.setIsConnected(true);
            connectedPeer.setIPAddress(hostAddress);
        } else {
            connectedPeer = new Peer("", macAddress, hostAddress, true);
        }
        //store the information
        storePeers();
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
        if (localDevice == null) {
            localDevice = new Peer(android.os.Build.MODEL, "");
        }
        localDevice.setIPAddress(LocalIPFinder.getLocalDottedDecimalIPAddress());
        localDevice.setUserContext(((P2PApplicationContext) mActivity.getApplication()).gettUserContext());
        localDevice.setIsConnected(true);
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

    private void clear() {
        // mActivity.getApplication().getSharedPreferences("P2P", Context.MODE_PRIVATE).edit().clear().commit();
    }

    public void clearInfo() {
        connectedPeer = null;
        clear();
    }

    public void logMsg(Message msg) {
        DatabaseHelper helper = OpenHelperManager.getHelper(mActivity, DatabaseHelper.class);
        try {
            MyMsg myMsg = new MyMsg();
            myMsg.setValue(msg.toJson());
            helper.getMsgDao().create(myMsg);
        } catch (SQLException e) {
            android.util.Log.d("Ali", e.getMessage());
        }
    }


    public boolean isHandshake(String deviceAddress) {
        if (connectedPeer == null)
            return false;
        if (connectedPeer.getMacAddress().equalsIgnoreCase(deviceAddress)) {
            if (connectedPeer.getUserContext() != null)
                return true;
        }
        return false;
    }

    public Peer getConnectedPeer() {
        return connectedPeer;
    }

    public void setConnectedPeer(Peer connectedPeer) {
        this.connectedPeer = connectedPeer;
    }
}
