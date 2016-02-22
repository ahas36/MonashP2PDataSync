package monash.infotech.monashp2pdatasync.entities.form;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import monash.infotech.monashp2pdatasync.entities.ConflictMethodType;
import monash.infotech.monashp2pdatasync.entities.ConflictRuleType;
import monash.infotech.monashp2pdatasync.entities.context.UserRole;

/**
 * Created by john on 12/9/2015.
 * An entity to store the title of different items(records) for each form
 */
@DatabaseTable(tableName = "items")
public class Item {
    //an auto generate id
    @DatabaseField(generatedId = true, columnName = "ItemId")
    private Integer ItemId;
    //type of the form this item belongs to
    @DatabaseField(canBeNull = false, foreign = true,foreignAutoCreate = true,uniqueIndexName = "unique_type_title")
    private FormType FormTypeId;
    //title of item, this value should be unique for each form type
    @DatabaseField(canBeNull = false,uniqueIndexName = "unique_type_title")
    private String ItemTitle;

    //item context
    @DatabaseField(dataType = DataType.ENUM_INTEGER,defaultValue = "0")
    private UserRole accessLvl;

    @DatabaseField(dataType = DataType.ENUM_STRING,defaultValue = "NORMAL")
    private Priority priority;
    @DatabaseField(dataType = DataType.ENUM_STRING,defaultValue = "TEXT")
    private ItemType type;

    @DatabaseField(dataType = DataType.ENUM_STRING,defaultValue = "TEXTAREA")
    private InputType inputType;
    @DatabaseField
    private String categoryName;

    @DatabaseField
    private String extraData;

    @DatabaseField
    private String textTitle;

    @DatabaseField(dataType = DataType.ENUM_STRING,defaultValue = "REPLACE")
    private ConflictMethodType conflictResolveMethod;

    @DatabaseField(dataType = DataType.ENUM_STRING,defaultValue = "RECENT_WIN")
    private ConflictRuleType conflictRule;

    public Item(Integer itemId) {
        ItemId = itemId;
    }

    public Item(Integer itemId, FormType formTypeId, String itemTitle) {
        ItemId = itemId;
        FormTypeId = formTypeId;
        ItemTitle = itemTitle;
    }

    public Item(FormType formTypeId, String itemTitle) {
        FormTypeId = formTypeId;
        ItemTitle = itemTitle;
    }

    public Item() {
    }

    public String getTextTitle() {
        return textTitle;
    }

    public void setTextTitle(String textTitle) {
        this.textTitle = textTitle;
    }

    public Integer getItemId() {
        return ItemId;
    }

    public void setItemId(Integer itemId) {
        ItemId = itemId;
    }

    public FormType getFormTypeId() {
        return FormTypeId;
    }

    public void setFormTypeId(FormType formTypeId) {
        FormTypeId = formTypeId;
    }

    public String getItemTitle() {
        return ItemTitle;
    }

    public void setItemTitle(String itemTitle) {
        ItemTitle = itemTitle;
    }

    public UserRole getAccessLvl() {
        return accessLvl;
    }

    public void setAccessLvl(UserRole accessLvl) {
        this.accessLvl = accessLvl;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public String getCategoryName() {
        return categoryName;
    }


    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public ConflictMethodType getConflictResolveMethod() {
        return conflictResolveMethod;
    }

    public void setConflictResolveMethod(ConflictMethodType conflictResolveMethod) {
        this.conflictResolveMethod = conflictResolveMethod;
    }

    public ConflictRuleType getConflictRule() {
        return conflictRule;
    }

    public void setConflictRule(ConflictRuleType conflictRule) {
        this.conflictRule = conflictRule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;

        Item items = (Item) o;

        return !(ItemId != null ? !ItemId.equals(items.ItemId) : items.ItemId != null);

    }

    @Override
    public int hashCode() {
        return ItemId != null ? ItemId.hashCode() : 0;
    }
}
