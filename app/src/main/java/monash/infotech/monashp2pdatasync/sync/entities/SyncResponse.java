package monash.infotech.monashp2pdatasync.sync.entities;

/**
 * Created by john on 1/27/2016.
 */
public class SyncResponse {
    private SyncResponseType type;
    private String msg;
    private int lastLogId;
    public SyncResponse(SyncResponseType type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public SyncResponse() {
    }

    public int getLastLogId() {
        return lastLogId;
    }

    public void setLastLogId(int lastLogId) {
        this.lastLogId = lastLogId;
    }

    public SyncResponseType getType() {
        return type;
    }

    public void setType(SyncResponseType type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
