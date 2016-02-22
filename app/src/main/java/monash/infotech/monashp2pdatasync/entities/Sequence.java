package monash.infotech.monashp2pdatasync.entities;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by john on 1/14/2016.
 */
public class Sequence {
    @DatabaseField
    private Integer sequence;
    //item
    @DatabaseField(id = true)
    private String  category;

    public Sequence(String category, Integer sequence) {
        this.category = category;
        this.sequence = sequence;
    }

    public Sequence() {
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
