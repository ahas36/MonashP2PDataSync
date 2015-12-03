package monash.infotech.monashp2pdatasync.connectivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import monash.infotech.monashp2pdatasync.app.HomeActivity;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private HomeActivity mActivity;
    private List<WifiP2pDevice> peers = new ArrayList();

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            peers.clear();
            peers.addAll(peerList.getDeviceList());

            // Data you need to pass to activity
            mActivity.viewPeers(peers);
            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of available
            // peers, trigger an update.
            //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            if (peers.size() == 0) {
                //    Log.d(WiFiDirectActivity.TAG, "No devices found");
                return;
            }
        }
    };


    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Activity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = (HomeActivity) activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Toast.makeText(context, "Wifi P2P is enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Wi-Fi P2P is not enabled
                Toast.makeText(context, "Wi-Fi P2P is not enabled", Toast.LENGTH_SHORT).show();
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);

            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            Toast.makeText(context, "connected/disconnected", Toast.LENGTH_LONG).show();
            if (mManager == null) {
                ConnectionManager.getManager().clearInfo();
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);


            if (networkInfo.isConnected()) {

                // We are connected with the other device, request connection
                // info to find group owner IP
                //mActivity.setLocalIPAddress(LocalIPFinder.getgetLocalDottedDecimalIPAddress());

                mManager.requestConnectionInfo(mChannel, connectionListener);

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                ConnectionManager.getManager().clearInfo();
            }
        }
    }

    WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            // InetAddress from WifiP2pInfo struct.
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            // After the group negotiation, we can determine the group owner.
            if (info.groupFormed && info.isGroupOwner) {
               // ConnectionManager.getManager().updateLocalPeer();
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a server thread and accepting
                // incoming connections.
                //mActivity.cm.setGroupOwnerIPAddress(groupOwnerAddress);
            } else if (info.groupFormed) {
                if (!ConnectionManager.getManager().getLocalDevice().getIPAddress().equals(groupOwnerAddress.getHostAddress())) {
                    ConnectionManager.getManager().setIpAddress(groupOwnerAddress.getHostAddress(), "");
                    ConnectionManager.getManager().sendHandshakeMsg();
                }
                // The other device acts as the client. In this case,
                // you'll want to create a client thread that connects to the group
                // owner.
            }

        }
    };

}
