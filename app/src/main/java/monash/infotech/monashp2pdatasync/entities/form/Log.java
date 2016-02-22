package monash.infotech.monashp2pdatasync.entities.form;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

/**
 * Created by john on 12/5/2015.
 * An Entity to store logs
 */
public class Log {
    //auto generate ID
    @DatabaseField(generatedId = true, columnName = "logId")
    private Integer logId;
    //foreign key to the form that this log belongs to
    @DatabaseField(foreign = true)
    private Form form;
    //type of the log
    @DatabaseField
    private LogType logType;
    //the owner of the log
    @DatabaseField
    private String logOwnerID;
    //the time this log created
    @DatabaseField
    private long logTimeStamp;
    //list of all the items
    @ForeignCollectionField(eager = false,foreignFieldName = "log")
    ForeignCollection<LogItems> items;

    public Log(Integer logId) {
        this.logId = logId;
    }
    public Log() {
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public String getLogOwnerID() {
        return logOwnerID;
    }

    public void setLogOwnerID(String logOwnerID) {
        this.logOwnerID = logOwnerID;
    }

    public ForeignCollection<LogItems> getItems() {
        return items;
    }

    public void setItems(ForeignCollection<LogItems> items) {
        this.items = items;
    }

    public long getLogTimeStamp() {
        return logTimeStamp;
    }

    public void setLogTimeStamp(long logTimeStamp) {
        this.logTimeStamp = logTimeStamp;
    }
}
