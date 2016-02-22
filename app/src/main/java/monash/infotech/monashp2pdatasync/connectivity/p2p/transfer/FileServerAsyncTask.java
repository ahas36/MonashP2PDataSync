package monash.infotech.monashp2pdatasync.connectivity.p2p.transfer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.messaging.Message;
import monash.infotech.monashp2pdatasync.messaging.MessageHandler;
import monash.infotech.monashp2pdatasync.messaging.MessageParser;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileServerAsyncTask extends AsyncTask<Void, Void, Void> {

    private Activity context;
    private TextView statusText;

    public FileServerAsyncTask(Activity context, View statusText) {
        this.context = context;
        this.statusText = (TextView) statusText;
    }

    @Override
    protected Void doInBackground(Void... params) {
        ServerSocket serverSocket = null;
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            serverSocket = new ServerSocket(8888);
            while (true) {
                Socket client = serverSocket.accept();
                InputStream inputstream = client.getInputStream();
                byte data[] = new byte[2048];

                ByteArrayOutputStream dest = new ByteArrayOutputStream();
                int count;
                while ((count = inputstream.read(data, 0, 2048)) != -1) {
                    dest.write(data, 0, count);
                }
                if (dest != null) {
                    Message msg= MessageParser.parse(dest.toByteArray());
                    MessageHandler.HandleMessage(msg);
                }
                else
                {
                    break;
                }
            }
        } catch (IOException e) {
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    android.util.Log.d("Ali", e.getMessage());
                }
            }
        }
        return null;
    }

    private Handler messageHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Notification noti = new Notification.Builder(context)
                    .setContentTitle("New MyMsg")
                    .setContentText("asd")
                    .setSmallIcon(R.drawable.refresh)
                    .build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, noti);
            // statusText.setText(result);
        }
    };

    /**
     * Start activity that can handle the JPEG image
     */
    @Override
    protected void onPostExecute(Void result) {

    }


}
