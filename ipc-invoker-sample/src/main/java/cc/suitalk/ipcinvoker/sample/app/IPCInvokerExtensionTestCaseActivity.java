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

import cc.suitalk.ipcinvoker.IPCRemoteInvokeCallback;
import cc.suitalk.ipcinvoker.sample.R;
import cc.suitalk.ipcinvoker.sample.app.model.ThreadPool;
import cc.suitalk.ipcinvoker.sample.extension.IPCTask$AG;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/11/18.
 */

public class IPCInvokerExtensionTestCaseActivity extends AppCompatActivity {

    private static final String TAG = "IPCInvokerSample.IPCInvokerExtensionTestCaseActivity";

    private TextView myPidTv;

    private EditText getKeyEt;
    private TextView getResultTv;

    private EditText putKeyEt;
    private EditText putValueEt;
    private TextView putResultTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        myPidTv = (TextView) findViewById(R.id.myPidTv);

        getKeyEt = (EditText) findViewById(R.id.getByKeyEt);
        getResultTv = (TextView) findViewById(R.id.getResultTv);

        putKeyEt = (EditText) findViewById(R.id.putKeyEt);
        putValueEt = (EditText) findViewById(R.id.putValueEt);
        putResultTv = (TextView) findViewById(R.id.putResultTv);

        myPidTv.setText("" + android.os.Process.myPid());

        findViewById(R.id.getValueBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        String key = getKeyEt.getText().toString();
                        final String value = IPCTask$AG.getValue(key);
                        Log.i(TAG, "getValue(%s)", value);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getResultTv.setText(value);
                            }
                        });
                    }
                });
            }
        });
        findViewById(R.id.putValueBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = putKeyEt.getText().toString();
                String value = putValueEt.getText().toString();
                IPCTask$AG.setValue(key, value, new IPCRemoteInvokeCallback<Bundle>() {
                    @Override
                    public void onCallback(final Bundle data) {
                        Log.i(TAG, "onCallback(%s)", data);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                putResultTv.setText(data.getString("result"));
                            }
                        });
                    }
                });
            }
        });
    }

    protected int getLayoutId() {
        return R.layout.ipc_invoker_extension_test_case_activity;
    }
}
