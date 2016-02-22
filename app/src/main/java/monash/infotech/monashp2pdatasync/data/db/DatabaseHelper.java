package monash.infotech.monashp2pdatasync.data.db;

/**
 * Created by john on 12/4/2015.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.entities.ConflictMethodType;
import monash.infotech.monashp2pdatasync.entities.ConflictRuleType;
import monash.infotech.monashp2pdatasync.entities.MyMsg;
import monash.infotech.monashp2pdatasync.entities.Sequence;
import monash.infotech.monashp2pdatasync.entities.SyncHistory;
import monash.infotech.monashp2pdatasync.entities.context.UserRole;
import monash.infotech.monashp2pdatasync.entities.form.Form;
import monash.infotech.monashp2pdatasync.entities.form.FormItem;
import monash.infotech.monashp2pdatasync.entities.form.FormType;
import monash.infotech.monashp2pdatasync.entities.form.InputType;
import monash.infotech.monashp2pdatasync.entities.form.Item;
import monash.infotech.monashp2pdatasync.entities.form.ItemType;
import monash.infotech.monashp2pdatasync.entities.form.Log;
import monash.infotech.monashp2pdatasync.entities.form.LogItems;
import monash.infotech.monashp2pdatasync.entities.form.Priority;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 *
 *
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    /************************************************
     * Suggested Copy/Paste code. Everything from here to the done block.
     ************************************************/

    private static final String DATABASE_NAME = "p2pdb.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Form, String> formDao;
    private Dao<Log, Integer> logDao;
    private Dao<LogItems, Integer> logItemDao;
    private Dao<FormItem, Integer> formItemDao;
    private Dao<Item, Integer> itemDao;
    private Dao<FormType, Integer> formTypeDao;
    private Dao<MyMsg, Integer> msgDao;
    private Dao<SyncHistory, String> syncHistoryDao;
    private Dao<Sequence, String> sequenceDao;
    private Context context;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
        this.context=context;
    }

    /************************************************
     * Suggested Copy/Paste Done
     ************************************************/

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
        try {

            // Create tables. This onCreate() method will be invoked only once of the application life time i.e. the first time when the application starts.
            TableUtils.createTable(connectionSource, Form.class);
            TableUtils.createTable(connectionSource, Log.class);
            TableUtils.createTable(connectionSource, FormItem.class);
            TableUtils.createTable(connectionSource, Item.class);
            TableUtils.createTable(connectionSource, LogItems.class);
            TableUtils.createTable(connectionSource, FormType.class);
            TableUtils.createTable(connectionSource, MyMsg.class);
            TableUtils.createTable(connectionSource, SyncHistory.class);
            TableUtils.createTable(connectionSource, Sequence.class);
            initData();
        } catch (SQLException e) {
            android.util.Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
        }
    }

    private void initData()
    {
        InputStream is = context.getResources().openRawResource(R.raw.schema);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            android.util.Log.d("Ali", e.getMessage());
        } catch (IOException e) {
            android.util.Log.d("Ali", e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                android.util.Log.d("Ali", e.getMessage());
            }
        }

        String jsonString = writer.toString();
        try {
            JSONArray forms = new JSONObject(jsonString).getJSONArray("forms");
            for(int fCounter=0;fCounter<forms.length();fCounter++) {
                JSONObject jo = forms.getJSONObject(fCounter);

                //init the form type
                JSONObject formMetaData = jo.getJSONObject("form-meta-data");
                FormType formType = new FormType(formMetaData.getString("title"), formMetaData.has("semantic-key") ? formMetaData.getString("semantic-key") : null);
                formTypeDao.create(formType);
                //create the form items
                JSONArray formItems = jo.getJSONArray("items");
                for (int i = 0; i < formItems.length(); i++) {
                    JSONObject temp = formItems.getJSONObject(i);
                    JSONArray items = temp.getJSONArray("items");
                    String category = temp.getString("title");
                    for (int j = 0; j < items.length(); j++) {
                        //create each item
                        JSONObject item = items.getJSONObject(j);
                        Item itemEntity = new Item();
                        itemEntity.setItemTitle(item.getString("key"));
                        itemEntity.setCategoryName(category);
                        itemEntity.setFormTypeId(formType);
                        if (item.has("accessLvl")) {
                            itemEntity.setAccessLvl(UserRole.valueOf(item.getString("accessLvl")));
                        }
                        if (item.has("type")) {
                            itemEntity.setInputType(InputType.valueOf(item.getString("type").toUpperCase()));
                        }
                        if (item.has("priority")) {
                            itemEntity.setPriority(Priority.valueOf(item.getString("priority")));
                        }
                        if (item.has("dataType")) {
                            itemEntity.setType(ItemType.valueOf(item.getString("dataType")));
                        }
                        if (item.has("enums")) {
                            itemEntity.setExtraData("{\"enums\":" + item.getString("enums") + ",\"titleMap\":" + item.getString("titleMap") + "}");
                        }
                        if (item.has("title")) {
                            itemEntity.setTextTitle(item.getString("title"));
                        }
                        if (item.has("conflictRule")) {
                            itemEntity.setConflictRule(ConflictRuleType.valueOf(item.getString("conflictRule")));
                        }
                        if (item.has("conflictMethod")) {
                            itemEntity.setConflictResolveMethod(ConflictMethodType.valueOf(item.getString("conflictMethod")));
                        }
                        itemDao.create(itemEntity);
                    }
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
        try {

            // In case of change in database of next version of application, please increase the value of DATABASE_VERSION variable, then this method will be invoked
            //automatically. Developer needs to handle the upgrade logic here, i.e. create a new table or a new column to an existing table, take the backups of the
            // existing database etc.

            TableUtils.dropTable(connectionSource, Form.class,true);
            TableUtils.dropTable(connectionSource, Log.class,true);
            TableUtils.dropTable(connectionSource, FormItem.class,true);
            TableUtils.dropTable(connectionSource, Item.class,true);
            TableUtils.dropTable(connectionSource, LogItems.class,true);
            TableUtils.dropTable(connectionSource, FormType.class,true);
            TableUtils.dropTable(connectionSource, MyMsg.class,true);
            TableUtils.dropTable(connectionSource, SyncHistory.class, true);
            TableUtils.dropTable(connectionSource, Sequence.class, true);
            onCreate(sqliteDatabase, connectionSource);

        } catch (SQLException e) {
            android.util.Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "
                    + newVer, e);
        }
    }

    // Create the getDao methods of all database tables to access those from android code.
    // Insert, delete, read, update everything will be happened through DAOs

    public Dao<Form, String> getFormDao() throws SQLException {
        if (formDao == null) {
            formDao = getDao(Form.class);
        }
        return formDao;
    }
    public Dao<Log, Integer> getLogDao() throws SQLException {
        if (logDao == null) {
            logDao = getDao(Log.class);
        }
        return logDao;
    }
    public Dao<FormType, Integer> getFormTypeDao() throws SQLException {
        if (formTypeDao == null) {
            formTypeDao = getDao(FormType.class);
        }
        return formTypeDao;
    }
    public Dao<FormItem, Integer> getFormItemDao() throws SQLException {
        if (formItemDao == null) {
            formItemDao = getDao(FormItem.class);
        }
        return formItemDao;
    }
    public Dao<Item, Integer> getItemDao() throws SQLException {
        if (itemDao == null) {
            itemDao = getDao(Item.class);
        }
        return itemDao;
    }
    public Dao<LogItems, Integer> getLogItemDao() throws SQLException {
        if (logItemDao == null) {
            logItemDao = getDao(LogItems.class);
        }
        return logItemDao;
    }
    public Dao<MyMsg, Integer> getMsgDao() throws SQLException {
        if (msgDao == null) {
            msgDao = getDao(MyMsg.class);
        }
        return msgDao;
    }

    public Dao<SyncHistory, String> getSyncHistoryDao() throws SQLException {
        if (syncHistoryDao == null) {
            syncHistoryDao = getDao(SyncHistory.class);
        }
        return syncHistoryDao;
    }

    public Dao<Sequence, String> getSequenceDao() throws SQLException {
        if (sequenceDao == null) {
            sequenceDao = getDao(Sequence.class);
        }
        return sequenceDao;
    }
}