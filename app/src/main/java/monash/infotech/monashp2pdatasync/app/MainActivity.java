package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import monash.infotech.monashp2pdatasync.entities.context.UserContext;
import monash.infotech.monashp2pdatasync.entities.context.UserRole;
import monash.infotech.monashp2pdatasync.messaging.MessageCreator;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        P2PApplicationContext context= (P2PApplicationContext) getApplicationContext();
        if((context.getToken().isEmpty() || context.getKey().isEmpty()))
        {
            Intent intent = new Intent(context, Login.class);
            startActivity(intent);
            finish();
        }
        else
        {
            MessageCreator.init(context.getToken());
            Intent intent = new Intent(context, MainFragmentActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
