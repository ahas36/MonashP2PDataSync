package monash.infotech.monashp2pdatasync.entities;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by john on 12/22/2015.
 */
public class MyMsg {
    //auto generate ID
    @DatabaseField(generatedId = true, columnName = "id")
    private Integer id;
    //item
    @DatabaseField
    private String  value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
