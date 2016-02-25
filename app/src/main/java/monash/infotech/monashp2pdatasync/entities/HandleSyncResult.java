package monash.infotech.monashp2pdatasync.entities;

import monash.infotech.monashp2pdatasync.entities.form.HandleSyncResultType;

/**
 * Created by ahas36 on 23/02/16.
 */
public class HandleSyncResult {
    //result of conflict resolution
    private HandleSyncResultType type;
    //the form id of sender
    private String senderFormID;
    //item id
    private int itemId;
    //form id
    private String value;
    //form id of the local device (as same forms can have different ids)
    private String localFormID;
    public HandleSyncResult(HandleSyncResultType type, String senderFormID, int itemId, String value,String localFormID) {
        this.type = type;
        this.senderFormID = senderFormID;
        this.itemId = itemId;
        this.value = value;
        this.localFormID=localFormID;
    }

    public HandleSyncResultType getType() {
        return type;
    }

    public void setType(HandleSyncResultType type) {
        this.type = type;
    }

    public String getSenderFormID() {
        return senderFormID;
    }

    public void setSenderFormID(String senderFormID) {
        this.senderFormID = senderFormID;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLocalFormID() {
        return localFormID;
    }

    public void setLocalFormID(String localFormID) {
        this.localFormID = localFormID;
    }
}
