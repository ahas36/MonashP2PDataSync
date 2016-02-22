package monash.infotech.monashp2pdatasync.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.data.db.DatabaseHelper;
import monash.infotech.monashp2pdatasync.entities.MyMsg;

public class MessageFragment extends Fragment {

    public MessageFragment() {
    }

    private DatabaseHelper databaseHelper = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View rootView = inflater.inflate(R.layout.fragment_string_list, container, false);

        try {
            ListView lv = (ListView) rootView.findViewById(R.id.msgList);
            final Dao<MyMsg, Integer> messageDao = getHelper().getMsgDao();
            List<MyMsg> messages = messageDao.queryForAll();
            List<String> msgs=new ArrayList<>();
            for(MyMsg msg:messages)
            {
                msgs.add(msg.getValue());
            }
            ;
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, msgs);

            lv.setAdapter(adapter);

        } catch (SQLException e) {
            android.util.Log.d("Ali", e.getMessage());
        } catch (Exception e) {
            android.util.Log.d("Ali", e.getMessage());
        }
        return rootView;

    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return databaseHelper;
    }


}
