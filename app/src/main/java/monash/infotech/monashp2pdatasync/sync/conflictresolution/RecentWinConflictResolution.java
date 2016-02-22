package monash.infotech.monashp2pdatasync.sync.conflictresolution;

import monash.infotech.monashp2pdatasync.entities.form.ConflictItem;

/**
 * Created by john on 12/22/2015.
 */
public class RecentWinConflictResolution implements ConflictResolution {

    @Override
    public String resolve(ConflictItem item1, ConflictItem item2) {
        switch (item1.item.getConflictResolveMethod()) {
            case ADD:
                return ConflictResolverMethods.add(Double.valueOf(item1.value), Double.valueOf(item2.value));
            case MERGE_POINTS:
                break;
            case REPLACE:
                switch (item1.item.getConflictRule()) {
                    case AUTHORITY_WIN:
                        if(item1.context.getRole().ordinal()>item2.context.getRole().ordinal())
                            return item1.value;
                        else
                            return item2.value;
                    case EXPERT_WIN:
                        double item1Experties=0;
                        double item2Experties=0;
                        try {
                            item1Experties=item1.context.getExpertise().get(item1.item.getCategoryName()).doubleValue();
                        }
                        catch (Exception e)
                        {

                        }
                        try {
                            item2Experties=item2.context.getExpertise().get(item2.item.getCategoryName()).doubleValue();
                        }
                        catch (Exception e)
                        {

                        }
                        if(item1Experties>item2Experties)
                            return item1.value;
                        else
                            return item2.value;
                    case RECENT_WIN:
                        if(item1.timeStamp>item2.timeStamp)
                            return item1.value;
                        else
                            return item2.value;
                }
        }
        return item1.value;
    }

}
