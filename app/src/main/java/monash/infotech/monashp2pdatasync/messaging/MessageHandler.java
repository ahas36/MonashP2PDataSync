package monash.infotech.monashp2pdatasync.messaging;

import org.json.JSONException;

import java.sql.SQLException;

import monash.infotech.monashp2pdatasync.connectivity.p2p.ConnectionManager;
import monash.infotech.monashp2pdatasync.handshake.HandshakeManager;
import monash.infotech.monashp2pdatasync.sync.SyncManager;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponse;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponseType;

/**
 * Created by john on 1/26/2016.
 */
public class MessageHandler {

    public static void HandleMessage(Message  message) throws JSONException, SQLException {

        ConnectionManager.getManager().logMsg(message);
        switch (message.getType())
        {
            case handshake:
                HandshakeManager.handleHandshake(message);
                break;
            case handshakeResponse:
                HandshakeManager.handleHandshakeResponse(message);
                break;
            case ping:
                break;
            case syncEnd:
                try {
                    SyncManager.handelSynEndMsg();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case syncRequest:
                try {
                    SyncManager.handleSyncRequest(message);
                    SyncManager.sendSyncRespond();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case syncRespond:
                try {
                    SyncManager.handleSyncRequest(message);
                    SyncManager.sendSynEndMsg(new SyncResponse(SyncResponseType.SUCCESS,""));
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
