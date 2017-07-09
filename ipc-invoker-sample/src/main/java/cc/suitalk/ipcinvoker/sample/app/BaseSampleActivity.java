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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCRemoteInvokeCallback;
import cc.suitalk.ipcinvoker.sample.R;
import cc.suitalk.ipcinvoker.sample.app.ipctask.IPCInvokeTask_GetValue;
import cc.suitalk.ipcinvoker.sample.app.ipctask.IPCInvokeTask_PutValue;
import cc.suitalk.ipcinvoker.sample.app.model.ThreadPool;

/**
 * Created by albieliang on 2017/6/3.
 */

public class BaseSampleActivity extends AppCompatActivity {

    private TextView myPidTv;

    private EditText remoteProcessNameEt;

    private Button getValueBtn;
    private Button putValueBtn;

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

        remoteProcessNameEt = (EditText) findViewById(R.id.remoteProcessNameEt);

        getValueBtn = (Button) findViewById(R.id.getValueBtn);
        putValueBtn = (Button) findViewById(R.id.putValueBtn);

        getKeyEt = (EditText) findViewById(R.id.getByKeyEt);
        getResultTv = (TextView) findViewById(R.id.getResultTv);

        putKeyEt = (EditText) findViewById(R.id.putKeyEt);
        putValueEt = (EditText) findViewById(R.id.putValueEt);
        putResultTv = (TextView) findViewById(R.id.putResultTv);


        myPidTv.setText("" + android.os.Process.myPid());

        getValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        String remoteProcessName = remoteProcessNameEt.getText().toString();
                        String key = getKeyEt.getText().toString();
                        Bundle bundle = new Bundle();
                        bundle.putString(IPCInvokeTask_GetValue.KEY, key);

                        final Bundle result = IPCInvoker.invokeSync(remoteProcessName, bundle, IPCInvokeTask_GetValue.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getResultTv.setText(result.getString(IPCInvokeTask_GetValue.VALUE));
                            }
                        });
                    }
                });
            }
        });

        putValueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remoteProcessName = remoteProcessNameEt.getText().toString();
                String key = putKeyEt.getText().toString();
                String value = putValueEt.getText().toString();

                IPCInvokeTask_PutValue putValue = new IPCInvokeTask_PutValue();
                putValue.key = key;
                putValue.value = value;

                IPCInvoker.invokeAsync(remoteProcessName, putValue, IPCInvokeTask_PutValue.class, new IPCRemoteInvokeCallback<Bundle>() {
                    @Override
                    public void onCallback(final Bundle data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                putResultTv.setText(data.getString(IPCInvokeTask_PutValue.RESULT));
                            }
                        });
                    }
                });
            }
        });
    }

    protected int getLayoutId() {
        return R.layout.base_sample_activity;
    }
}
