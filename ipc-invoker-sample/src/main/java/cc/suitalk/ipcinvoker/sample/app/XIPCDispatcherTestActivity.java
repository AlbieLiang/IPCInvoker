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
import cc.suitalk.ipcinvoker.IPCRemoteAsyncInvoke;
import cc.suitalk.ipcinvoker.IPCRemoteInvokeCallback;
import cc.suitalk.ipcinvoker.annotation.Singleton;
import cc.suitalk.ipcinvoker.extension.event.XIPCDispatcher;
import cc.suitalk.ipcinvoker.extension.event.XIPCObserver;
import cc.suitalk.ipcinvoker.sample.R;
import cc.suitalk.ipcinvoker.sample.event.IPC;
import cc.suitalk.ipcinvoker.sample.event.XObservable;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.type.IPCString;

/**
 * Created by albieliang on 2017/7/20.
 */

public class XIPCDispatcherTestActivity extends AppCompatActivity {

    private static final String TAG = "IPCInvokerSample.XIPCDispatcherTestActivity";

    EditText processEt;
    TextView observerMsgPanelTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xipcdispatcher_test_activity);
        setTitle(R.string.support_process);
        //
        final XIPCObserver<IPCString> observer1 = new XIPCObserver<IPCString>() {
            @Override
            public void onCallback(final IPCString data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String log = String.format("register observer by Observable, onCallback(%s)", data);
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
                String process = processEt.getText().toString();
                XObservable.get(process, XIPCDispatcherImpl.class).registerIPCObserver(observer1);
            }
        });
        findViewById(R.id.unregisterByObserverBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String process = processEt.getText().toString();
                XObservable.get(process, XIPCDispatcherImpl.class).unregisterIPCObserver(observer1);
            }
        });
        findViewById(R.id.publishEventBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPC.getMainIPCClient().invokeAsync(null, IPCInvokeTask_PublishEvent.class, null);
            }
        });

    }

    @Singleton
    private static class IPCInvokeTask_PublishEvent implements IPCRemoteAsyncInvoke<Bundle, Bundle> {

        @Override
        public void invoke(Bundle data, IPCRemoteInvokeCallback<Bundle> callback) {
            XIPCDispatcherImpl dispatcher = new XIPCDispatcherImpl();
            IPCString event = new IPCString();
            event.value = String.format("current process name : %s, task : %s, pid : %s, time : %s",
                    IPCInvokeLogic.getCurrentProcessName(), hashCode(), android.os.Process.myPid(), System.currentTimeMillis());
            dispatcher.dispatch(event);
            Log.i(TAG, "publish event(%s)", event.value);
        }
    }

    private static class XIPCDispatcherImpl extends XIPCDispatcher<IPCString> {
    }
}
