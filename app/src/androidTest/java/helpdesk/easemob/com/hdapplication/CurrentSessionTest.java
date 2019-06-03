package helpdesk.easemob.com.hdapplication;

import android.test.AndroidTestCase;
import android.util.Log;

import com.hyphenate.kefusdk.HDDataCallBack;
import com.hyphenate.kefusdk.entity.HDSession;
import com.hyphenate.kefusdk.manager.session.CurrentSessionManager;

import java.util.List;


/**
 * Created by liyuzhao on 16/10/8.
 */
public class CurrentSessionTest extends AndroidTestCase {

    static final String LOG_TAG = "SessionTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    boolean getSessionTimeout = false;

    public void testGetAllSession(){
        getSessionTimeout = true;
        final long startTime = System.currentTimeMillis();
        final Object mutex = new Object();
        CurrentSessionManager.getInstance().getSessionsFromServer(new HDDataCallBack<List<HDSession>>() {

            @Override
            public void onSuccess(List<HDSession> value) {
                getSessionTimeout = false;
                synchronized (mutex){
                    mutex.notify();
                }
                if (value.size() <= 0){
                    fail("get session value is empty");
                    return;
                }
                Log.e(LOG_TAG, "get session spend time(ms):" + (System.currentTimeMillis() - startTime));
                Log.e(LOG_TAG, "get session value:" + value);
                assertTrue(true);
            }

            @Override
            public void onError(int error, String errorMsg) {
                getSessionTimeout = false;
                synchronized (mutex){
                    mutex.notify();
                }
                fail("get session error:" + errorMsg);
            }

            @Override
            public void onAuthenticationException() {
                getSessionTimeout = false;
                synchronized (mutex){
                    mutex.notify();
                }
                fail("get session onAuthenticationException");
            }
        });

        synchronized (mutex){
            try {
                mutex.wait(20 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertFalse("get session timeout 20s", getSessionTimeout);

    }


}
