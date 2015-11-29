package monash.infotech.monashp2pdatasync.connectivity.transfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.AndroidCharacter;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import monash.infotech.monashp2pdatasync.connectivity.ConnectionManager;
import monash.infotech.monashp2pdatasync.messaging.Message;
import monash.infotech.monashp2pdatasync.messaging.MessageType;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileServerAsyncTask extends AsyncTask<Void, Void, Void> {

    private Activity context;
    private TextView statusText;
    private String result="";
    public FileServerAsyncTask(Activity context, View statusText) {
        this.context = context;
        this.statusText = (TextView) statusText;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            ServerSocket serverSocket = new ServerSocket(8888);
            while (true) {
                Socket client = serverSocket.accept();
                InputStream inputstream = client.getInputStream();
                BufferedReader input =
                        new BufferedReader(new InputStreamReader(inputstream));
                result = input.readLine();
                if (result != null) {

                    Message msg=Message.fromJson(result);
                    messageHandler.sendEmptyMessage(0);
                    if(msg.getType().equals(MessageType.handshake))
                    {

                        ConnectionManager.getManager().setIpAddress(msg.getSender().getIPAddress(),msg.getSender().getMacAddress());
                    }
                    if(msg.getType().equals(10))
                    {
                        break;
                    }
                }

            }
            serverSocket.close();
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private Handler messageHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            statusText.setText(result);
        }
    };

    /**
     * Start activity that can handle the JPEG image
     */
    @Override
    protected void onPostExecute(Void result) {

    }


}
