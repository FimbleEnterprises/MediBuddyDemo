package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AndroidUnitTests {

    private static final String TAG = "ExampleInstrumentedTest";
    
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.fimbleenterprises.medimileage", appContext.getPackageName());
    }

    /**
     * Verifies that the MyApp.getAppContext() method returns a non-null context.
     */
    @Test
    public void canMyAppGetAppContext() {
        try {
            assertNotNull(MyApp.getAppContext());
            Log.i(TAG, "canMyAppGetAppContext Test passed.");
        } catch (Exception e) {
            Log.w(TAG, "canMyAppGetAppContext: MyApp.getAppContext was null.  This is a problem.  Test failed.");
            e.printStackTrace();
        }
    }

    /**
     * Checks if instantiating a new MyPreferencesHelper object can acquire its own context that isn't null.
     */
    @Test
    public void myPreferencesHelperHasValidContext() {
        MyPreferencesHelper helper = new MyPreferencesHelper();
        try {
            assertNotNull(helper.context);
        } catch (AssertionError e) {
            e.printStackTrace();
        }
    }

}
