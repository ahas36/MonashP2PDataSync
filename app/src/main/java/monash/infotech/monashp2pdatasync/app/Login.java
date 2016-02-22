package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import monash.infotech.monashp2pdatasync.R;
import monash.infotech.monashp2pdatasync.connectivity.rest.RestClient;
import monash.infotech.monashp2pdatasync.entities.context.UserContext;
import monash.infotech.monashp2pdatasync.entities.context.UserRole;
import monash.infotech.monashp2pdatasync.messaging.MessageCreator;
import monash.infotech.monashp2pdatasync.security.Security;

public class Login extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText userName=((EditText)findViewById(R.id.login_username));
        final EditText password=((EditText)findViewById(R.id.login_password));
        ((Button)findViewById(R.id.BtnLogin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uNameTxt = userName.getText().toString();
                final String passTxt = password.getText().toString();
                new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... params) {
                        return RestClient.login(uNameTxt, passTxt);
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
                                MessageCreator.init(jsonObj.getString("token"));
                                //init user context
                                //decrypt token
                                String decryptToken = Security.getInstance().decrypt(jsonObj.getString("token"));
                                //convert token to json object
                                JSONObject jsonToken=new JSONObject(decryptToken);
                                UserContext uc=new UserContext(jsonToken.getInt("userId"), UserRole.valueOf(jsonToken.getString("role")));
                                ((P2PApplicationContext)getApplicationContext()).setUserContext(uc);
                                Intent intent = new Intent(Login.this, MainFragmentActivity.class);
                                startActivity(intent);
                                finish();
                            } catch (JSONException e) {
                                android.util.Log.d("Ali", e.getMessage());
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
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
