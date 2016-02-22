package monash.infotech.monashp2pdatasync.app;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.List;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.data.db.DatabaseHelper;
import monash.infotech.monashp2pdatasync.data.db.DatabaseManager;
import monash.infotech.monashp2pdatasync.data.log.Logger;
import monash.infotech.monashp2pdatasync.entities.form.Form;
import monash.infotech.monashp2pdatasync.entities.form.FormType;
import monash.infotech.monashp2pdatasync.entities.form.Log;

public class FormListActivity extends Fragment {

    public FormListActivity(){}

    private DatabaseHelper databaseHelper = null;

    @Override
    public View  onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + this.getActivity().getPackageName() + "/databases/p2pdb.db";
                String backupDBPath = "backupname.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
        View rootView = inflater.inflate(R.layout.activity_form_list, container, false);
        final Context context=getActivity().getApplicationContext();
        try {
            ListView lv = (ListView) rootView.findViewById(R.id.formsList);
            final Dao<Form, String> formDao = getHelper().getFormDao();
            List<Form> forms = formDao.queryForAll();

            final FormArrayAdapter faa = new FormArrayAdapter(getActivity(), forms);

            lv.setAdapter(faa);
            ((Button) rootView.findViewById(R.id.btnAddForm)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), DataEntryActivity.class);
                    //based on item add info to intent
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Please select the type of the form you want to add");
                    List<FormType> formTypes=null;

                    try {
                        formTypes = DatabaseManager.getFormTypeDao().queryForAll();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    final int typeListLength=formTypes.size();
                    CharSequence[] typeTitles=new CharSequence[typeListLength];
                    Integer[] typeIds=new Integer[typeListLength];

                    int counter=0;
                    for(FormType ft:formTypes)
                    {
                        typeTitles[counter]=ft.getFormTypeTitle();
                        typeIds[counter++]=ft.getFormTypeId();
                    }

                    builder.setItems(typeTitles,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    //based on item add info to intent
                                    intent.putExtra("formType", typeIds[which]);
                                    getActivity().startActivity(intent);
                                }
                            });
                    builder.create().show();
                }
            });
        } catch (SQLException e) {
            android.util.Log.d("Ali", e.getMessage());
        }
        catch (Exception e)
        {
            android.util.Log.d("Ali", e.getMessage());
        }
        return rootView;

    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper( getActivity(), DatabaseHelper.class);
        }
        return databaseHelper;
    }


}
