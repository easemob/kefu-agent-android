package helpdesk.easemob.com.hdapplication;

import android.util.Log;

import static junit.framework.Assert.assertTrue;

/**
 * Created by liyuzhao on 16/2/22.
 */
public class LoginTest{

    protected int i1;
    protected int i2;
    static final String LOG_TAG = "LoginTest";

    public void testAdd() {
        Log.d(LOG_TAG, "testAdd");
        assertTrue(LOG_TAG + "1", ((i1 + i2) == 5));
    }


}
