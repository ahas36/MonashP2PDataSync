package monash.infotech.monashp2pdatasync.sync;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import monash.infotech.monashp2pdatasync.connectivity.p2p.ConnectionManager;
import monash.infotech.monashp2pdatasync.data.db.DatabaseManager;
import monash.infotech.monashp2pdatasync.data.log.Logger;
import monash.infotech.monashp2pdatasync.entities.HandleSyncResult;
import monash.infotech.monashp2pdatasync.entities.Peer;
import monash.infotech.monashp2pdatasync.entities.SyncHistory;
import monash.infotech.monashp2pdatasync.entities.form.ConflictItem;
import monash.infotech.monashp2pdatasync.entities.form.Form;
import monash.infotech.monashp2pdatasync.entities.form.FormItem;
import monash.infotech.monashp2pdatasync.entities.form.FormType;
import monash.infotech.monashp2pdatasync.entities.form.HandleSyncResultType;
import monash.infotech.monashp2pdatasync.entities.form.Item;
import monash.infotech.monashp2pdatasync.entities.form.LogType;
import monash.infotech.monashp2pdatasync.messaging.Message;
import monash.infotech.monashp2pdatasync.messaging.MessageCreator;
import monash.infotech.monashp2pdatasync.sync.conflictresolution.ConflictResolution;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponse;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponseType;
import monash.infotech.monashp2pdatasync.utils.Compress;

/**
 * Created by john on 12/8/2015.
 */
public class SyncManager {
    //create and send sync respond
    public static void sendSyncRespond(List<HandleSyncResult> handleSyncResults) {
        try {
            //create sync msg
            Message msg = MessageCreator.createSyncResponse(handleSyncResults);
            //if sync msg has file, compress and send it
            if (msg.getFiles() != null && !msg.getFiles().isEmpty()) {
                byte[] zip = Compress.zip(msg);
                ConnectionManager.getManager().sendFile(zip);
            } else {
                //convert msg to json and send it
                Gson gson = new Gson();
                ConnectionManager.getManager().sendFile(gson.toJson(msg));
            }
        } catch (SQLException e) {

            sendSynEndMsg(new SyncResponse(SyncResponseType.FAIL, e.getMessage()));
            e.printStackTrace();

        } catch (JSONException e) {
            sendSynEndMsg(new SyncResponse(SyncResponseType.FAIL, e.getMessage()));
            e.printStackTrace();
        }
    }
    //handle sync end msg
    public static void handelSynEndMsg(Message msg) throws SQLException, JSONException, IllegalAccessException {
        Gson gson = new Gson();
        //convert json to object
        SyncResponse syncResponse = gson.fromJson(msg.getMsgBody(), SyncResponse.class);
        //if type is success
        if (syncResponse.getType().equals(SyncResponseType.SUCCESS)) {
            //find the sync history by mac address
            SyncHistory syncHistory = DatabaseManager.getSyncHistoryDao().queryForId(ConnectionManager.getManager().getConnectedDevice().getMacAddress());
            if(syncHistory==null) {
                //if not exist crete a new one
                syncHistory=new SyncHistory(ConnectionManager.getManager().getConnectedDevice().getMacAddress(), syncResponse.getLastLogId());
            }
            else
            {
                //update the last sync time(log id)
                syncHistory.setSynTime(syncResponse.getLastLogId());
            }
            //apply changes
            DatabaseManager.getSyncHistoryDao().createOrUpdate(syncHistory);
        }
        //disconnect
        ConnectionManager.getManager().disconnect();
    }

    public static void handleSyncResponse(Message msg) throws SQLException, JSONException, IllegalAccessException {
        //get msg body as json
        JSONObject jsonMsg = new JSONObject(msg.getMsgBody());
        //get list of requested item to change
        JSONArray requestedItems = jsonMsg.has("request")?jsonMsg.getJSONArray("request"):null;
        //get a list of item that generate in respond to sync request (during conflict resolved, the sync request value didn't win )
        JSONArray respondsItem = jsonMsg.has("respond")?jsonMsg.getJSONArray("respond"):null;
        //apply changes based on requested item
        List<HandleSyncResult> handleSyncResults = handleSync(requestedItems, msg.getSender(), msg.getReciver());
        //insert respondsItem
        applyResolvedChanges(respondsItem, msg.getSender());
        SyncResponse syncResponse = new SyncResponse(SyncResponseType.SUCCESS, "");
        //if there is some modified items in respond to requestedItems, send another respond msg
        if (handleSyncResults != null && !handleSyncResults.isEmpty()) {

            sendSyncResponseChangesMsg(handleSyncResults);
        }
        else {
            //inser the last log id into db
            int lastLogId=jsonMsg.getInt("lastLogId");
            SyncHistory syncHistory = DatabaseManager.getSyncHistoryDao().queryForId(ConnectionManager.getManager().getConnectedDevice().getMacAddress());
            if(syncHistory==null) {
                syncHistory=new SyncHistory(ConnectionManager.getManager().getConnectedDevice().getMacAddress(), lastLogId);
            }
            else
            {
                syncHistory.setSynTime(lastLogId);
            }
            DatabaseManager.getSyncHistoryDao().createOrUpdate(syncHistory);
            //create and send end msg
            sendSynEndMsg(syncResponse);
        }
    }

    //send a response msg that only has respond list
    public static void sendSyncResponseChangesMsg(List<HandleSyncResult> handleSyncResults) throws JSONException, SQLException {
        Message msg = MessageCreator.createSyncResponseChangesMsg(handleSyncResults);
        if (msg.getFiles() != null && !msg.getFiles().isEmpty()) {
            byte[] zip = Compress.zip(msg);
            ConnectionManager.getManager().sendFile(zip);
        } else {
            //convert msg to json and send it
            Gson gson = new Gson();
            ConnectionManager.getManager().sendFile(gson.toJson(msg));
        }
    }

    //apply all the changes in respond list and log it
    private static void applyResolvedChanges(JSONArray respondList, Peer sender) throws JSONException, SQLException, IllegalAccessException {
        if(respondList==null)
            return;
        Dao<Form, String> formDao = DatabaseManager.getFormDao();
        Dao<FormItem, Integer> formItemDao = DatabaseManager.getFormItemDao();
        Logger logger = new Logger(sender.getDeviceName());
        for (int i = 0; i < respondList.length(); i++) {
            JSONObject form = respondList.getJSONObject(i);
            //find the form with id
            Form oldForm = formDao.queryForId(form.getString("form_id"));
            Object[] tempArray = oldForm.getItems().toArray();
            FormItem[] oldFormItems = Arrays.copyOf(tempArray, tempArray.length, FormItem[].class);
            JSONArray items = form.getJSONArray("items");

            for (int x = 0; x < items.length(); x++) {
                JSONObject item = items.getJSONObject(x);
                int item_id = item.getInt("item_id");
                Stream<FormItem> formItemOptional = Stream.of(oldForm.getItems()).filter(v -> v.getItem().getItemId() == item_id);
                Optional<FormItem> first = formItemOptional.findFirst();
                FormItem formItem = first.get();
                formItem.setValue(item.getString("value"));
                formItemDao.update(formItem);
            }
            logger.log(oldForm.getFormId(), oldFormItems, LogType.SYNC);
        }
    }


    //apply changes and call conflict resolver if its needed. Return a list of modified items
    public static List<HandleSyncResult> handleSync(JSONArray json, Peer sender, Peer reciver) throws SQLException, JSONException, IllegalAccessException {
        if(json==null)
            return null;
        Dao<Form, String> formDao = DatabaseManager.getFormDao();
        Dao<Item, Integer> itemDao = DatabaseManager.getItemDao();
        Dao<FormItem, Integer> formItemDao = DatabaseManager.getFormItemDao();
        ConflictResolution cr = new ConflictResolution();
        Logger logger = new Logger(sender.getDeviceName());
        FormItem[] oldFormItems = null;
        List<HandleSyncResult> syncResult = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject form = json.getJSONObject(i);
            //find the form with id
            Form oldForm = formDao.queryForId(form.getString("form_id"));
            FormType formType = DatabaseManager.getFormTypeDao().queryForId(form.getInt("form_type"));
            if (oldForm == null) {
                //find the form by semantic key
                if (form.has("semanticKey")) {
                    JSONObject semanticKeyObject = form.getJSONObject("semanticKey");
                    String semanticKeyQuery = "select form_id from (select form_id,group_concat(value) as vals,group_concat(item_id)" +
                            " as itemIds from (select form_id,value,item_id from formitem where item_id in (" + semanticKeyObject.getString("ids") + ") order by item_id) group by form_id) " +
                            "where vals='" + semanticKeyObject.getString("vals") + "' and itemIds='" + semanticKeyObject.getString("ids") + "'";
                    try {
                        String tempFormId = formItemDao.queryRaw(semanticKeyQuery).getFirstResult()[0];
                        oldForm = formDao.queryForId(tempFormId);
                    } catch (Exception e) {
                        //it's a new form
                    }
                }
            }
            //if form not exist, create a new form and add all the items
            if (oldForm == null) {
                Form newForm = new Form();
                newForm.setFormId(form.getString("form_id"));
                newForm.setFormType(formType);
                formDao.create(newForm);
                oldFormItems = new FormItem[]{};
                for (int j = 0; j < form.getJSONArray("logs").length(); j++) {
                    JSONObject logs = form.getJSONArray("logs").getJSONObject(j);
                    for (int z = 0; z < logs.getJSONArray("log_items").length(); z++) {
                        JSONObject logItem = logs.getJSONArray("log_items").getJSONObject(z);
                        FormItem formItem = new FormItem(itemDao.queryForId(logItem.getInt("item_id")), newForm, logItem.getString("value"));
                        //sender win so the sync result is lost (receiver)
                        syncResult.add(new HandleSyncResult(HandleSyncResultType.LOST, formItem.getForm().getFormId(), formItem.getItem().getItemId(), formItem.getValue(), formItem.getForm().getFormId()));
                        formItemDao.create(formItem);
                    }

                }
                logger.log(newForm.getFormId(), oldFormItems, LogType.SYNC);

            } else {
                //if form exist, find the form items
                if (oldForm.getItems() != null) {
                    Object[] tempArray = oldForm.getItems().toArray();
                    oldFormItems = Arrays.copyOf(tempArray, tempArray.length, FormItem[].class);
                } else {
                    oldFormItems = new FormItem[]{};
                }
                //for each log
                for (int j = 0; j < form.getJSONArray("logs").length(); j++) {
                    //get the log
                    JSONObject logs = form.getJSONArray("logs").getJSONObject(j);
                    //for each item in the log
                    for (int z = 0; z < logs.getJSONArray("log_items").length(); z++) {
                        //get the item
                        JSONObject logItem = logs.getJSONArray("log_items").getJSONObject(z);
                        //get the item id
                        int id = logItem.getInt("item_id");
                        //search for the item in the existing form
                        Stream<FormItem> formItemOptional = Stream.of(oldForm.getItems()).filter(v -> v.getItem().getItemId() == id);
                        Optional<FormItem> first = formItemOptional.findFirst();
                        FormItem formItem = null;
                        String value = "";
                        HandleSyncResultType resultType = null;
                        //if item exist
                        if (first.isPresent()) {
                            formItem = first.get();
                            //find item's last modified time
                            String query = "select l.logtimestamp from logitems li join log l on li.log_id= l.logid join (select li2.item_id,max(l2.logtimestamp) as max from logitems li2  join log l2 on li2.log_id= l2.logid  where l2.form_id='" + oldForm.getFormId() + "' and li2.item_id=" + formItem.getItem().getItemId() + " group by li2.item_id) q on q.item_id=li.item_id and q.max=l.logtimestamp where form_id='" + oldForm.getFormId() + "'";
                            String[] firstResult = DatabaseManager.getLogItemDao().queryRaw(query).getFirstResult();
                            String logTime = "0";
                            if (firstResult != null && firstResult.length > 0) {
                                logTime = firstResult[0];
                            }
                            //find the item type
                            Item item = DatabaseManager.getItemDao().queryForId(formItem.getItem().getItemId());
                            //call the conflict resolver to resolve the conflict if its needed and compute the new value
                            value = cr.resolve(new ConflictItem(logItem.getString("value"), logs.getLong("logtimestamp"), item, sender.getUserContext()), new ConflictItem(formItem.getValue(), Long.valueOf(logTime), item, reciver.getUserContext()));
                            if (value.equals(logItem.getString("value"))) {
                                resultType = HandleSyncResultType.LOST;
                            } else {
                                if (value.equals(formItem.getValue())) {
                                    resultType = HandleSyncResultType.WIN;
                                } else {
                                    resultType = HandleSyncResultType.MODIFIED;
                                }
                            }
                        } else {//if item not exist, create new item
                            formItem = new FormItem();
                            formItem.setItem(itemDao.queryForId(id));
                            formItem.setForm(oldForm);
                            value = logItem.getString("value");
                            resultType = HandleSyncResultType.LOST;
                        }
                        //insert or update the form item
                        formItem.setValue(value);
                        syncResult.add(new HandleSyncResult(resultType, form.getString("form_id"), formItem.getItem().getItemId(), formItem.getValue(), formItem.getForm().getFormId()));
                        formItemDao.createOrUpdate(formItem);
                    }

                }
                try {
                    logger.log(oldForm.getFormId(), oldFormItems, LogType.SYNC);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
        return syncResult;
    }

    //receive a sync request msg, make required changes and send a sync response
    public static void handelSyncRequest(Message msg) throws IllegalAccessException, SQLException, JSONException {
        List<HandleSyncResult> handleSyncResults = handleSync(new JSONArray(msg.getMsgBody()), msg.getSender(), msg.getReciver());
        sendSyncRespond(handleSyncResults);
    }


    //start the data sync
    public static void startDataSync() {
        try {
            Message msg = MessageCreator.createSyncRequest();

            if (msg.getFiles() != null && !msg.getFiles().isEmpty()) {
                byte[] zip = Compress.zip(msg);
                ConnectionManager.getManager().sendFile(zip);
            } else {
                Gson gson = new Gson();
                ConnectionManager.getManager().sendFile(gson.toJson(msg));
            }
        } catch (SQLException e) {

            sendSynEndMsg(new SyncResponse(SyncResponseType.FAIL, e.getMessage()));
            e.printStackTrace();

        } catch (JSONException e) {
            sendSynEndMsg(new SyncResponse(SyncResponseType.FAIL, e.getMessage()));
            e.printStackTrace();
        }
    }

    //send sync end msg
    public static void sendSynEndMsg(SyncResponse response) {
        ConnectionManager connectionManager = ConnectionManager.getManager();
        //generate sync end msg
        Message msg = null;
        try {
            msg = MessageCreator.createSyncEndMsg(response);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //send
        connectionManager.sendFile(msg.toJson());
    }
    //create and send a sync fail msg
    public static void sendSynFailMsg(String msg) {
        ConnectionManager connectionManager = ConnectionManager.getManager();
        SyncResponse response = new SyncResponse(SyncResponseType.FAIL, msg);
        //generate sync end msg
        Message message = null;
        try {
            message = MessageCreator.createSyncEndMsg(response);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //send
        connectionManager.sendFile(message.toJson());
    }
}
