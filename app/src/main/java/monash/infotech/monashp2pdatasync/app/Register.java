package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.entities.UserRole;
import monash.infotech.monashp2pdatasync.restclient.RestClient;
import monash.infotech.monashp2pdatasync.security.Security;

public class Register extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final EditText userName=((EditText)findViewById(R.id.register_uName));
        final EditText password=((EditText)findViewById(R.id.register_uName));
        final Spinner role=((Spinner)findViewById(R.id.register_role));
        new AsyncTask<Void,Void,List<UserRole>>(){

            @Override
            protected List<UserRole> doInBackground(Void... params) {

                return RestClient.getRoles();
            }
            @Override
            protected void onPostExecute(List<UserRole> c)
            {
                List<String> list = new ArrayList<String>();
                for(UserRole ur:c)
                {
                    list.add(ur.getUserRoleTtile());
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Register.this,
                        android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                role.setAdapter(dataAdapter);
            }
            }.execute();
        ((Button)findViewById(R.id.BtnRegister)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uNameTxt= userName.getText().toString();
                final String passTxt= password.getText().toString();
                final String roleTxt= String.valueOf(role.getSelectedItem());
                new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... params) {
                        return RestClient.register(uNameTxt,passTxt,roleTxt);
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        if(!result.isEmpty())
                        {
                            try
                            {
                                JSONObject jsonObj = new JSONObject(result);
                                ((P2PApplicationContext)getApplicationContext()).setToken(jsonObj.getString("token"));
                                ((P2PApplicationContext)getApplicationContext()).setKey(jsonObj.getJSONObject("key").toString());
                                Security.init(new BigInteger(jsonObj.getJSONObject("key").getString("mod")), new BigInteger(jsonObj.getJSONObject("key").getString("exp")));
                                Intent intent = new Intent(Register.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }.execute();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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
