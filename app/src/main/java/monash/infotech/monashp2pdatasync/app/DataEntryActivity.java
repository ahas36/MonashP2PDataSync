package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.android.gms.maps.MapFragment;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.data.db.DatabaseHelper;
import monash.infotech.monashp2pdatasync.data.db.DatabaseManager;
import monash.infotech.monashp2pdatasync.data.log.Logger;
import monash.infotech.monashp2pdatasync.entities.KeyVal;
import monash.infotech.monashp2pdatasync.entities.Sequence;
import monash.infotech.monashp2pdatasync.entities.form.Form;
import monash.infotech.monashp2pdatasync.entities.form.FormItem;
import monash.infotech.monashp2pdatasync.entities.form.FormType;
import monash.infotech.monashp2pdatasync.entities.form.InputType;
import monash.infotech.monashp2pdatasync.entities.form.Item;
import monash.infotech.monashp2pdatasync.entities.form.LogType;

public class DataEntryActivity extends Activity {

    Map<String, View> viewMap = new HashMap<String, View>();
    Map<Integer, ImageCapture> imageCaptureMap = new HashMap<Integer, ImageCapture>();
    Map<Integer, VideoCapture> videoCaptureMap = new HashMap<Integer, VideoCapture>();
    Map<Integer, AudioRecorder> audioCaptureMap = new HashMap<Integer, AudioRecorder>();
    Form form;
    int formTypeID;
    FormItem[] oldFormItems;
    private DatabaseHelper databaseHelper = null;
    private boolean isCreate = false;
    P2PApplicationContext context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (P2PApplicationContext) getApplicationContext();
        setContentView(R.layout.activity_data_entry);
        form = null;
        try {
            final Dao<Form, String> formDao = DatabaseManager.getFormDao();
            String id = "";
            if (getIntent().hasExtra("formId"))
                id = getIntent().getStringExtra("formId");

            if (!id.isEmpty()) {
                form = formDao.queryForId(id);
                formTypeID = form.getFormType().getFormTypeId();
                if (form.getItems() != null) {
                    Object[] tempArray = form.getItems().toArray();
                    oldFormItems = Arrays.copyOf(tempArray, tempArray.length, FormItem[].class);
                }
            } else {
                formTypeID = getIntent().getIntExtra("formType", 1);
            }

        } catch (Exception e) {

            android.util.Log.d("Ali", e.getMessage());
        }
        try {
            createUi();
        } catch (SQLException e) {
            android.util.Log.d("Ali", e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    // This is how, DatabaseHelper can be initialized for future use
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    private void createUi() throws SQLException, JSONException {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.form);
        PreparedQuery<Item> itemsQuery = DatabaseManager.getItemDao().queryBuilder().where().eq("FormTypeId_id", formTypeID).and().le("accessLvl", context.gettUserContext().getRole()).prepare();
        List<Item> items = DatabaseManager.getItemDao().query(itemsQuery);
        String category = "";
        LinearLayout ly = null;
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Dao<FormItem, Integer> formItemDao = DatabaseManager.getFormItemDao();
        for (Item item : items) {
            String itemValue = "";
            if (form != null) {
                PreparedQuery<FormItem> formItemQuery = formItemDao.queryBuilder().where().eq("item_id", item.getItemId()).and().eq("form_id", form.getFormId()).prepare();
                FormItem formItem = formItemDao.queryForFirst(formItemQuery);
                if (formItem != null)
                    itemValue = formItem.getValue();
            }
            if (!category.equals(item.getCategoryName())) {
                category = item.getCategoryName();
                LinearLayout temp = new LinearLayout(this);
                temp.setLayoutParams(lparams);
                temp.setOrientation(LinearLayout.VERTICAL);
                temp.setBackgroundResource(R.drawable.border);
                linearLayout.addView(temp);
                TextView tv = new TextView(this);
                if (Build.VERSION.SDK_INT < 23) {

                    tv.setTextAppearance(this, android.R.style.TextAppearance_Large);

                } else {

                    tv.setTextAppearance(android.R.style.TextAppearance_Large);
                }
                tv.setLayoutParams(lparams);
                tv.setText(category);
                temp.addView(tv);
                ly = temp;
            }
            switch (item.getInputType()) {
                case TAGTEXT: {
                    if (item.getTextTitle() != null && !item.getTextTitle().isEmpty()) {
                        String taTitle = item.getTextTitle();
                        TextView taTV = new TextView(this);
                        taTV.setLayoutParams(lparams);
                        taTV.setText(taTitle);
                        ly.addView(taTV);
                    }
                    EditText txtArea = new EditText(this);
                    txtArea.setBackgroundResource(R.drawable.textbox_border);
                    txtArea.setLayoutParams(lparams);
                    txtArea.setMinLines(1);
                    txtArea.setMaxLines(1);
                    txtArea.setLines(1);
                    txtArea.setGravity(Gravity.TOP | Gravity.LEFT);
                    txtArea.setText(itemValue);

                    viewMap.put(item.getItemTitle(), txtArea);
                    ly.addView(txtArea);
                }
                break;
                case SELECT: {
                    if (item.getTextTitle() != null && !item.getTextTitle().isEmpty()) {
                        String taTitle = item.getTextTitle();
                        TextView taTV = new TextView(this);
                        taTV.setLayoutParams(lparams);
                        taTV.setText(taTitle);
                        ly.addView(taTV);
                    }

                    Spinner spinner = new Spinner(this);
                    spinner.setLayoutParams(lparams);
                    JSONObject extraInfo = new JSONObject(item.getExtraData());
                    JSONArray radioList = extraInfo.getJSONArray("enums");
                    JSONObject titleMap = extraInfo.getJSONObject("titleMap");
                    List<String> values = new ArrayList<>();
                    int index = 0;
                    for (int q = 0; q < radioList.length(); q++) {
                        values.add(titleMap.getString(radioList.getString(q)));
                        if (titleMap.getString(radioList.getString(q)).equals(itemValue)) {
                            index = q;
                        }

                    }
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, values);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(dataAdapter);
                    viewMap.put(item.getItemTitle(), spinner);
                    spinner.setSelection(index);
                    ly.addView(spinner);
                }
                break;
                case TEXTAREA:
                    if (item.getTextTitle() != null && !item.getTextTitle().isEmpty()) {
                        String taTitle = item.getTextTitle();
                        TextView taTV = new TextView(this);
                        taTV.setLayoutParams(lparams);
                        taTV.setText(taTitle);
                        ly.addView(taTV);
                    }
                    EditText txtArea = new EditText(this);
                    txtArea.setBackgroundResource(R.drawable.textbox_border);
                    txtArea.setLayoutParams(lparams);
                    txtArea.setMinLines(3);
                    txtArea.setMaxLines(5);
                    txtArea.setLines(4);
                    txtArea.setVerticalScrollBarEnabled(true);
                    txtArea.setGravity(Gravity.TOP | Gravity.LEFT);
                    ly.addView(txtArea);
                    viewMap.put(item.getItemTitle(), txtArea);
                    txtArea.setText(itemValue);
                    break;
                case RADIO:
                    String radioTitle = item.getTextTitle();
                    RadioGroup rgp = new RadioGroup(this);
                    TextView radioTV = new TextView(this);
                    radioTV.setLayoutParams(lparams);
                    radioTV.setText(radioTitle);
                    rgp.setLayoutParams(lparams);
                    rgp.setOrientation(LinearLayout.HORIZONTAL);
                    ly.addView(radioTV);
                    ly.addView(rgp);
                    JSONObject jsonObject = new JSONObject(item.getExtraData());
                    JSONArray radioList = jsonObject.getJSONArray("enums");
                    if (radioList.length() > 4) {
                        rgp.setOrientation(LinearLayout.VERTICAL);
                    }
                    JSONObject titleMap = jsonObject.getJSONObject("titleMap");
                    int checkedId = -1;
                    for (int q = 0; q < radioList.length(); q++) {

                        RadioButton radioButton = new RadioButton(this);
                        radioButton.setText(titleMap.getString(radioList.getString(q)));


                        rgp.addView(radioButton);
                        if (titleMap.getString(radioList.getString(q)).equals(itemValue)) {
                            checkedId = radioButton.getId();
                        }
                    }
                    rgp.check(checkedId);
                    viewMap.put(item.getItemTitle(), rgp);

                    break;
                case ASSET: {
                    RelativeLayout flWrapper = new RelativeLayout(this);
                    flWrapper.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600));
                    ly.addView(flWrapper);
                    FrameLayout fl = new FrameLayout(this);
                    fl.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
                    fl.setId(id);
                    flWrapper.addView(fl);

                    ImageView transparentImageView = new ImageView(this);
                    transparentImageView.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    transparentImageView.setBackgroundColor(Color.TRANSPARENT);
                    final ScrollView mainScrollView = (ScrollView) findViewById(R.id.scrollView);
                    transparentImageView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            int action = event.getAction();
                            switch (action) {
                                case MotionEvent.ACTION_DOWN:
                                    // Disallow ScrollView to intercept touch events.
                                    mainScrollView.requestDisallowInterceptTouchEvent(true);
                                    // Disable touch on transparent view
                                    return false;

                                case MotionEvent.ACTION_UP:
                                    // Allow ScrollView to intercept touch events.
                                    mainScrollView.requestDisallowInterceptTouchEvent(true);
                                    return true;

                                case MotionEvent.ACTION_MOVE:
                                    mainScrollView.requestDisallowInterceptTouchEvent(true);
                                    return false;

                                default:
                                    return true;
                            }
                        }
                    });

                    flWrapper.addView(transparentImageView);
                    MapFragment mapFragment = MapFragment.newInstance();
                    FragmentManager fm = this.getFragmentManager();
                    fm.beginTransaction().replace(id, mapFragment).commit();
                }
                break;
                case SOUND: {
                    String fileName = "";
                    if (form == null) {
                        fileName = "?" + item.getItemId();
                    } else {
                        fileName = form.getFormId() + item.getItemId();
                    }

                    if (!itemValue.isEmpty()) {
                        String version = itemValue.substring(itemValue.indexOf("version_") + ("version_").length(), itemValue.indexOf(".3gpp"));
                        fileName += "version_" + (Integer.valueOf(version) + 1);
                    } else {
                        fileName += "version_1";
                    }
                    AudioRecorder audioRecorder = new AudioRecorder(this, ly, fileName, itemValue);
                    audioCaptureMap.put(item.getItemId(), audioRecorder);
                    break;
                }
                case IMAGE: {
                    String fileName = "";
                    if (form == null) {
                        fileName = "?" + item.getItemId();
                    } else {
                        fileName = form.getFormId() + item.getItemId();
                    }
                    if (!itemValue.isEmpty()) {
                        String version = itemValue.substring(itemValue.indexOf("version_") + ("version_").length(), itemValue.indexOf(".jpg"));

                        fileName += "version_" + (Integer.valueOf(version) + 1);
                    } else {
                        fileName += "version_1";
                    }
                    ImageCapture imageCapture = new ImageCapture(this, ly, fileName, item.getItemId(), itemValue);
                    imageCaptureMap.put(item.getItemId(), imageCapture);
                    break;
                }
                case VIDEO: {

                    String fileName = "";
                    if (form == null) {
                        fileName = "?" + item.getItemId();
                    } else {
                        fileName = form.getFormId() + item.getItemId();
                    }
                    if (!itemValue.isEmpty()) {
                        String version = itemValue.substring(itemValue.indexOf("version_") + ("version_").length(), itemValue.indexOf(".mp4"));
                        fileName += "version_" + (Integer.valueOf(version) + 1);
                    } else {
                        fileName += "version_1";
                    }
                    VideoCapture videoCapture = new VideoCapture(this, ly, fileName, item.getItemId(), itemValue);
                    videoCaptureMap.put(item.getItemId(), videoCapture);
                    break;
                }
            }
        }
        Button submit = new Button(this);
        submit.setText("submit");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSemanticKeyUnique()) {
                    Toast.makeText(DataEntryActivity.this, "the semantic key should be unique", Toast.LENGTH_LONG).show();
                    return;
                }
                if (form == null) {
                    try {
                        final Dao<FormType, Integer> formTypeDao = DatabaseManager.getFormTypeDao();
                        final Dao<Sequence, String> sequenceDao = DatabaseManager.getSequenceDao();
                        FormType formType = formTypeDao.queryForId(formTypeID);
                        Sequence seq = sequenceDao.queryForId("form");
                        if (seq == null) {
                            seq = new Sequence();
                            seq.setCategory("form");
                            seq.setSequence(1);
                            sequenceDao.create(seq);
                        }
                        String android_id = Settings.Secure.getString(getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        form = new Form();
                        form.setFormId(android_id + seq.getSequence());
                        seq.setSequence(seq.getSequence() + 1);
                        form.setFormType(formType);
                        form.setLastModified(0);
                        sequenceDao.update(seq);
                        DatabaseManager.getFormDao().create(form);
                        isCreate = true;
                    } catch (Exception e) {

                    }
                }
                for (Map.Entry<Integer, AudioRecorder> entry : audioCaptureMap.entrySet()) {
                    if (entry.getValue().isNew()) {
                        try {
                            if (isCreate) {
                                entry.getValue().setRecorded(form.getFormId() + "version_1");
                            }
                            entry.getValue().save();
                            Item item = DatabaseManager.getItemDao().queryForId(entry.getKey());
                            FormItem formItem = findFormItem(item);
                            formItem.setValue(entry.getValue().getRecorded());
                            DatabaseManager.getFormItemDao().createOrUpdate(formItem);
                        } catch (SQLException e) {
                            entry.getValue().clear();
                            e.printStackTrace();
                        }
                    }
                }

                for (Map.Entry<Integer, VideoCapture> entry : videoCaptureMap.entrySet()) {
                    if (entry.getValue().isNew()) {
                        try {
                            if (isCreate) {
                                entry.getValue().setVideoPath(form.getFormId() + "version_1");
                            }
                            entry.getValue().saveVideo();
                            Item item = DatabaseManager.getItemDao().queryForId(entry.getKey());
                            FormItem formItem = findFormItem(item);
                            formItem.setValue(entry.getValue().getVideoPath());
                            DatabaseManager.getFormItemDao().createOrUpdate(formItem);
                        } catch (SQLException e) {
                            entry.getValue().clear();
                            e.printStackTrace();
                        }
                    }
                }

                for (Map.Entry<Integer, ImageCapture> entry : imageCaptureMap.entrySet()) {
                    if (entry.getValue().isNew()) {
                        try {
                            if (isCreate) {
                                entry.getValue().setImagePath(form.getFormId() + "version_1");
                            }
                            entry.getValue().saveImage();
                            Item item = DatabaseManager.getItemDao().queryForId(entry.getKey());
                            FormItem formItem = findFormItem(item);
                            formItem.setValue(entry.getValue().getImagePath());
                            DatabaseManager.getFormItemDao().createOrUpdate(formItem);
                        } catch (SQLException e) {
                            entry.getValue().clear();
                            e.printStackTrace();
                        }
                    }
                }

                for (Map.Entry<String, View> entry : viewMap.entrySet()) {
                    Item tempItem = null;
                    String value = "";
                    try {
                        PreparedQuery<Item> itemTitleQuery = DatabaseManager.getItemDao().queryBuilder().where().eq("ItemTitle", entry.getKey()).prepare();
                        tempItem = DatabaseManager.getItemDao().queryForFirst(itemTitleQuery);

                    } catch (SQLException e) {
                        android.util.Log.d("Ali", e.getMessage());
                    }
                    final Item itemEntity = tempItem;
                    value = getMapValue(itemEntity.getItemTitle());

                    if (form.getItems() != null) {
                        Stream<FormItem> filter = Stream.of(form.getItems()).filter(ie -> ie.getItem().equals(itemEntity));
                        Optional<FormItem> formItemOptional = filter.findFirst();

                        if (formItemOptional.isPresent()) {
                            FormItem formItem = formItemOptional.get();
                            formItem.setValue(value);
                            try {
                                formItemDao.update(formItem);
                            } catch (SQLException e) {
                                android.util.Log.d("Ali", e.getMessage());
                            }

                        } else {
                            if (value.isEmpty())
                                continue;
                            FormItem formItem = new FormItem();
                            formItem = new FormItem();
                            formItem.setItem(itemEntity);
                            formItem.setForm(form);
                            formItem.setValue(value);
                            try {
                                formItemDao.create(formItem);
                            } catch (SQLException e) {
                                android.util.Log.d("Ali", e.getMessage());
                            }
                        }
                    } else {
                        if (value.isEmpty())
                            continue;
                        FormItem formItem = new FormItem();
                        formItem.setItem(itemEntity);
                        formItem.setForm(form);
                        formItem.setValue(value);
                        try {
                            formItemDao.create(formItem);
                        } catch (SQLException e) {
                            android.util.Log.d("Ali", e.getMessage());
                        }
                    }

                }
                try {

                    String deviceId = Settings.Secure.getString(getContentResolver(),
                            Settings.Secure.ANDROID_ID);

                    Logger logger = new Logger(deviceId);
                    DatabaseManager.getFormDao().update(form);
                    form = DatabaseManager.getFormDao().queryForId(form.getFormId());
                    if (isCreate) {
                        logger.log(form.getFormId(), oldFormItems,  LogType.CREATE);

                    } else {
                        logger.log(form.getFormId(), oldFormItems,  LogType.UPDATE);

                    }

                    //    getHelper().getLogDao().create(log);
                    Intent intent = new Intent(DataEntryActivity.this, MainFragmentActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(DataEntryActivity.this, "saved", Toast.LENGTH_LONG);
                } catch (SQLException e) {
                    Toast.makeText(DataEntryActivity.this, "error", Toast.LENGTH_LONG);
                    android.util.Log.d("Ali", e.getMessage());
                    //     } catch (JSONException e) {
                    //         android.util.Log.d("Ali", e.getMessage());
                    //     } catch (IllegalAccessException e) {
                    //         android.util.Log.d("Ali", e.getMessage());
                } catch (JSONException e) {
                    android.util.Log.d("Ali", e.getMessage());
                } catch (IllegalAccessException e) {
                    android.util.Log.d("Ali", e.getMessage());
                }
            }
        });
        linearLayout.addView(submit);

    }

    public FormItem findFormItem(Item itemEntity) {

        if (form.getItems() != null) {
            Stream<FormItem> filter = Stream.of(form.getItems()).filter(ie -> ie.getItem().equals(itemEntity));
            Optional<FormItem> formItemOptional = filter.findFirst();
            if (formItemOptional.isPresent()) {
                FormItem formItem = formItemOptional.get();
                return formItem;
            }
        }
        FormItem formItem = new FormItem();
        formItem = new FormItem();
        formItem.setItem(itemEntity);
        formItem.setForm(form);
        return formItem;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

		/*
         * You'll need this in your class to release the helper when done.
		 */
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        InputType inputType = null;
        try {
            inputType = DatabaseManager.getItemDao().queryForId(requestCode).getInputType();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (inputType != null && inputType == InputType.IMAGE && resultCode == RESULT_OK) {
            imageCaptureMap.get(requestCode).loadImage();
        }
        if (inputType != null && inputType == InputType.VIDEO && resultCode == RESULT_OK) {
            videoCaptureMap.get(requestCode).loadVideo();
        }
    }

    private boolean isSemanticKeyUnique() {
        try {
            FormType formType = DatabaseManager.getFormTypeDao().queryForId(formTypeID);
            JSONArray semanticKeyJsonArray = new JSONArray(formType.getSemanticKey());
            List<KeyVal> semanticItemList = new ArrayList<>();
            if (semanticKeyJsonArray.length() > 0) {
                for (int semanticCounter = 0; semanticCounter < semanticKeyJsonArray.length(); semanticCounter++) {
                    String fieldName = semanticKeyJsonArray.getString(semanticCounter);
                    View view = viewMap.get(fieldName);
                    String semanticValue = getMapValue(fieldName);
                    String id = DatabaseManager.getItemDao().queryBuilder().where().eq("itemTitle", fieldName).queryForFirst().getItemId().toString();
                    semanticItemList.add(new KeyVal(id, semanticValue));
                }
                Collections.sort(semanticItemList);
                String ids = "";
                String vals = "";
                for (KeyVal item : semanticItemList) {
                    ids += item.getKey() + ",";
                    vals += item.getValue() + ",";
                }
                ids = ids.substring(0, ids.length() - 1);
                vals = vals.substring(0, vals.length() - 1);
                String semanticKeyQuery = "select form_id from (select form_id,group_concat(value) as vals,group_concat(item_id)" +
                        " as itemIds from (select form_id,value,item_id from formitem where item_id in (" + ids + ") order by item_id) group by form_id) " +
                        "where vals='" + vals + "' and itemIds='" + ids + "'";
                String tempFormId = DatabaseManager.getFormItemDao().queryRaw(semanticKeyQuery).getFirstResult()[0];
                Form tempForm = DatabaseManager.getFormDao().queryForId(tempFormId);
                if (form != null && !form.getFormId().equals(tempForm.getFormId())) {
                    return false;
                }

                if (form == null && tempForm != null)
                    return false;
            }
        } catch (Exception e) {

        }
        return true;
    }

    private String getMapValue(String fieldName) {
        View view = viewMap.get(fieldName);
        CLAZZ classEnum = CLAZZ.valueOf(view.getClass().getSimpleName());
        String value = "";
        switch (classEnum) {
            case EditText:
                value = ((EditText) view).getText().toString();
                break;
            case RadioGroup:
                RadioGroup rgp = (RadioGroup) view;
                int selectedId = rgp.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);
                if(selectedRadioButton!=null)
                    value = selectedRadioButton.getText().toString();
                break;
            case Spinner:
                value = ((Spinner) view).getSelectedItem().toString();
                break;
        }
        return value;
    }

    private enum CLAZZ {
        RadioGroup, EditText, Spinner
    }
}
