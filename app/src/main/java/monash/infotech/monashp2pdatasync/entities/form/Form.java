package monash.infotech.monashp2pdatasync.entities.form;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

/**
 * Created by john on 12/9/2015.
 * An entity to store forms
 */
public class Form implements Cloneable{
    //auto generate ID
    @DatabaseField(id=true,columnName = "formId")
    private String formId;
    //type of the form
    @DatabaseField(canBeNull = false, foreign = true,foreignAutoCreate = true)
    private FormType formType;

    //the timestamp of last modification
    private long lastModified;
    //list of all the items
    @ForeignCollectionField(eager = false,foreignFieldName = "form")
    ForeignCollection<FormItem> items;

    public Form() {

    }

    public Form(String formId) {
        this.formId = formId;
    }

    public Form(String formId, FormType formType, long lastModified) {
        this.formId = formId;
        this.formType = formType;
        this.lastModified = lastModified;
    }

    public Form(FormType formType,  long lastModified) {
        this.formType = formType;
        this.lastModified = lastModified;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public FormType getFormType() {
        return formType;
    }

    public void setFormType(FormType formType) {
        this.formType = formType;
    }



    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public ForeignCollection<FormItem> getItems() {
        return items;
    }

    public void setItems(ForeignCollection<FormItem> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Form)) return false;

        Form form = (Form) o;

        return !(formId != null ? !formId.equals(form.formId) : form.formId != null);

    }

    @Override
    public int hashCode() {
        return formId != null ? formId.hashCode() : 0;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
