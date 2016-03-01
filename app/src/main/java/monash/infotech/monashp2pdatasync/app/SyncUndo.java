package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.json.JSONException;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.data.db.DatabaseManager;
import monash.infotech.monashp2pdatasync.data.log.Logger;
import monash.infotech.monashp2pdatasync.entities.Peer;
import monash.infotech.monashp2pdatasync.entities.SyncUndoEntity;
import monash.infotech.monashp2pdatasync.entities.form.Form;
import monash.infotech.monashp2pdatasync.entities.form.FormItem;
import monash.infotech.monashp2pdatasync.entities.form.Item;
import monash.infotech.monashp2pdatasync.entities.form.Log;
import monash.infotech.monashp2pdatasync.entities.form.LogItems;
import monash.infotech.monashp2pdatasync.entities.form.LogType;

public class SyncUndo extends Activity {
    Form form;
    List<Log> logs;
    Button perv, next, submitBtn;
    TextView logTitle;
    int currentIndex = 0;
    TableLayout logContentLayout;
    Map<Integer, SyncUndoEntity> viewMap = new HashMap<Integer, SyncUndoEntity>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_undo);
        String form_id = getIntent().getExtras().getString("form_id");
        try {
            form = DatabaseManager.getFormDao().queryForId(form_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            QueryBuilder<Log, Integer> logIntegerQueryBuilder = DatabaseManager.getLogDao().queryBuilder();
            logIntegerQueryBuilder.where().eq("form_id", form);
            logIntegerQueryBuilder.orderBy("logid", false);
            logs = logIntegerQueryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        submitBtn = (Button) findViewById(R.id.btnUndo);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undo();
            }
        });
        next = (Button) findViewById(R.id.btnNext);
        perv = (Button) findViewById(R.id.btnBack);
        logTitle = (TextView) findViewById(R.id.txtLogTitle);
        logContentLayout = (TableLayout) findViewById(R.id.logItemLayout);
        if(logs!=null)
        {
            List<Integer> emptyvalues=new ArrayList<>();
            for( int i=0;i<logs.size();i++)
            {
                Log l=logs.get(i);
                if(l.getItems()==null || l.getItems().size()<1)
                    emptyvalues.add(i);
            }
            for(int i:emptyvalues)
            {
                logs.remove(i);
            }
        }
        if(logs==null || logs.size()<2)
        {
            next.setEnabled(false);
            if(logs.isEmpty())
                return;
        }

        try {
            initLog(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    initLog(++currentIndex);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        perv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    initLog(--currentIndex);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void undo() {
        FormItem[] oldFormItems=null;
        Dao<FormItem, Integer> formItemDao = DatabaseManager.getFormItemDao();
        if (form.getItems() != null) {
            Object[] tempArray = form.getItems().toArray();
            oldFormItems = Arrays.copyOf(tempArray, tempArray.length, FormItem[].class);
        }
        for(Map.Entry<Integer,SyncUndoEntity> entry:viewMap.entrySet())
        {
            SyncUndoEntity sue=entry.getValue();
            if(!sue.getSelected().isChecked())
                continue;
            Stream<FormItem> filter = Stream.of(form.getItems()).filter(ie -> ie.getItem().equals(sue.getItem()));
            Optional<FormItem> formItemOptional = filter.findFirst();

            if (formItemOptional.isPresent()) {
                FormItem formItem = formItemOptional.get();
                formItem.setValue(sue.getOldValue());
                try {
                    formItemDao.update(formItem);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        String deviceId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Logger logger=new Logger(deviceId);
        try {
            logger.log(form.getFormId(),oldFormItems,LogType.UPDATE);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initLog(int index) throws SQLException {
        if (logContentLayout.getChildCount() > 0) {
            logContentLayout.removeAllViews();
            viewMap.clear();
        }
        if (index == 0) {
            perv.setEnabled(false);
        } else {
            perv.setEnabled(true);
        }
        if (index == logs.size()-1) {
            next.setEnabled(false);
        } else {
            next.setEnabled(true);
        }
        Log log = logs.get(index);

        Date date = new Date(log.getLogTimeStamp());
        String line_separator=System.getProperty("line.separator");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // Set your date format
        String log_title="Log Date: "+sdf.format(date)+line_separator;
        log_title+="log owner id "+log.getLogOwnerID()+line_separator;
        log_title+="log ID "+log.getLogId()+ " log type: "+log.getLogType();

        logTitle.setText(log_title);

        List<LogItems> items =  DatabaseManager.getLogItemDao().queryForEq("log_id", log);
        TableRow tr_head = new TableRow(this);
        tr_head.setLayoutParams(new TableLayout.LayoutParams(
                0,
                TableLayout.LayoutParams.WRAP_CONTENT));
        TextView label_itemTitle = new TextView(this);
        label_itemTitle.setText("Item Title");
        tr_head.addView(label_itemTitle);
        TextView label_OldValue = new TextView(this);
        label_OldValue.setText("before");
        tr_head.addView(label_OldValue);
        TextView label_currentValue = new TextView(this);
        label_currentValue.setText("after");
        tr_head.addView(label_currentValue);
        TextView label_undo = new TextView(this);
        label_undo.setText("Undo");
        tr_head.addView(label_undo);
        logContentLayout.addView(tr_head);
        for (LogItems logItem : items) {
            Item item=DatabaseManager.getItemDao().queryForId(logItem.getItem().getItemId());
            String rawQuery="select value from logitems join (select max(log_id) as maxVal from logitems join log on log.logId=logitems.log_id where log_id<"+log.getLogId()+" and item_id="+item.getItemId()+" and log.form_id='"+form.getFormId()+"')  maxtable on maxtable.maxVal=logitems.log_id and logitems.item_id="+item.getItemId();
            String[] firstResult = DatabaseManager.getLogItemDao().queryRaw(rawQuery).getFirstResult();
            String formItemValue="";
            if(firstResult!=null && firstResult.length>0)
                formItemValue=firstResult[0];
            TableRow row = new TableRow(this);
            tr_head.setLayoutParams(new TableLayout.LayoutParams(
                    0,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            TextView title = new TextView(this);
            title.setText(item.getTextTitle());
            row.addView(title);
            TextView OldValue = new TextView(this);
            OldValue.setText(formItemValue);
            OldValue.setMaxWidth(100);
            row.addView(OldValue);
            TextView currentValue = new TextView(this);
            currentValue.setText(logItem.getValue());
            currentValue.setMaxWidth(100);
            row.addView(currentValue);
            CheckBox undo = new CheckBox(this);
            undo.setText("Undo");
            row.addView(undo);
            viewMap.put(item.getItemId(),new SyncUndoEntity(undo,formItemValue,logItem.getValue(),item));
            logContentLayout.addView(row);
        }

    }
}
