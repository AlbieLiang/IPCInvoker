package cc.suitalk.ipcinvoker.sample.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import cc.suitalk.ipcinvoker.IPCInvokeClient;
import cc.suitalk.ipcinvoker.IPCInvokeLogic;
import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.event.IPCDispatcher;
import cc.suitalk.ipcinvoker.event.IPCObserver;
import cc.suitalk.ipcinvoker.sample.R;
import cc.suitalk.ipcinvoker.sample.app.model.ThreadPool;
import cc.suitalk.ipcinvoker.sample.service.MainProcessIPCService;
import cc.suitalk.ipcinvoker.sample.service.PushProcessIPCService;
import cc.suitalk.ipcinvoker.sample.service.SupportProcessIPCService;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.type.IPCVoid;

public class IPCObserverRestoreTestActivity extends AppCompatActivity {
    private static final String TAG = "IPCInvoker.IPCObserverRestoreTestActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ipc_observer_restore_test_activity);
        setTitle(R.string.app_name);
        getSupportActionBar().setSubtitle("IPCObserver Restore Test");

        findViewById(R.id.addObserverFromMainToSupportBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        addObserverFromMainToSupport();
                    }
                });
            }
        });
        findViewById(R.id.addObserverFromPushToSupportBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        addObserverFromPushToSupport();
                    }
                });
            }
        });
        findViewById(R.id.exitSupportBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        exitSupport();
                    }
                });
            }
        });
        findViewById(R.id.startSupportBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        startSupport();
                    }
                });
            }
        });
        findViewById(R.id.dispatchFromSupportBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        dispatchFromSupport();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                clear();
            }
        });
    }

    private void dispatchFromSupport() {
        IPCInvoker.invokeSync(SupportProcessIPCService.PROCESS_NAME, null, IPCDispatchSyncTask.class);
    }

    private void startSupport() {
        IPCInvoker.invokeSync(SupportProcessIPCService.PROCESS_NAME, null, IPCTestSyncTask.class);
    }

    private void exitSupport() {
        IPCInvoker.invokeSync(SupportProcessIPCService.PROCESS_NAME, null, IPCExitSyncTask.class);
    }

    private void addObserverFromPushToSupport() {
        IPCInvoker.invokeSync(PushProcessIPCService.PROCESS_NAME, null, IPCAddObserverSyncTask.class);
    }

    private void addObserverFromMainToSupport() {
        IPCInvoker.invokeSync(MainProcessIPCService.PROCESS_NAME, null, IPCAddObserverSyncTask.class);
    }

    private void clear() {
        IPCInvoker.invokeSync(MainProcessIPCService.PROCESS_NAME, null, IPCRemoveObserverSyncTask.class);
        IPCInvoker.invokeSync(PushProcessIPCService.PROCESS_NAME, null, IPCRemoveObserverSyncTask.class);
    }

    private static final IPCObserver observer = new IPCObserver() {
        @Override
        public void onCallback(Bundle data) {
            Log.i(TAG, "observer, from %s", IPCInvokeLogic.getCurrentProcessName());
        }
    };

    private static class IPCAddObserverSyncTask implements IPCSyncInvokeTask<IPCVoid, IPCVoid> {
        @Override
        public IPCVoid invoke(IPCVoid data) {
            Log.i(TAG, "IPCAddObserverSyncTask, invoke, %s", IPCInvokeLogic.getCurrentProcessName());
            IPCInvokeClient client = new IPCInvokeClient(SupportProcessIPCService.PROCESS_NAME);
            client.registerIPCObserver(IPCTestDispatcher.class.getName(), observer);
            return null;
        }
    }

    private static class IPCRemoveObserverSyncTask implements IPCSyncInvokeTask<IPCVoid, IPCVoid> {
        @Override
        public IPCVoid invoke(IPCVoid data) {
            Log.i(TAG, "IPCRemoveObserverSyncTask, invoke, %s", IPCInvokeLogic.getCurrentProcessName());
            IPCInvokeClient client = new IPCInvokeClient(SupportProcessIPCService.PROCESS_NAME);
            client.unregisterIPCObserver(IPCTestDispatcher.class.getName(), observer);
            return null;
        }
    }

    private static class IPCTestDispatcher extends IPCDispatcher {
        private static final IPCTestDispatcher INSTANCE = new IPCTestDispatcher();
        private static void dispatch() {
            INSTANCE.dispatch(new Bundle());
        }
    }

    private static class IPCDispatchSyncTask implements IPCSyncInvokeTask<IPCVoid, IPCVoid> {
        @Override
        public IPCVoid invoke(IPCVoid data) {
            Log.i(TAG, "IPCDispatchSyncTask, invoke, %s", IPCInvokeLogic.getCurrentProcessName());
            IPCTestDispatcher.dispatch();
            return null;
        }
    }

    private static class IPCTestSyncTask implements IPCSyncInvokeTask<IPCVoid, IPCVoid> {
        @Override
        public IPCVoid invoke(IPCVoid data) {
            Log.i(TAG, "IPCTestSyncTask, invoke, %s", IPCInvokeLogic.getCurrentProcessName());
            return null;
        }
    }

    private static class IPCExitSyncTask implements IPCSyncInvokeTask<IPCVoid, IPCVoid> {
        @Override
        public IPCVoid invoke(IPCVoid data) {
            Log.i(TAG, "IPCExitSyncTask, invoke, %s", IPCInvokeLogic.getCurrentProcessName());
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            return null;
        }
    }

}
