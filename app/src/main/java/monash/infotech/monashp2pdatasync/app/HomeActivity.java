package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.app.Fragment;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.connectivity.p2p.ConnectionManager;

public class HomeActivity extends Fragment {

    //instance of connection manager
    public ConnectionManager cm;
    Activity ac;
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_main, container, false);
        ac = getActivity();
        cm = ConnectionManager.getManager();
        cm.init(ac, this);


        //discover peers
        cm.startPeerDiscovery(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(ac, "onFailure discoverPeers", Toast.LENGTH_SHORT).show();
            }
        });

        ((Button) rootView.findViewById(R.id.dc)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cm.disconnect();
                cm.startPeerDiscovery(new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(ac, "onFailure discoverPeers", Toast.LENGTH_SHORT).show();
                    }
                });
                cm.startFileServer();
            }
        });
        ((Button) rootView.findViewById(R.id.getInfo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cm.getLocalDevice()!=null && cm.getConnectedDevice()!=null) {
                    ((TextView) rootView.findViewById(R.id.local)).setText(cm.getLocalDevice().getIPAddress() + "," + cm.getLocalDevice().getMacAddress() + "," + cm.getLocalDevice().getDeviceName() + "," + cm.getLocalDevice().isConnected());
                    ((TextView) rootView.findViewById(R.id.connected)).setText(cm.getConnectedDevice().getIPAddress() + "," + cm.getConnectedDevice().getMacAddress() + "," + cm.getConnectedDevice().getDeviceName() + "," + cm.getConnectedDevice().isConnected());
                    updatePeers();
                }
            }
        });
        return rootView;
    }
    public void updatePeers()
    {
        ((BaseAdapter)((ListView) rootView.findViewById(R.id.PeersList)).getAdapter()).notifyDataSetChanged();
    }

    //load the available peers into list view
    public void viewPeers(List<WifiP2pDevice> peers) {
        //filter peers to only contain mobile devices
        List<WifiP2pDevice> filteredPeers = new ArrayList<>();
        for (WifiP2pDevice p : peers) {
            if (p.primaryDeviceType.equals("10-0050F204-5")) {
                //Toast.makeText(context,p.deviceName,Toast.LENGTH_LONG);
                filteredPeers.add(p);
            }
        }
        PeerArrayAdapter itemsAdapter =
                new PeerArrayAdapter(ac, filteredPeers);
        ((ListView) rootView.findViewById(R.id.PeersList)).setAdapter(itemsAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        cm.registerReceiver();
    }

    /* unregister the broadcast receiver */
    @Override
    public void onPause() {
        super.onPause();
        cm.unregisterReceiver();
    }

}
