package monash.infotech.monashp2pdatasync.entities.form;

import monash.infotech.monashp2pdatasync.entities.context.UserContext;

/**
 * Created by john on 12/22/2015.
 */
public class ConflictItem {
    public String value;
    public long timeStamp;
    public Item item;
    public UserContext context;
    public boolean isReplace;
    public ConflictItem( String value,long timeStamp,Item item,UserContext context) {
        this.timeStamp = timeStamp;
        this.value = value;
        this.item=item;
        this.context=context;
    }

    public ConflictItem() {
    }
}
