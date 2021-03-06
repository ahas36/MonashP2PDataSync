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
 * Based on msg type, call the corresponding method to handle it
 */
public class MessageHandler {

    public static void HandleMessage(Message message) {

        ConnectionManager.getManager().logMsg(message);
        switch (message.getType()) {
            case handshake:
                try {
                    HandshakeManager.handleHandshake(message);
                } catch (Exception e) {
                    HandshakeManager.sendHandshakeFailMessage("failed to handle handshake"+e.getMessage()!=null?" msg = "+e.getMessage():"");
                }
                break;
            case handshakeResponse:
                try {
                    HandshakeManager.handleHandshakeResponse(message);
                }
                catch (Exception e)
                {
                    HandshakeManager.sendHandshakeFailMessage("failed to handle handshake response"+e.getMessage()!=null?" msg = "+e.getMessage():"");
                }
                break;
            case syncEnd:
                try {
                    SyncManager.handelSynEndMsg(message);
                } catch (Exception e) {
                    SyncManager.sendSynFailMsg("failed to handel sync end msg"+e.getMessage()!=null?" msg = "+e.getMessage():"");
                    ConnectionManager.getManager().disconnect();
                    e.printStackTrace();
                }
                break;
            case syncRequest:
                try {
                    SyncManager.handelSyncRequest(message);
                } catch (Exception e) {
                    SyncManager.sendSynFailMsg("failed to handle sync request"+e.getMessage()!=null?" msg = "+e.getMessage():"");
                    e.printStackTrace();
                }
                break;
            case syncRespond:
                try {

                    SyncManager.handleSyncResponse(message);
                } catch (Exception e) {
                    SyncManager.sendSynFailMsg("failed to handle sync response"+e.getMessage()!=null?" msg = "+e.getMessage():"");
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
