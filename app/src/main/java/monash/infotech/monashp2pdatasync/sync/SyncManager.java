package monash.infotech.monashp2pdatasync.sync;

import android.util.Log;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Arrays;

import monash.infotech.monashp2pdatasync.connectivity.p2p.ConnectionManager;
import monash.infotech.monashp2pdatasync.data.db.DatabaseManager;
import monash.infotech.monashp2pdatasync.data.log.Logger;
import monash.infotech.monashp2pdatasync.entities.SyncHistory;
import monash.infotech.monashp2pdatasync.entities.form.ConflictItem;
import monash.infotech.monashp2pdatasync.entities.form.Form;
import monash.infotech.monashp2pdatasync.entities.form.FormItem;
import monash.infotech.monashp2pdatasync.entities.form.FormType;
import monash.infotech.monashp2pdatasync.entities.form.Item;
import monash.infotech.monashp2pdatasync.entities.form.LogType;
import monash.infotech.monashp2pdatasync.messaging.Message;
import monash.infotech.monashp2pdatasync.messaging.MessageCreator;
import monash.infotech.monashp2pdatasync.sync.conflictresolution.ConflictResolution;
import monash.infotech.monashp2pdatasync.sync.conflictresolution.RecentWinConflictResolution;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponse;
import monash.infotech.monashp2pdatasync.sync.entities.SyncResponseType;
import monash.infotech.monashp2pdatasync.utils.Compress;

/**
 * Created by john on 12/8/2015.
 */
public class SyncManager {

    public static void sendSyncRespond() {
        try {
            Message msg = MessageCreator.createSyncResponse();
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

    public static void handelSynEndMsg() throws SQLException {

        DatabaseManager.getSyncHistoryDao().createOrUpdate(new SyncHistory(ConnectionManager.getManager().getConnectedDevice().getMacAddress(), System.currentTimeMillis()));
        ConnectionManager.getManager().disconnect();
    }

    public static void handleSyncResponse(Message msg) throws SQLException, JSONException {
        JSONObject jsonMsg = new JSONObject(msg.getMsgBody());

    }


    public static void handleSyncRequest(Message msg) throws SQLException, JSONException, IllegalAccessException {
        Gson gson = new Gson();
        Dao<Form, String> formDao = DatabaseManager.getFormDao();
        Dao<Item, Integer> itemDao = DatabaseManager.getItemDao();
        Dao<FormItem, Integer> formItemDao = DatabaseManager.getFormItemDao();
        JSONArray json = new JSONArray(msg.getMsgBody());
        ConflictResolution cr = new RecentWinConflictResolution();
        Logger logger = new Logger(msg.getSender().getDeviceName());
        FormItem[] oldFormItems = null;
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
                        formItemDao.create(formItem);
                    }

                }
                logger.log(newForm.getFormId(), oldFormItems, LogType.SYNC_REQUEST);

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
                            value = cr.resolve(new ConflictItem(logItem.getString("value"), logs.getLong("logtimestamp"), item, msg.getSender().getUserContext()), new ConflictItem(formItem.getValue(), Long.valueOf(logTime), item, msg.getReciver().getUserContext()));

                        } else {//if item not exist, create new item and insert
                            formItem = new FormItem();
                            formItem.setItem(itemDao.queryForId(id));
                            formItem.setForm(oldForm);
                            value = logItem.getString("value");
                        }
                        formItem.setValue(value);
                        formItemDao.createOrUpdate(formItem);
                    }

                }
                try {
                    logger.log(oldForm.getFormId(), oldFormItems, LogType.SYNC_REQUEST);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }

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
        Message msg = MessageCreator.createSyncEndMsg(response);
        //send
        connectionManager.sendFile(msg.toJson());
    }
}
