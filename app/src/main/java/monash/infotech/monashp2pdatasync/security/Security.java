package monash.infotech.monashp2pdatasync.security;


import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import monash.infotech.monashp2pdatasync.entities.context.UserContext;

/**
 * Created by john on 11/30/2015.
 *
 * A class for handling authentication
 *
 * init and generate the public key
 */
public class Security {
    //singleton instance
    private static Security instance;
    //
    public static Security getInstance() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized Security getSync() {
        if (instance == null) {
            instance = new Security();
        }
        return instance;
    }

    private static BigInteger modulus;
    private static BigInteger exponent;

    private Key publicKey;
    private Cipher cipher;
    //init:  set the modulues and exponent for generating public key
    public static void init(BigInteger modulus,BigInteger exponent)
    {
        Security.modulus=modulus;
        Security.exponent=exponent;
    }

    public Security() {
        try {

            //Get Public Key
            RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            publicKey = fact.generatePublic(rsaPublicKeySpec);
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (Exception ex) {

        }
    }

    //decrypt with the use of public key
    public  String decrypt(String text) {
        byte[] dectyptedText = null;
        try {
            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] data=Base64.decode(text, Base64.NO_WRAP);
            String str="";
            for(int i=0;i<data.length;i++)
            {
                str+=data[i]+"\n";
            }
            android.util.Log.d("ali",str);
            dectyptedText = cipher.doFinal(Base64.decode(text, Base64.NO_WRAP));
            return new String(dectyptedText);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //authenticate the user
    public boolean authenticate(UserContext user,String token) throws JSONException {
        //decrypt token
        try {
            String decryptToken = this.decrypt(token);
            //convert token to json object
            JSONObject jsonToken = new JSONObject(decryptToken);
            //if the user id and role match return true, else false
            return ((user.getId() == jsonToken.getInt("userId")) && user.getRole().toString().equals(jsonToken.getString("role")));
        }
        catch (Exception e)
        {
            return false;
        }
    }
}

