package monash.infotech.monashp2pdatasync.handshake;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

import monash.infotech.monashp2pdatasync.connectivity.p2p.ConnectionManager;
import monash.infotech.monashp2pdatasync.entities.Peer;
import monash.infotech.monashp2pdatasync.messaging.Message;
import monash.infotech.monashp2pdatasync.messaging.MessageCreator;
import monash.infotech.monashp2pdatasync.messaging.MessageType;
import monash.infotech.monashp2pdatasync.security.Security;
import monash.infotech.monashp2pdatasync.sync.SyncManager;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponse;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponseType;

/**
 * Created by john on 1/25/2016.
 * Handle the handshake procedure
 */
public class HandshakeManager {


    //send handshake message

    public static void sendHandshake() throws JSONException, SQLException {
        //get an instance of connection manager
        ConnectionManager connectionManager = ConnectionManager.getManager();
        //retrieve local device and connected peer address
        connectionManager.updateLocalPeer();
        Peer localDevice = connectionManager.getLocalDevice();
        Peer connectedPeer = connectionManager.getConnectedDevice();
        //create handshake msg
        Message msg = MessageCreator.createHandshakeMessage(MessageType.handshake);
        //send the msg
        connectionManager.sendFile(msg.toJson());
    }


    //send handshake response message

    private static void sendHandshakeResponse() throws JSONException, SQLException {
        //get an instance of connection manager
        ConnectionManager connectionManager = ConnectionManager.getManager();
        //create handshake response msg
        Message msg = MessageCreator.createHandshakeMessage(MessageType.handshakeResponse);
        //send the msg
        connectionManager.sendFile(msg.toJson());
    }



    //handle an incoming handshake message

    public static boolean handleHandshake(Message msg) throws JSONException, SQLException {
        JSONObject msgBody=new JSONObject(msg.getMsgBody());
        //read and decrypt token
        String token=msgBody.getString("token");
        long lastSync=msgBody.getLong("lastSync");
        try {
            if(!Security.getInstance().authenticate(msg.getSender().getUserContext(),token)) {
                SyncManager.sendSynEndMsg(new SyncResponse(SyncResponseType.FAIL, "authentication faild"));
                return false;
            }
        } catch (JSONException e) {
            return false;
            //// TODO: 1/27/2016 add exception 
        }
        ConnectionManager connectionManager = ConnectionManager.getManager();
        connectionManager.updateLocalPeer();
        //update connected peer
        connectionManager.setConnectedPeerIpAddress(msg.getSender().getIPAddress(), msg.getSender().getMacAddress());
        connectionManager.getConnectedDevice().setMacAddress(msg.getSender().getMacAddress());
        connectionManager.getConnectedDevice().setUserContext(msg.getSender().getUserContext());
        connectionManager.getConnectedDevice().setLastSync(lastSync);
        //send handshake response
        sendHandshakeResponse();
        return true;
    }


    //handle an incoming handshake response message

    public static boolean  handleHandshakeResponse(Message msg) throws JSONException {
        JSONObject msgBody=new JSONObject(msg.getMsgBody());
        //read and decrypt token
        String token=msgBody.getString("token");
        long lastSync=msgBody.getLong("lastSync");
        try {
            if(!Security.getInstance().authenticate(msg.getSender().getUserContext(),token)) {
                SyncManager.sendSynEndMsg(new SyncResponse(SyncResponseType.FAIL, "authentication faild"));
                return false;
            }
        } catch (JSONException e) {
            return false;
            //// TODO: 1/27/2016 add exception
        }
        ConnectionManager connectionManager = ConnectionManager.getManager();
        //update connected peer
        connectionManager.setConnectedPeerIpAddress(msg.getSender().getIPAddress(), msg.getSender().getMacAddress());
        connectionManager.getConnectedDevice().setMacAddress(msg.getSender().getMacAddress());
        connectionManager.getConnectedDevice().setUserContext(msg.getSender().getUserContext());
        connectionManager.getConnectedDevice().setLastSync(lastSync);
        return true;
    }
}
