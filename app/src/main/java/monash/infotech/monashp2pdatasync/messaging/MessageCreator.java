package monash.infotech.monashp2pdatasync.messaging;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import monash.infotech.monashp2pdatasync.connectivity.p2p.ConnectionManager;
import monash.infotech.monashp2pdatasync.data.db.DatabaseManager;
import monash.infotech.monashp2pdatasync.entities.ConflictMethodType;
import monash.infotech.monashp2pdatasync.entities.HandleSyncResult;
import monash.infotech.monashp2pdatasync.entities.KeyVal;
import monash.infotech.monashp2pdatasync.entities.Peer;
import monash.infotech.monashp2pdatasync.entities.SyncHistory;
import monash.infotech.monashp2pdatasync.entities.form.Form;
import monash.infotech.monashp2pdatasync.entities.form.FormItem;
import monash.infotech.monashp2pdatasync.entities.form.FormType;
import monash.infotech.monashp2pdatasync.entities.form.HandleSyncResultType;
import monash.infotech.monashp2pdatasync.entities.form.Log;
import monash.infotech.monashp2pdatasync.entities.form.LogItems;
import monash.infotech.monashp2pdatasync.entities.form.LogType;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponse;

/**
 * Created by john on 12/8/2015.
 * A class that create different type of messages
 */
public class MessageCreator {
    //Store the users token
    private static String token;

    //init the message creator
    public static void init(String token) {

        MessageCreator.token = token;
    }

    //generate handshake message; MessageType could be handshake and handshakeResponse
    public static Message createHandshakeMessage(MessageType msgType) throws JSONException, SQLException {
        //get sender and reciver from connection manager
        ConnectionManager manager = ConnectionManager.getManager();
        Peer sender = manager.getLocalDevice();
        Peer reciver = manager.getConnectedDevice();
        Message msg = null;
        //get the message id
        try {
            msg = new Message(DatabaseManager.SequencePlusPlus("msgNo"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //set the sender
        msg.setSender(sender);
        //set the reciver
        msg.setReciver(reciver);
        //set msg type
        msg.setType(msgType);
        //set msg body which is token for authentication
        JSONObject msgBody=new JSONObject();
        msgBody.put("token", token);
        SyncHistory syncHistory = DatabaseManager.getSyncHistoryDao().queryForId(reciver.getMacAddress());
        long lastSync = syncHistory == null ? 0 : syncHistory.getSynTime();
        msgBody.put("lastSync",lastSync);
        msg.setMsgBody(msgBody.toString());
        return msg;
    }

    //generate end sync message
    public static Message createSyncEndMsg(SyncResponse response) {
        //get sender and reciver from connection manager
        ConnectionManager manager = ConnectionManager.getManager();
        Peer sender = manager.getLocalDevice();
        Peer reciver = manager.getConnectedDevice();
        //set msg id
        Message msg = null;
        try {
            msg = new Message(DatabaseManager.SequencePlusPlus("msgNo"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        msg.setSender(sender);
        msg.setReciver(reciver);
        msg.setType(MessageType.syncEnd);
        Gson gson = new Gson();
        msg.setMsgBody(gson.toJson(response));
        return msg;
    }
    //create a json msg that contains information regarding the sync request items that modified during the sync processes
    public static JSONArray SyncRespondMsg(List<HandleSyncResult> handleSyncResults) throws JSONException {
        JSONArray syncResult= new JSONArray();
        Map<String,List<HandleSyncResult>> resultMap=new HashMap<>();
        for (HandleSyncResult h:handleSyncResults)
        {
            if(resultMap.containsKey(h.getSenderFormID()))
            {
                List<HandleSyncResult> tempHandleSyncResult = resultMap.get(h.getSenderFormID());
                tempHandleSyncResult.add(h);
                resultMap.put(h.getSenderFormID(),tempHandleSyncResult);
            }
            else
            {
                List<HandleSyncResult> tempL=new ArrayList<>();
                tempL.add(h);
                resultMap.put(h.getSenderFormID(),tempL);
            }

        }
        for (Map.Entry<String,List<HandleSyncResult>> entry : resultMap.entrySet()) {

            JSONObject formJson = new JSONObject();
            formJson.put("form_id", entry.getKey());
            JSONArray items=new JSONArray();
            for (HandleSyncResult hsr:entry.getValue()) {
                JSONObject syncResultLog=new JSONObject();
                if(!hsr.getType().equals(HandleSyncResultType.LOST))
                {
                    syncResultLog.put("item_id", hsr.getItemId());
                    syncResultLog.put("value", hsr.getValue());
                    items.put(syncResultLog);
                }
            }
            formJson.put("items",items);
            syncResult.put(formJson);
        }
        return syncResult;
    }
    //generate sync request message
    public static Message createSyncResponse(List<HandleSyncResult> handleSyncResults) throws SQLException, JSONException {
        Message msg = null;
        try {
            msg = new Message(DatabaseManager.SequencePlusPlus("msgNo"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //get the DAOs
        Dao<LogItems, Integer> logItemDao = DatabaseManager.getLogItemDao();

        //get sender and reciver from connection manager
        ConnectionManager manager = ConnectionManager.getManager();
        Peer sender = manager.getLocalDevice();
        Peer reciver = manager.getConnectedDevice();

        //find the last time that the sync happened with the connected peer

        long lastSync = reciver.getLastSync();

        //create a query that returns all the log items that created after the last sync
        //////////////////////////0///////////1///////////2/////////3///////////4/////////////////////5//////////////6///////////7///////////
        String query = "select it.inputType,li.item_id,li.value,l.logType,it.conflictResolveMethod,l.logtimestamp,li.log_id,l.form_id from  logitems li  join log l on li.log_id= l.logid join\n" +
                "(select l2.form_id,li2.item_id,max(l2.logtimestamp) as max from logitems li2  join log l2 on li2.log_id= l2.logid group by li2.item_id,l2.form_id) q on q.item_id=li.item_id and q.max=l.logtimestamp" +
                " join items it on it.itemId=li.item_id where  it.accessLvl<= " + reciver.getUserContext().getRole().ordinal() + " and  l.logtimestamp>" + lastSync;
        GenericRawResults<String[]> values = logItemDao.queryRaw(query);
        String[] columnNames = values.getColumnNames();
        JSONArray json = new JSONArray();
        JSONArray syncResult= SyncRespondMsg(handleSyncResults);
        for (String[] value : values.getResults()) {

            Stream<HandleSyncResult> filter = Stream.of(handleSyncResults).filter(hsr -> hsr.getLocalFormID().equals(value[7]) && hsr.getItemId()== Integer.valueOf(value[1]));
            Optional<HandleSyncResult> formItemOptional = filter.findFirst();
            if (formItemOptional.isPresent()) {
                    continue;
            }
            if (value[0].equals("SOUND") || value[0].equals("VIDEO") || value[0].equals("IMAGE")) {
                msg.addFile(value[2]);
            }
            JSONObject formJson = null;
            for (int i = 0; i < json.length(); i++) {
                if (json.getJSONObject(i).has("form_id") && json.getJSONObject(i).getString("form_id").equals(value[columnNames.length - 1])) {
                    formJson = json.getJSONObject(i);
                    break;
                }
            }
            if (formJson == null) {
                //get form and its type
                String formId = value[columnNames.length - 1];
                Form form = DatabaseManager.getFormDao().queryForId(formId);
                FormType formType = DatabaseManager.getFormTypeDao().queryForId(form.getFormType().getFormTypeId());
                //create and add the form to the log msg
                formJson = new JSONObject();
                formJson.put("form_id", formId);
                formJson.put("logs", new JSONArray());
                formJson.put("form_type", formType.getFormTypeId());
                //add semantic key
                JSONObject semanticKey = new JSONObject();
                String semanticKeyColumn = formType.getSemanticKey();
                JSONArray semanticKeyJsonArray = new JSONArray(semanticKeyColumn);
                //if there is any semantic key
                List<KeyVal> semanticItemList = new ArrayList<>();
                if (semanticKeyJsonArray.length() > 0) {
                    for (int semanticCounter = 0; semanticCounter < semanticKeyJsonArray.length(); semanticCounter++) {
                        String fieldName = semanticKeyJsonArray.getString(semanticCounter);
                        String semanticValueRawQuery = "select fi.value,fi.item_id from formitem as fi join items on items.ItemId=fi.item_id where items.ItemTitle=\"" + fieldName + "\" and fi.form_id=\"" + formId + "\"";
                        String[] firstResult = DatabaseManager.getFormItemDao().queryRaw(semanticValueRawQuery).getFirstResult();
                        String semanticValue = firstResult[0];
                        String id = firstResult[1];
                        semanticItemList.add(new KeyVal(id, semanticValue));
                    }
                    Collections.sort(semanticItemList);
                    String ids = "";
                    String vals = "";
                    for (KeyVal item : semanticItemList) {
                        ids += item.getKey() + ",";
                        vals += item.getValue() + ",";
                    }
                    semanticKey.put("ids", ids.substring(0, ids.length() - 1));
                    semanticKey.put("vals", vals.substring(0, vals.length() - 1));
                    formJson.put("semanticKey", semanticKey);
                }
                json.put(formJson);
            }
            JSONObject log = null;
            JSONArray log_items = formJson.getJSONArray("logs");
            for (int i = 0; i < log_items.length(); i++) {
                if (log_items.getJSONObject(i).has("log_id") && log_items.getJSONObject(i).getString("log_id").equals(value[columnNames.length - 2])) {
                    log = log_items.getJSONObject(i);
                    break;
                }
            }
            if (log == null) {
                log = new JSONObject();
                log.put("logtimestamp", value[columnNames.length - 3]);
                log.put("log_id", value[columnNames.length - 2]);
                log.put("log_items", new JSONArray());
                log.put("log_type", value[3]);
                log_items.put(log);
            }
            JSONObject logItem = new JSONObject();
            logItem.put(columnNames[1], value[1]);
            logItem.put(columnNames[2], value[2]);
            log.getJSONArray("log_items").put(logItem);
        }
        Gson gson = new Gson();

        msg.setSender(sender);
        msg.setReciver(reciver);
        msg.setType(MessageType.syncRespond);
        JSONObject syncRespondJson=new JSONObject();
        syncRespondJson.put("respond",syncResult);
        syncRespondJson.put("request",syncResult);
        msg.setMsgBody(syncRespondJson.toString());
        return msg;
    }

    public static Message createSyncRequest() throws SQLException, JSONException {
        Message msg = null;
        try {
            msg = new Message(DatabaseManager.SequencePlusPlus("msgNo"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //get sender and reciver from connection manager
        ConnectionManager manager = ConnectionManager.getManager();
        Peer sender = manager.getLocalDevice();
        Peer reciver = manager.getConnectedDevice();
        //get log dao
        Dao<LogItems, Integer> logItemDao = DatabaseManager.getLogItemDao();
        //retrieve the last time that data get synced with the connceted peer
        long lastSync = reciver.getLastSync();
        //query log items to retrieve all the new changes for reciver peer
        String query = "select it.inputType,li.item_id,li.value,l.logtimestamp,li.log_id,l.form_id from  logitems li  join log l on li.log_id= l.logid join\n" +
                "(select l2.form_id,li2.item_id,max(l2.logtimestamp) as max from logitems li2  join log l2 on li2.log_id= l2.logid group by li2.item_id,l2.form_id) q on q.item_id=li.item_id and q.max=l.logtimestamp" +
                " join items it on it.itemId=li.item_id where it.accessLvl<= " + reciver.getUserContext().getRole().ordinal() + " and  l.logtimestamp>" + lastSync;
        GenericRawResults<String[]> values = logItemDao.queryRaw(query);
        String[] columnNames = values.getColumnNames();
        JSONArray json = new JSONArray();
        for (String[] value : values.getResults()) {
            if(lastSync<Long.valueOf(value[3]))
            {
                JSONObject formJson = null;
                formJson = new JSONObject();
                formJson.put("asd","qwe");
            }
            if (value[0].equals("SOUND") || value[0].equals("VIDEO") || value[0].equals("IMAGE")) {
                msg.addFile(value[2]);
            }
            JSONObject formJson = null;
            for (int i = 0; i < json.length(); i++) {
                if (json.getJSONObject(i).has("form_id") && json.getJSONObject(i).getString("form_id").equals(value[columnNames.length - 1])) {
                    formJson = json.getJSONObject(i);
                    break;
                }
            }
            if (formJson == null) {
                //get form and form type
                String formId = value[columnNames.length - 1];
                Form form = DatabaseManager.getFormDao().queryForId(formId);
                FormType formType = DatabaseManager.getFormTypeDao().queryForId(form.getFormType().getFormTypeId());
                //create and add the form to the log msg
                formJson = new JSONObject();
                formJson.put("form_id", formId);
                formJson.put("logs", new JSONArray());
                formJson.put("form_type", formType.getFormTypeId());
                //add semantic key
                JSONObject semanticKey = new JSONObject();
                String semanticKeyColumn = formType.getSemanticKey();
                JSONArray semanticKeyJsonArray = new JSONArray(semanticKeyColumn);
                //if there is any semantic key
                List<KeyVal> semanticItemList = new ArrayList<>();
                if (semanticKeyJsonArray.length() > 0) {
                    for (int semanticCounter = 0; semanticCounter < semanticKeyJsonArray.length(); semanticCounter++) {
                        String fieldName = semanticKeyJsonArray.getString(semanticCounter);
                        String semanticValueRawQuery = "select fi.value,fi.item_id from formitem as fi join items on items.ItemId=fi.item_id where items.ItemTitle=\"" + fieldName + "\" and fi.form_id=\"" + formId + "\"";
                        String[] firstResult = DatabaseManager.getFormItemDao().queryRaw(semanticValueRawQuery).getFirstResult();
                        if(firstResult==null)
                            continue;
                        String semanticValue = firstResult[0];
                        String id = firstResult[1];
                        semanticItemList.add(new KeyVal(id, semanticValue));
                    }
                    Collections.sort(semanticItemList);
                    String ids = "";
                    String vals = "";
                    for (KeyVal item : semanticItemList) {
                        ids += item.getKey() + ",";
                        vals += item.getValue() + ",";
                    }
                    semanticKey.put("ids", ids.substring(0, ids.length() - 1));
                    semanticKey.put("vals", vals.substring(0, vals.length() - 1));
                    formJson.put("semanticKey", semanticKey);
                }
                json.put(formJson);
            }
            JSONObject log = null;
            JSONArray log_items = formJson.getJSONArray("logs");
            for (int i = 0; i < log_items.length(); i++) {
                if (log_items.getJSONObject(i).has("log_id") && log_items.getJSONObject(i).getString("log_id").equals(value[columnNames.length - 2])) {
                    log = log_items.getJSONObject(i);
                    break;
                }
            }
            if (log == null) {
                log = new JSONObject();
                log.put("logtimestamp", value[columnNames.length - 3]);
                log.put("log_id", value[columnNames.length - 2]);
                log.put("log_items", new JSONArray());
                log_items.put(log);
            }
            JSONObject logItem = new JSONObject();
            logItem.put(columnNames[1], value[1]);
            logItem.put(columnNames[2], value[2]);
            log.getJSONArray("log_items").put(logItem);
        }
        Gson gson = new Gson();

        msg.setSender(sender);
        msg.setReciver(reciver);
        msg.setType(MessageType.syncRequest);
        msg.setMsgBody(json.toString());
        return msg;
    }
}
