package monash.infotech.monashp2pdatasync.data.db;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;
/**
 * Created by john on 12/4/2015.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    public static void main(String[] args) throws SQLException, IOException {
        // Provide the name of .txt file which you have already created and kept in res/raw directory
        writeConfigFile("ormlite_config.txt");
    }
}
