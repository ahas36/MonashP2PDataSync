package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        P2PApplicationContext context= (P2PApplicationContext) getApplicationContext();
        if(context.getToken().isEmpty() || context.getKey().isEmpty())
        {
            Intent intent = new Intent(context, Login.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
