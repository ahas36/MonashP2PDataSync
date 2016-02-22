package monash.infotech.monashp2pdatasync.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

import monash.infotech.monashp2pdatasync.entities.form.Priority;

/**
 * Created by john on 12/22/2015.
 */
public class SyncHistory {
    @DatabaseField(id = true)
    private String macAddress;
    @DatabaseField
    private long synTime;
    @DatabaseField (dataType = DataType.ENUM_STRING,defaultValue = "HIGH")
    private Priority syncPriorityType;
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Priority getSyncPriorityType() {
        return syncPriorityType;
    }

    public void setSyncPriorityType(Priority syncPriorityType) {
        this.syncPriorityType = syncPriorityType;
    }

    public long getSynTime() {
        return synTime;
    }

    public void setSynTime(long synTime) {
        this.synTime = synTime;
    }

    public SyncHistory(String macAddress,long synTime) {
        this.synTime = synTime;
        this.macAddress = macAddress;
    }
    public SyncHistory()
    {

    }
}
