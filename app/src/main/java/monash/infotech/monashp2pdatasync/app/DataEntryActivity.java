package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import monash.infotech.monashp2pdatasync.R;

public class DataEntryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_entry);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.form);
        InputStream is = getResources().openRawResource(R.raw.schema);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String jsonString = writer.toString();
        try {
            JSONObject jo = new JSONObject(jsonString);
            JSONArray ja = jo.getJSONArray("form");
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject temp = ja.getJSONObject(i);
                String title = temp.getString("title");
                JSONArray items = temp.getJSONArray("items");
                LinearLayout ly = new LinearLayout(this);
                ly.setLayoutParams(lparams);
                ly.setOrientation(LinearLayout.VERTICAL);
                ly.setBackground(getResources().getDrawable(R.drawable.border));
                linearLayout.addView(ly);
                TextView tv = new TextView(this);
                tv.setLayoutParams(lparams);
                tv.setText(title);
                ly.addView(tv);


                for (int j = 0; j < items.length(); j++) {
                    JSONObject item = items.getJSONObject(j);
                    String type = item.getString("type");

                    switch (type) {
                        case "select": {
                            if(item.has("title")) {
                                String taTitle=item.getString("title");
                                TextView taTV = new TextView(this);
                                taTV.setLayoutParams(lparams);
                                taTV.setText(taTitle);
                                ly.addView(taTV);
                            }
                            Spinner spinner = new Spinner(this);
                            spinner.setLayoutParams(lparams);
                            JSONArray radioList = item.getJSONArray("enums");
                            JSONObject titleMap = item.getJSONObject("titleMap");
                            List<String> values = new ArrayList<>();
                            for (int q = 0; q < radioList.length(); q++) {
                                values.add(titleMap.getString(radioList.getString(q)));
                            }
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                                    android.R.layout.simple_spinner_item, values);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(dataAdapter);
                            ly.addView(spinner);
                        }
                            break;
                        case "textarea":
                            if(item.has("title")) {
                                String taTitle=item.getString("title");
                                TextView taTV = new TextView(this);
                                taTV.setLayoutParams(lparams);
                                taTV.setText(taTitle);
                                ly.addView(taTV);
                            }
                            EditText txtArea = new EditText(this);
                            txtArea.setLayoutParams(lparams);
                            ly.addView(txtArea);
                            break;
                        case "radio":
                            String radioTitle = item.getString("title");
                            RadioGroup rgp = new RadioGroup(this);
                            TextView radioTV = new TextView(this);
                            radioTV.setLayoutParams(lparams);
                            radioTV.setText(radioTitle);
                            rgp.setLayoutParams(lparams);
                            rgp.setOrientation(LinearLayout.HORIZONTAL);
                            ly.addView(radioTV);
                            ly.addView(rgp);
                            JSONArray radioList=item.getJSONArray("enums");
                            if(radioList.length()>4)
                            {
                                rgp.setOrientation(LinearLayout.VERTICAL);
                            }
                            JSONObject titleMap=item.getJSONObject("titleMap");
                            for (int q = 0; q < radioList.length(); q++) {
                                RadioButton radioButton = new RadioButton(this);
                                radioButton.setText(titleMap.getString(radioList.getString(q)));
                                rgp.addView(radioButton);
                            }
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
}
