package monash.infotech.monashp2pdatasync.data.log;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.j256.ormlite.dao.Dao;

import org.json.JSONException;

import java.sql.SQLException;

import monash.infotech.monashp2pdatasync.data.db.DatabaseManager;
import monash.infotech.monashp2pdatasync.entities.form.Form;
import monash.infotech.monashp2pdatasync.entities.form.FormItem;
import monash.infotech.monashp2pdatasync.entities.form.Log;
import monash.infotech.monashp2pdatasync.entities.form.LogItems;
import monash.infotech.monashp2pdatasync.entities.form.LogType;

/**
 * Created by john on 12/6/2015.
 * A class to log all the changes
 */
public class Logger {
    //Device Id of the user who made the changes
    private String ownerId;
    public static int lastLogId=0;
    public Logger(String ownerId) {
        this.ownerId = ownerId;
    }

    //Log the change, Id of the object, old items, new items, type of the log
    public Log log(String objectId, FormItem[] oldObj,  LogType lt) throws IllegalAccessException, JSONException, SQLException {
        //get the DAOs
        Dao<Log, Integer> logDao = DatabaseManager.getLogDao();
        Dao<LogItems, Integer> logItemDao = DatabaseManager.getLogItemDao();
        //get the form
        Form newObj=DatabaseManager.getFormDao().queryForId(objectId);
        //create a new log
        Log log = new Log();
        log.setLogOwnerID(ownerId);
        log.setLogType(lt);
        log.setLogTimeStamp(System.currentTimeMillis());
        log.setForm(newObj);
        logDao.create(log);

        switch (lt) {
            case CREATE:
            case UPDATE:
            case SYNC:
                //loop over all the new items
                for (FormItem newItem : newObj.getItems()) {
                    //if the old object exist
                    if (oldObj != null && oldObj.length > 0) {
                        //find the corresponding item in old object
                        Stream<FormItem> filter = Stream.of(oldObj).filter(v -> v.getItem().getItemId() == newItem.getItem().getItemId());
                        Optional<FormItem> oldItem = filter.findFirst();
                        //if the item exist and the value is not same
                        if (!oldItem.isPresent() || oldItem.get() == null || !oldItem.get().getValue().equals(newItem.getValue())) {
                            //log the item
                            LogItems logItem = new LogItems(newItem.getItem(), log, newItem.getValue());
                            logItemDao.create(logItem);
                        }

                    }
                    //if the old object does not exist
                    else {
                        //log the item
                        LogItems logItem = new LogItems(newItem.getItem(), log, newItem.getValue());
                        logItemDao.create(logItem);
                    }
                }
                //if no changes
                if (log.getItems() != null && log.getItems().size() == 0) {
                    return null;
                }
                break;
        }
        lastLogId=log.getLogId();
        return log;
    }

}
