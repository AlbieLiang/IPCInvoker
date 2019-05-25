package cc.suitalk.ipcinvoker;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;

import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/10/17.
 */

class BindServiceExecutor {

    private static final String TAG = "IPC.BindServiceExecutor";

    private static Handler sBindServiceHandler;

    static {
        HandlerThread thread = new HandlerThread("IPC.BindServiceExecutor-Thread") {
            @Override
            protected void onLooperPrepared() {
                Log.i(TAG, "onLooperPrepared(tid : %s)", getId());
            }
        };
        thread.start();
        sBindServiceHandler = new Handler(thread.getLooper());
    }

    public static boolean bindService(final Context context, final Intent intent, final ServiceConnection conn, final int flags) {
//        sBindServiceHandler.removeCallbacks();
        return sBindServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                context.bindService(intent, conn, flags);
            }
        });
    }

    public static boolean unbindService(final Context context, final ServiceConnection conn) {
        return sBindServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    context.unbindService(conn);
                } catch (Exception e) {
                    Log.e(TAG, "unbindService error, %s", android.util.Log.getStackTraceString(e));
                }
            }
        });
    }
}
