package monash.infotech.monashp2pdatasync.sync.conflictresolution;

import monash.infotech.monashp2pdatasync.entities.form.ConflictItem;

/**
 * Created by john on 12/22/2015.
 */
public interface ConflictResolution {
    public String resolve(ConflictItem item1,ConflictItem item2);
}
