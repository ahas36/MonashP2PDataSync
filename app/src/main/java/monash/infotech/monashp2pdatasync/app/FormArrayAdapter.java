package monash.infotech.monashp2pdatasync.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.List;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.data.db.DatabaseHelper;
import monash.infotech.monashp2pdatasync.data.log.Logger;
import monash.infotech.monashp2pdatasync.entities.form.Form;

/**
 * Created by john on 12/4/2015.
 */
public class FormArrayAdapter extends ArrayAdapter<Form> {
    private final Context context;
    private final List<Form> values;

    public FormArrayAdapter(Context context, List<Form> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.form_list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.formidtextview);
        final Form form = values.get(position);
        textView.setText(String.valueOf(form.getFormId()));
        final Button button = (Button) rowView.findViewById(R.id.btnFormDelete);
        // change the icon for Windows and iPhone
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
                                    String android_id = Settings.Secure.getString(context.getContentResolver(),
                                            Settings.Secure.ANDROID_ID);
                                    Logger logger=new Logger(android_id);
                                //    Log le=logger.log(form.getFormId(), form.getEventId(), null, form, LogType.DELETE);
                                    databaseHelper.getFormDao().delete(form);
                               //     databaseHelper.getLogDao().create(le);
                                    values.remove(form);
                                    notifyDataSetChanged();
                                } catch (SQLException e) {
                                    android.util.Log.d("Ali", e.getMessage());
                                }/* catch (JSONException e) {
                                    android.util.Log.d("Ali", e.getMessage());
                                } catch (IllegalAccessException e) {
                                    android.util.Log.d("Ali", e.getMessage());
                                }*/
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        final Button btnView = (Button) rowView.findViewById(R.id.btnFormView);
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, DataEntryActivity.class);
                //based on item add info to intent
                intent.putExtra("formId", form.getFormId());
                context.startActivity(intent);
            }
        });
        return rowView;
    }

    public Form getItem(int position) {

        return values.get(position);
    }

}

