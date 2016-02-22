package monash.infotech.monashp2pdatasync.entities;

/**
 * Created by john on 2/12/2016.
 */
public class KeyVal implements Comparable<KeyVal> {
    private String key;
    private String value;

    public KeyVal(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(KeyVal another) {
        return this.key.compareTo(another.getKey());
    }
}
