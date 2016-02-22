package monash.infotech.monashp2pdatasync.entities.form;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

/**
 * Created by john on 12/9/2015.
 * entity for storing log's items
 */
public class LogItems {
    //auto generate ID
    @Expose
    @DatabaseField(generatedId = true, columnName = "logItemId")
    private Integer logItemId;
    //item
    @Expose
    @DatabaseField(foreign = true,foreignAutoCreate = true)
    private Item item;
    //log
    @DatabaseField(foreign = true,foreignAutoCreate = true)
    private Log log;
    //value
    @Expose
    @DatabaseField
    private String value;

    public LogItems(Item item, Log log, String value) {
        this.item = item;
        this.log = log;
        this.value = value;
    }

    public LogItems(Item item) {
        this.item = item;
    }

    public LogItems() {
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
