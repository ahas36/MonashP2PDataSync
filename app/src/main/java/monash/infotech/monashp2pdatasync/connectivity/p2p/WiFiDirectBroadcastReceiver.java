package monash.infotech.monashp2pdatasync.connectivity.p2p;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import org.json.JSONException;

import java.net.InetAddress;
import java.security.spec.ECField;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import monash.infotech.monashp2pdatasync.app.HomeActivity;
import monash.infotech.monashp2pdatasync.entities.Peer;
import monash.infotech.monashp2pdatasync.handshake.HandshakeManager;
import monash.infotech.monashp2pdatasync.messaging.Message;
import monash.infotech.monashp2pdatasync.messaging.MessageCreator;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponse;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponseType;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Fragment mActivity;
    private List<WifiP2pDevice> peers = new ArrayList();
    //A listener
    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // clear the list
            peers.clear();
            //add all the available peers
            peers.addAll(peerList.getDeviceList());

            // pass all the available peers to activity
            ((HomeActivity) mActivity).viewPeers(peers);

            if (peers.size() == 0) {
                //    android.util.Log.d(WiFiDirectActivity.TAG, "No devices found");
                return;
            }
        }
    };


    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Fragment activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
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
                WifiP2pDevice p2pGroupInfo = ((WifiP2pGroup) intent.getExtras().get("p2pGroupInfo")).getOwner();
                if(!ConnectionManager.getManager().getLocalDevice().getMacAddress().equalsIgnoreCase(p2pGroupInfo.deviceAddress)) {
                    Peer p = new Peer(p2pGroupInfo.deviceName, p2pGroupInfo.deviceAddress);
                    ConnectionManager.getManager().setConnectedPeer(p);
                }
                    // We are connected with the other device, request connection
                    // info to find group owner IP
                    mManager.requestConnectionInfo(mChannel, connectionListener);

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing

            }
        }
    }

    WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            //setup local device
            if (ConnectionManager.getManager().getLocalDevice() == null) {
                ConnectionManager.getManager().updateLocalPeer();
            }
            // InetAddress from WifiP2pInfo struct.
            InetAddress groupOwnerAddress = info.groupOwnerAddress;
            // After the group negotiation, we can determine the group owner.
            if (info.groupFormed && info.isGroupOwner) {
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a server thread and accepting
                // incoming connections.

            } else if (info.groupFormed) {
                if (!ConnectionManager.getManager().getLocalDevice().getIPAddress().equals(groupOwnerAddress.getHostAddress())) {
                    ConnectionManager.getManager().setConnectedPeerIpAddress(groupOwnerAddress.getHostAddress(), null);
                    try {
                        HandshakeManager.sendHandshake();
                    } catch (Exception e) {
                        HandshakeManager.sendHandshakeFailMessage("failed to generate and send handshake msg");
                        e.printStackTrace();
                    }
                }

                // The other device acts as the client. In this case,
                // you'll want to create a client thread that connects to the group
                // owner.
            }

        }
    };

}
