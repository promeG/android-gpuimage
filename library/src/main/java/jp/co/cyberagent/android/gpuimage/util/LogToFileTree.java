package jp.co.cyberagent.android.gpuimage.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by guyacong on 2015/1/10.
 *
 * user Timber, save to file
 */
public final class LogToFileTree extends Timber.DebugTree {

    private static final String TAG = "YOLO_TEST_12_30";
    static final String LOG_FILE = "yolo-test-12-30.log";
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void v(String message, Object... args) {
        super.v(message, args);
        logToFile(message);
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        super.v(t, message, args);
        logToFile(message);
    }

    @Override
    public void d(String message, Object... args) {
        //super.d(message, args);
        logToFile(message);
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        super.d(t, message, args);
        logToFile(message);
    }

    @Override
    public void w(String message, Object... args) {
        super.w(message, args);
        logToFile(message);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        super.w(t, message, args);
        logToFile(message);
    }

    @Override
    public void i(String message, Object... args) {
        //super.i(message, args);
        logToFile(message);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        super.i(t, message, args);
        logToFile(message);
    }

    @Override
    public void e(String message, Object... args) {
        super.e(message, args);
        logToFile(message);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        super.e(t, message, args);
        logToFile(message);
    }

    private void logToFile(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        Log.d(TAG, message);

        if(true) {
            return;
        }

        String logDir = Environment.getExternalStorageDirectory() + File.separator + LOG_FILE;
        if (TextUtils.isEmpty(logDir)) {
            return;
        }

        BufferedWriter buf = null;
        try {
            File logFile = new File(logDir);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            buf = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"));
            //buf.append("Timber: " + DATE_FORMAT.format(new Date()));
            //buf.newLine();
            buf.append(message);
            buf.newLine();
            buf.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (buf != null) {
                    buf.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

}
