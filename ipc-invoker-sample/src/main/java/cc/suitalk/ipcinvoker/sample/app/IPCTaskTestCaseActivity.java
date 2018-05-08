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

import cc.suitalk.ipcinvoker.IPCAsyncInvokeTask;
import cc.suitalk.ipcinvoker.IPCInvokeCallback;
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.annotation.Singleton;
import cc.suitalk.ipcinvoker.extension.XIPCInvoker;
import cc.suitalk.ipcinvoker.sample.R;
import cc.suitalk.ipcinvoker.sample.app.model.ThreadPool;
import cc.suitalk.ipcinvoker.sample.nimble.TestType;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/5/8.
 */

public class IPCTaskTestCaseActivity extends AppCompatActivity {

    private static final String TAG = "IPCInvokerSample.IPCTaskTestCaseActivity";

    TextView msgPanelTv;
    View syncInvokeBtn;
    View asyncInvokeBtn;

    EditText processEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xipcinvoker_test_case_activity);
        setTitle(R.string.app_name);
        getSupportActionBar().setSubtitle("IPCTask TestCase");

        msgPanelTv = (TextView) findViewById(R.id.msgPanelTv);
        syncInvokeBtn = findViewById(R.id.syncInvokeBtn);
        asyncInvokeBtn = findViewById(R.id.asyncInvokeBtn);

        syncInvokeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String process = processEt.getText().toString();
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        TestType data = new TestType();
                        data.key = "wx-developer";
                        data.value = "XIPCInvoker";
                        final Integer result = XIPCInvoker.invokeSync(process, data, IPCInvokeTask_getInt.class);

                        Log.i(TAG, "result : %s", result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msgPanelTv.setText("get result : " + result);
                            }
                        });
                    }
                });
            }
        });

        asyncInvokeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String process = processEt.getText().toString();
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        TestType data = new TestType();
                        data.key = "wx-developer";
                        data.value = "AlbieLiang";
                        XIPCInvoker.invokeAsync(process, data, IPCInvokeTask_getString.class, new IPCInvokeCallback<String>() {

                            @Override
                            public void onCallback(final String data) {
                                Log.i(TAG, "result : %s", data);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        msgPanelTv.setText(data);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        processEt = (EditText) findViewById(R.id.remoteProcessNameEt);
    }

    private static class IPCInvokeTask_getInt implements IPCSyncInvokeTask<TestType, Integer> {

        @Override
        public Integer invoke(TestType data) {
            return (data.key + data.value + System.currentTimeMillis()).hashCode();
        }
    }

    @Singleton
    private static class IPCInvokeTask_getString implements IPCAsyncInvokeTask<TestType, String> {

        int count;

        @Override
        public void invoke(TestType data, IPCInvokeCallback<String> callback) {
            callback.onCallback(data.key + ":" + data.value + ", count : " + (count++));
        }
    }
}
