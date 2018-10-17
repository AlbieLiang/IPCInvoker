package cc.suitalk.ipcinvoker;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by aierbi on 2018/10/17.
 */

class BindServiceExecutor {

    private static Handler sBindServiceHandler;

    static {
        HandlerThread thread = new HandlerThread("IPC.BindServiceExecutor-Thread") {
            @Override
            protected void onLooperPrepared() {
                sBindServiceHandler = new Handler();
            }
        };
        thread.start();
        sBindServiceHandler = new Handler(thread.getLooper());
    }

    public static boolean bindService(final Context context, final Intent intent, final ServiceConnection conn, final int flags) {
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
                context.unbindService(conn);
            }
        });
    }
}
