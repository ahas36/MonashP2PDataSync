package monash.infotech.monashp2pdatasync;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import monash.infotech.monashp2pdatasync.connectivity.ConnectionManager;
import monash.infotech.monashp2pdatasync.messaging.Message;
import monash.infotech.monashp2pdatasync.messaging.MessageType;

/**
 * Created by john on 11/21/2015.
 */
public class PeerArrayAdapter extends ArrayAdapter<WifiP2pDevice> {
    private final Context context;
    private final List<WifiP2pDevice> values;
    private final ConnectionManager cm;

    public PeerArrayAdapter(Context context, List<WifiP2pDevice> values) {
        super(context, -1, values);
        this.context = context;

        this.values = values;

        this.cm = ConnectionManager.getManager();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.discovery_list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.peerTitle);
        final Button button = (Button) rowView.findViewById(R.id.btnConnect);
        final WifiP2pDevice device = values.get(position);
        textView.setText(device.deviceName + " " + device.primaryDeviceType+" "+device.status);
        if (device.status==0) {
            button.setText("Sync");
        } else {
            button.setText("Connect");
        }

        // change the icon for Windows and iPhone
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button.getText().toString().equals("Sync")) {
                    Message m=new Message(0, MessageType.ping,null,null,"ping"+ Calendar.getInstance().getTimeInMillis());
                    cm.sendFile(device,m.toJson());
                } else {
                    cm.connect(device);
                }
            }
        });

        return rowView;
    }

}

