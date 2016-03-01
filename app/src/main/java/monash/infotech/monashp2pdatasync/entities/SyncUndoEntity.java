package monash.infotech.monashp2pdatasync.entities;

import android.view.View;
import android.widget.CheckBox;

import monash.infotech.monashp2pdatasync.entities.form.Item;

/**
 * Created by ahas36 on 2/03/16.
 */
public class SyncUndoEntity {
    CheckBox selected;
    String oldValue;
    String newValue;
    Item item;
    public SyncUndoEntity(CheckBox selected, String oldValue, String newValue,Item item) {
        this.selected = selected;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.item=item;
    }

    public CheckBox getSelected() {
        return selected;
    }

    public void setSelected(CheckBox selected) {
        this.selected = selected;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
