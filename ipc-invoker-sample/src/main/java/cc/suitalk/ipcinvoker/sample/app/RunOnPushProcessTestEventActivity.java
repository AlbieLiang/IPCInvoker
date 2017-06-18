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
import cc.suitalk.ipcinvoker.event.IPCObserver;
import cc.suitalk.ipcinvoker.sample.IPCSampleData;
import cc.suitalk.ipcinvoker.sample.R;
import cc.suitalk.ipcinvoker.sample.event.IPC;
import cc.suitalk.ipcinvoker.sample.event.MObservable;
import cc.suitalk.ipcinvoker.sample.event.OnClickEventDispatcher;
import cc.suitalk.ipcinvoker.sample.service.PushProcessIPCService;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/6/18.
 */

public class RunOnPushProcessTestEventActivity extends AppCompatActivity {

    private static final String TAG = "IPCInvokerSample.RunOnPushProcessTestEventActivity";

    TextView clientMsgPanelTv;
    View registerByClientBtn;
    View unregisterByClientBtn;

    EditText processEt;
    TextView observerMsgPanelTv;
    View registerByObserverBtn;
    View unregisterByObserverBtn;
    View publishEventBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_on_push_process_test_event_activity);
        setTitle(R.string.push_process);

        final IPCObserver observer = new IPCObserver() {
            @Override
            public void onCallback(Bundle data) {
                Log.i(TAG, "register observer by client, onCallback(%s, %s)", hashCode(), data);
            }
        };
        clientMsgPanelTv = (TextView) findViewById(R.id.clientMsgPanelTv);
        registerByClientBtn = findViewById(R.id.registerByClientBtn);
        unregisterByClientBtn = findViewById(R.id.unregisterByClientBtn);

        registerByClientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        IPC.getMainIPCClient().registerIPCObserver(OnClickEventDispatcher.class.getName(), observer);
                    }
                });
            }
        });

        unregisterByClientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        IPC.getMainIPCClient().unregisterIPCObserver(OnClickEventDispatcher.class.getName(), observer);
                    }
                });
            }
        });

        //
        final IPCObserver observer1 = new IPCObserver() {
            @Override
            public void onCallback(Bundle data) {
                Log.i(TAG, "register observer by client, onCallback(%s, %s)", hashCode(), data);
            }
        };
        processEt = (EditText) findViewById(R.id.remoteProcessNameEt);
        observerMsgPanelTv = (TextView) findViewById(R.id.observerMsgPanelTv);
        registerByObserverBtn = findViewById(R.id.registerByObserverBtn);
        unregisterByObserverBtn = findViewById(R.id.unregisterByObserverBtn);
        publishEventBtn = findViewById(R.id.publishEventBtn);

        registerByObserverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        MObservable.get(processEt.getText().toString(), OnClickEventDispatcher.class).registerIPCObserver(observer1);
                    }
                });
            }
        });
        unregisterByObserverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        MObservable.get(processEt.getText().toString(), OnClickEventDispatcher.class).unregisterIPCObserver(observer1);
                    }
                });
            }
        });
        publishEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        Bundle data = new Bundle();
                        IPC.getMainIPCClient().invokeSync(data, IPCInvokeTask_PublishEvent.class);
                    }
                });
            }
        });

    }

    private static class IPCInvokeTask_PublishEvent implements IPCSyncInvokeTask {

        @Override
        public Bundle invoke(Bundle data) {
            OnClickEventDispatcher dispatcher = new OnClickEventDispatcher();
            IPCSampleData event = new IPCSampleData();
            event.result = String.format("current process name : %s, pid : %s",
                    IPCInvokeLogic.getCurrentProcessName(), android.os.Process.myPid());
            dispatcher.dispatch(event);
            Log.i(TAG, "publish event(%s)", event.result);
            return null;
        }
    }
}
