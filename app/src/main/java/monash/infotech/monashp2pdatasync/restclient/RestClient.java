package monash.infotech.monashp2pdatasync.restclient;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import monash.infotech.monashp2pdatasync.entities.UserRole;

/**
 * Created by john on 11/30/2015.
 */
public class RestClient {

    private static final String HOST_ADDRESS="http://192.168.1.4:8080/P2PServer/webresources/";

    public static String  register(String username,String password, String role)
    {
        String[][] queryParams = new String[][]{};
        String[][] pathParams = new String[][]{{"userName",username},{"password",password},{"role",role}};

        RestConnection conn = new
                RestConnection(HOST_ADDRESS+"entities.users/register/userName/password/role", pathParams, queryParams);
        try {


        RestResponse response = conn.get();
            return response.getDataAsString();
        }
        catch (Exception e){}
        return null;
    }
    public static String  login(String username,String password)
    {
        String[][] queryParams = new String[][]{};
        String[][] pathParams = new String[][]{{"userName",username},{"password",password}};

        RestConnection conn = new
                RestConnection(HOST_ADDRESS+"entities.users/login/userName/password", pathParams, queryParams);
        try {
            RestResponse response = conn.get();
            return response.getDataAsString();
        }
        catch (Exception e){}
        return null;
    }
    public static List<UserRole> getRoles()
    {
        String[][] pathParams = new String[][]{};
        String[][] queryParams = new String[][]{};

        RestConnection conn = new
                RestConnection(HOST_ADDRESS+"entities.userrole", pathParams, queryParams);
        try {


            RestResponse response = conn.get();
            Type listType = new TypeToken<ArrayList<UserRole>>() {
            }.getType();
            return response.getDataAsObject(listType);
        }
        catch (Exception e){}
        return null;
    }
}
