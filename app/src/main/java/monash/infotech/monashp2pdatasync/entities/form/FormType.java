package monash.infotech.monashp2pdatasync.entities.form;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

/**
 * Created by john on 12/9/2015.
 * An entity to store different type of form
 */
public class FormType {
    //auto generate ID
    @DatabaseField(generatedId = true, columnName = "formTypeId")
    private Integer formTypeId;
    //name of the form e.g. health assessment
    @DatabaseField(unique = true, canBeNull = false,columnName = "FormTypeTitle")
    private String FormTypeTitle;
    //the semantic key for this type of form (e.g. Property address)
    @DatabaseField
    private String semanticKey;

    //list of all the items
    @ForeignCollectionField(eager = false,foreignFieldName = "FormTypeId")
    ForeignCollection<Item> formTypeItems;

    public FormType(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public FormType(Integer formTypeId, String formTypeTitle) {
        this.formTypeId = formTypeId;
        FormTypeTitle = formTypeTitle;
    }

    public FormType(String formTypeTitle) {
        FormTypeTitle = formTypeTitle;
    }
    public FormType(String formTypeTitle,String semanticKey) {
        FormTypeTitle = formTypeTitle;
        this.semanticKey=semanticKey;
    }
    public FormType() {
    }

    public Integer getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public String getFormTypeTitle() {
        return FormTypeTitle;
    }

    public String getSemanticKey() {
        return semanticKey;
    }

    public void setSemanticKey(String semanticKey) {
        this.semanticKey = semanticKey;
    }

    public ForeignCollection<Item> getFormTypeItems() {
        return formTypeItems;
    }

    public void setFormTypeItems(ForeignCollection<Item> formTypeItems) {
        this.formTypeItems = formTypeItems;
    }

    public void setFormTypeTitle(String formTypeTitle) {
        FormTypeTitle = formTypeTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormType)) return false;

        FormType formType = (FormType) o;

        return !(formTypeId != null ? !formTypeId.equals(formType.formTypeId) : formType.formTypeId != null);

    }

    @Override
    public int hashCode() {
        return formTypeId != null ? formTypeId.hashCode() : 0;
    }
}
