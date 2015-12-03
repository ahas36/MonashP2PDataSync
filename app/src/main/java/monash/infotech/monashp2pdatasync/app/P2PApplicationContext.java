package monash.infotech.monashp2pdatasync.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import monash.infotech.monashp2pdatasync.security.Security;

/**
 * Created by john on 11/30/2015.
 */
public class P2PApplicationContext extends android.app.Application {

    private String token;
    private String key;
    SharedPreferences sp;

    @Override
    public void onCreate()
    {
        super.onCreate();

        sp = getSharedPreferences("P2P", Context.MODE_PRIVATE);
        sp.edit().clear().commit();
        token=sp.getString("token","");
        key=sp.getString("key","");
        if(!key.isEmpty())
        {
            initSecurity();
        }
    }

    public  String getToken() {
        return token;
    }

    public  void setToken(String token) {
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("token",token);
        edit.commit();
        this.token = token;
    }
    public  String getKey() {
        return key;
    }
    public void initSecurity()
    {
        Gson gson=new Gson();
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(key);
            Security.init(new BigInteger(jsonObj.getJSONObject("key").getString("mod")),new BigInteger(jsonObj.getJSONObject("key").getString("exp")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public  void setKey(String key) {

        SharedPreferences.Editor edit = sp.edit();
        edit.putString("key",key);
        edit.commit();
        this.key = key;
    }
}