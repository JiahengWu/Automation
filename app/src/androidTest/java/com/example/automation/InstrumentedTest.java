package com.example.automation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static android.os.SystemClock.sleep;
import static org.junit.Assert.assertEquals;

// cd Library/Android/sdk/platform-tools
// ./adb shell am instrument -w com.example.automation.test/androidx.test.runner.AndroidJUnitRunner


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {

    private UiDevice device;

    //variables for launch an app
    private static final String APP_PACKAGE = "com.sina.weibo";
    private static final int LAUNCH_TIMEOUT = 5000;


    // get current date for folder name
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
    private Date curDate = new Date(System.currentTimeMillis());
    private String date_yyyyMMdd = dateFormatter.format(curDate);

    // paths for log files
    private String sdcardPath = Environment.getExternalStorageDirectory().getPath() + "/";
    private String logpath_activity = sdcardPath + date_yyyyMMdd + "/" + curDate + "-activity.txt";
    private String logpath_array = sdcardPath + date_yyyyMMdd + "/" + curDate + "-array.txt";
    private String logpath_position = sdcardPath + date_yyyyMMdd + "/" + curDate + "-pos.txt";
    private String PATH_TEST_SWIPE_ARRAY = sdcardPath + "gesture.txt";

    private long mDownTime;
    private Object bridgeObj;
    private Method injectInputEvent;

    private final int automationLoop = 2;
    private static final int MOTION_EVENT_INJECTION_DELAY_MILLIS = 0;
    private static final double FRAME_INTERVAL = 16.66666;

    @Before
    public void startMainActivityFromHomeScreen() throws Exception {

        //create folder for data
        makeDirectory(sdcardPath + date_yyyyMMdd);

        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        // device.pressHome();

        // Launch app
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //Clear out any previous instances
        context.startActivity(intent);
        //device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT);

        // Initialize UiAutomationBridge
        Field mUiAutomationBridge = Class.forName("android.support.test.uiautomator.UiDevice").getDeclaredField("mUiAutomationBridge");
        mUiAutomationBridge.setAccessible(true);

        bridgeObj = mUiAutomationBridge.get(device);
        injectInputEvent = Class.forName("android.support.test.uiautomator.UiAutomatorBridge")
                .getDeclaredMethod("injectInputEvent", android.view.InputEvent.class, boolean.class);

        // Read gestures and playback gestures on device
        for (int i = 0; i < automationLoop; i++) {
            //get the user gesture data
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(PATH_TEST_SWIPE_ARRAY)));
            String temp;
            while ((temp = br.readLine()) != null) {
                String[] ss = temp.trim().split(",");
                ArrayList<String> components = new ArrayList<>(Arrays.asList(ss));

                int id = Integer.parseInt(components.get(0));
                int pointCount = Integer.parseInt(components.get(2));

                int count = 4;
                Point[] P = new Point[pointCount];
                for (int c = 0; c < pointCount; c++) {
                    P[c] = new Point();
                    P[c].x = Integer.parseInt(components.get(count));
                    count += 1;
                    P[c].y = Integer.parseInt(components.get(count));
                    count += 1;
                }

                log_activity_start("start_swipe_array", id);
                swipe(P);
                log_activity_end("end_swipe_array");
                if (i == 0) {
                    log_array(components, P, pointCount);
                }
            }
            br.close();
            sleep(2000);
        }
    }

    // perform the swipe action
    private boolean swipe(Point[] segments) {
        boolean ret;

        // must have some points
        if (segments.length == 0)
            return false;

        // first touch starts exactly at the point requested
        mDownTime = SystemClock.uptimeMillis();
        long lastTime = mDownTime;
        ret = touchDown(segments[0].x, segments[0].y);
        int currSkipCount = 0;

        for (int seg = 1; seg < segments.length - 1; seg++) {
            sleep(MOTION_EVENT_INJECTION_DELAY_MILLIS);
            long currTime = SystemClock.uptimeMillis();

            if (currSkipCount < totalSkipCount(lastTime, currTime)) {
                currSkipCount += 1;
                continue;
            }

            currSkipCount = 0;
            ret &= touchMove(segments[seg].x, segments[seg].y, currTime);
            lastTime = currTime;

            if (!ret) {
                break;
            }
        }

        sleep(MOTION_EVENT_INJECTION_DELAY_MILLIS);
        long currTime = SystemClock.uptimeMillis();
        ret &= touchUp(segments[segments.length - 1].x, segments[segments.length - 1].y, currTime);
        return ret;
    }

    //create folder to keep data
    private static void makeDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    // log_activity_start function start
    private void log_activity_start(String str, int id) {
        String time = "" + System.currentTimeMillis();
        writeFile(logpath_activity, time + "," + str + "," + id);
    }

    // log_activity_end function start
    private void log_activity_end(String str) {
        String time = "" + System.currentTimeMillis();
        writeFile(logpath_activity, time + "," + str);
    }

    // log positions of each touch event
    private void log_pos(long timestamp, int x, int y, String category) {
        writeFile(logpath_position, timestamp + "," + x + "," + y + "," + category);
    }

    // log_array function start
    private void log_array(ArrayList list, Point[] point_array, int point_number) {
        String line = "";
        line += list.get(0);
        for (int i = 1; i < 4; i++) {
            line += "," + list.get(i);
        }
        for (int i = 0; i < point_number; i++) {
            line += "," + point_array[i].x + "," + point_array[i].y;
        }
        writeFile(logpath_array, line);
    }

    // how many frames should we skip (due to lag)?
    private int totalSkipCount(long lastTime, long currTime) {
        double duration = currTime - lastTime;
        double mul = duration / FRAME_INTERVAL;
        if (mul <= 1)
            return 0;

        // use 1.7, 2.7, etc. as a threshold
        if (mul > Math.round(mul) - 0.3)
            return (int) Math.round(mul) - 1;
        else
            return (int) Math.round(mul) - 2;
    }

    private boolean touchDown(int x, int y) {
        MotionEvent event = MotionEvent.obtain(
                mDownTime, mDownTime, MotionEvent.ACTION_DOWN, x, y, 0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        log_pos(mDownTime, x, y, "down");
        return injectEventSync(event);
    }

    private boolean touchMove(int x, int y, long time) {
        MotionEvent event = MotionEvent.obtain(
                mDownTime, time, MotionEvent.ACTION_MOVE, x, y, 0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        log_pos(time, x, y, "move");
        return injectEventSync(event);
    }

    private boolean touchUp(int x, int y, long time) {
        MotionEvent event = MotionEvent.obtain(
                mDownTime, time, MotionEvent.ACTION_UP, x, y, 0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        log_pos(time, x, y, "up");
        mDownTime = 0;
        return injectEventSync(event);
    }

    private boolean injectEventSync(InputEvent event) {
        try {
            Log.e("touch-event", "" + event.getEventTime());
            return ((Boolean) injectInputEvent.invoke(bridgeObj, event, true));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // write content to file, then start a new line
    private void writeFile(String filename, String content) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(filename, true);
            fw.write(content + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.example.automation", appContext.getPackageName());
    }
}
