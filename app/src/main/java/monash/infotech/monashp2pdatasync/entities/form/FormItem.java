package monash.infotech.monashp2pdatasync.entities.form;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by john on 12/9/2015.
 * an entity to store forms items and their values
 */
public class FormItem {
    //auto generate ID
    @DatabaseField(generatedId = true, columnName = "formItemId")
    private Integer formItemId;
    //item
    @DatabaseField(foreign = true,foreignAutoCreate = true)
    private Item item;
    //form
    @DatabaseField(foreign = true)
    private Form form;
    //value
    @DatabaseField
    private String value;

    public FormItem(Item item, Form form, String value) {
        this.item = item;
        this.form = form;
        this.value = value;
    }

    public FormItem(Item item, Form form) {
        this.item = item;
        this.form = form;
    }

    public FormItem() {
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormItem)) return false;

        FormItem formItem = (FormItem) o;

        return !(item != null ? !item.equals(formItem.item) : formItem.item != null);

    }

    @Override
    public int hashCode() {
        return item != null ? item.hashCode() : 0;
    }
}
