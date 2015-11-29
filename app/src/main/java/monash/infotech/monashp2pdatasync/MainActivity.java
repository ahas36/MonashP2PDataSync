package monash.infotech.monashp2pdatasync;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import monash.infotech.monashp2pdatasync.connectivity.ConnectionManager;

public class MainActivity extends Activity {
    //popup window for showing peers
    PopupWindow pwindo;
    //instance of connection manager
    public ConnectionManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cm=ConnectionManager.getManager();
        cm.init(this);

        //button for show available peers
                ((Button) findViewById(R.id.send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //discover peers
                cm.startPeerDiscovery(new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        LayoutInflater inflater = (LayoutInflater) MainActivity.this
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.sync_popup,
                                (ViewGroup) findViewById(R.id.popup_element));
                        //get the screen size
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int width = size.x;
                        int height = size.y;

                        //create a popup window
                        pwindo = new PopupWindow(layout, (int) (width * .8), (int) (height * .85), true);
                        // Closes the popup window when touch outside.
                        pwindo.setFocusable(true);
                        pwindo.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        //override default to stop discovery
                        pwindo.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                //commented as it stop other devices to find this peer, however if we add a ready to sync option and turn on the discovery in this case, the battery usage could be optimize
                                // mManager.stopPeerDiscovery(mChannel,null);
                                pwindo.dismiss();
                            }
                        });
                        //display the window
                        pwindo.showAtLocation(layout, Gravity.BOTTOM, 0, 30);

                        //sync btn clicked
                        ((Button) pwindo.getContentView().findViewById(R.id.BtnSync)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //if device is connected
                            }
                        });
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this, "onFailure discoverPeers", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        ((Button)findViewById(R.id.getInfo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.local)).setText(cm.getLocalDevice().getIPAddress()+","+cm.getLocalDevice().getMacAddress()+","+cm.getLocalDevice().getDeviceName()+","+cm.getLocalDevice().isConnected());
                ((TextView)findViewById(R.id.connected)).setText(cm.getConnectedDevice().getIPAddress()+","+cm.getConnectedDevice().getMacAddress()+","+cm.getConnectedDevice().getDeviceName()+","+cm.getConnectedDevice().isConnected());
            }
        });
    }

    //load the available peers into list view
    public void viewPeers( List<WifiP2pDevice> peers)
    {
        //filter peers to only contain mobile devices
        List<WifiP2pDevice> filteredPeers=new ArrayList<>();
        for(WifiP2pDevice p:peers)
        {
            if(p.primaryDeviceType.equals("10-0050F204-5"))
            {
                //Toast.makeText(context,p.deviceName,Toast.LENGTH_LONG);
                filteredPeers.add(p);
            }
        }
        PeerArrayAdapter itemsAdapter =
                new PeerArrayAdapter(MainActivity.this,filteredPeers);
        ((ListView)pwindo.getContentView().findViewById(R.id.PeersList)).setAdapter(itemsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
    protected void onResume() {
        super.onResume();
        cm.registerReceiver();
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        cm.unregisterReceiver();
    }

}
