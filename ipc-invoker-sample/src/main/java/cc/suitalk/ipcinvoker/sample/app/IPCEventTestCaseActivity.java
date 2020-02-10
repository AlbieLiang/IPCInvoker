/*
 *  Copyright (C) 2017-present Albie Liang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package cc.suitalk.ipcinvoker.sample.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cc.suitalk.ipcinvoker.IPCInvokeLogic;
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.ThreadCaller;
import cc.suitalk.ipcinvoker.event.IPCObserver;
import cc.suitalk.ipcinvoker.inner.IPCData;
import cc.suitalk.ipcinvoker.sample.IPCSampleData;
import cc.suitalk.ipcinvoker.sample.R;
import cc.suitalk.ipcinvoker.sample.event.IPC;
import cc.suitalk.ipcinvoker.sample.event.MObservable;
import cc.suitalk.ipcinvoker.sample.event.OnClickEventDispatcher;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/6/18.
 */

public class IPCEventTestCaseActivity extends AppCompatActivity {

    private static final String TAG = "IPCInvokerSample.IPCEventTestCaseActivity";

    TextView clientMsgPanelTv;

    EditText processEt;
    TextView observerMsgPanelTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ipc_event_test_activity);
        setTitle(R.string.push_process);
        getSupportActionBar().setSubtitle("IPCEvent TestCase");

        final IPCObserver<IPCData> observer = new IPCObserver<IPCData>() {
            @Override
            public void onCallback(final IPCData data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IPCSampleData d = (IPCSampleData) data;
                        String log = String.format("register observer by client, onCallback(%s), cost : %s",
                                d.result, (System.nanoTime() - d.timestamp) / 1000000.0d);
                        Log.i(TAG, log);
                        clientMsgPanelTv.setText(log);
                    }
                });
            }
        };
        clientMsgPanelTv = (TextView) findViewById(R.id.clientMsgPanelTv);

        findViewById(R.id.registerByClientBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPC.getMainIPCClient().registerIPCObserver(OnClickEventDispatcher.class, observer);
            }
        });

        findViewById(R.id.unregisterByClientBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPC.getMainIPCClient().unregisterIPCObserver(OnClickEventDispatcher.class, observer);
            }
        });

        //
        final IPCObserver<Bundle> observer1 = new IPCObserver<Bundle>() {
            @Override
            public void onCallback(final Bundle data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String log = String.format("register observer by Observable, onCallback(%s), cost : %s",
                                data.getString("result"), (System.nanoTime() - data.getLong("timestamp")) / 1000000.0d);
                        Log.i(TAG, log);
                        observerMsgPanelTv.setText(log);
                    }
                });
            }
        };
        processEt = (EditText) findViewById(R.id.remoteProcessNameEt);
        observerMsgPanelTv = (TextView) findViewById(R.id.observerMsgPanelTv);

        findViewById(R.id.registerByObserverBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MObservable.get(processEt.getText().toString(), OnClickEventDispatcher.class).registerIPCObserver(observer1);
            }
        });
        findViewById(R.id.unregisterByObserverBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MObservable.get(processEt.getText().toString(), OnClickEventDispatcher.class).unregisterIPCObserver(observer1);
            }
        });
        findViewById(R.id.publishEventBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadCaller.post(new Runnable() {
                    @Override
                    public void run() {
                        IPC.getMainIPCClient().invokeSync(null, IPCInvokeTask_PublishEvent.class);
                    }
                });
            }
        });

    }

    private static class IPCInvokeTask_PublishEvent implements IPCSyncInvokeTask<Bundle, Bundle> {

        @Override
        public Bundle invoke(Bundle data) {
            OnClickEventDispatcher dispatcher = new OnClickEventDispatcher();
            IPCSampleData event = new IPCSampleData();
            event.result = String.format("current process name : %s, task : %s, pid : %s, time : %s",
                    IPCInvokeLogic.getCurrentProcessName(), hashCode(), android.os.Process.myPid(), System.currentTimeMillis());
            event.timestamp = System.nanoTime();
            dispatcher.dispatch(event);
            Log.i(TAG, "publish event(%s)", event.result);
            return null;
        }
    }
}
