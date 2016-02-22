package monash.infotech.monashp2pdatasync.connectivity.p2p.transfer;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by john on 11/25/2015.
 */
public class ClientFileSender {


    public static void send(String host, int port, byte[] file) {
        int len;
        Socket socket = new Socket();
        byte buf[] = new byte[1024];
        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data will be retrieved by the server device.
             */
            OutputStream outputStream = socket.getOutputStream();
           // ContentResolver cr = context.getContentResolver();
            InputStream inputStream = null;
            inputStream = new ByteArrayInputStream(file);
            ;
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            //catch logic
        } catch (IOException e) {
           // android.util.Log.d("ali",e.getLocalizedMessage());
        }

        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred. */ finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
        }
    }
}
