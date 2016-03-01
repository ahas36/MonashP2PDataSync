package monash.infotech.monashp2pdatasync.data.db;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import monash.infotech.monashp2pdatasync.entities.MyMsg;
import monash.infotech.monashp2pdatasync.entities.Sequence;
import monash.infotech.monashp2pdatasync.entities.SyncHistory;
import monash.infotech.monashp2pdatasync.entities.form.Form;
import monash.infotech.monashp2pdatasync.entities.form.FormItem;
import monash.infotech.monashp2pdatasync.entities.form.FormType;
import monash.infotech.monashp2pdatasync.entities.form.Item;
import monash.infotech.monashp2pdatasync.entities.form.Log;
import monash.infotech.monashp2pdatasync.entities.form.LogItems;

/**
 * Created by john on 1/27/2016.
 * create and store all the dao. Whenever its needed, other calsses call this calls to get access to requested dao
 */
public class DatabaseManager {
    //dao's
    private static Dao<Form, String> formDao;
    private static Dao<Log, Integer> logDao;
    private static Dao<LogItems, Integer> logItemDao;
    private static Dao<FormItem, Integer> formItemDao;
    private static Dao<Item, Integer> itemDao;
    private static Dao<FormType, Integer> formTypeDao;
    private static Dao<MyMsg, Integer> msgDao;
    private static Dao<SyncHistory, String> syncHistoryDao;
    private static Dao<Sequence, String> sequenceDao;

    //DB helper

    private static DatabaseHelper helper;

    //init the database manager
    public static void init(Context context) {
        helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            formDao = helper.getFormDao();
            logDao = helper.getLogDao();
            logItemDao = helper.getLogItemDao();
            formItemDao = helper.getFormItemDao();
            itemDao = helper.getItemDao();
            formTypeDao = helper.getFormTypeDao();
            msgDao = helper.getMsgDao();
            syncHistoryDao = helper.getSyncHistoryDao();
            sequenceDao = helper.getSequenceDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //sequence++;
    public static int SequencePlusPlus(String sequenceName) throws SQLException {
        Sequence seq = sequenceDao.queryForId(sequenceName);
        if (seq == null)
        {
            seq = new Sequence();
            seq.setCategory(sequenceName);
            seq.setSequence(1);
            sequenceDao.create(seq);
            return 1;
        }
        int value=seq.getSequence();
        seq.setSequence(value + 1);
        sequenceDao.update(seq);
        return value;
    }


    public static int getSequence(String sequenceName) throws SQLException {
        Sequence seq = sequenceDao.queryForId(sequenceName);
        if (seq == null)
        {
            seq = new Sequence();
            seq.setCategory(sequenceName);
            seq.setSequence(1);
            sequenceDao.create(seq);
            return 1;
        }
        return seq.getSequence();
    }

    public static Dao<Form, String> getFormDao() {
        return formDao;
    }

    public static Dao<Log, Integer> getLogDao() {
        return logDao;
    }

    public static Dao<LogItems, Integer> getLogItemDao() {
        return logItemDao;
    }

    public static Dao<FormItem, Integer> getFormItemDao() {
        return formItemDao;
    }

    public static Dao<Item, Integer> getItemDao() {
        return itemDao;
    }

    public static Dao<FormType, Integer> getFormTypeDao() {
        return formTypeDao;
    }

    public static Dao<MyMsg, Integer> getMsgDao() {
        return msgDao;
    }

    public static Dao<SyncHistory, String> getSyncHistoryDao() {
        return syncHistoryDao;
    }

    public static Dao<Sequence, String> getSequenceDao() {
        return sequenceDao;
    }

    public static DatabaseHelper getHelper() {
        return helper;
    }
    //delete all tables
    public static void deleteAll()
    {
        try {
            logItemDao.deleteBuilder().delete();
            formItemDao.deleteBuilder().delete();
            logDao.deleteBuilder().delete();
            formDao.deleteBuilder().delete();
            msgDao.deleteBuilder().delete();
            syncHistoryDao.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
