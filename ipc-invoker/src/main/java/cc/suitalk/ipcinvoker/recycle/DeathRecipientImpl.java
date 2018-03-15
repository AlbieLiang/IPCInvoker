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

package cc.suitalk.ipcinvoker.recycle;

import android.os.IBinder;

import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/3/15.
 */

public class DeathRecipientImpl implements IBinder.DeathRecipient {

    private static final String TAG = "IPC.DeathRecipientImpl";

    private String process;

    public DeathRecipientImpl(String process) {
        this.process = process;
    }

    @Override
    public void binderDied() {
        Log.i(TAG, "binderDied(%s)", process);
        if (process == null || process.length() == 0) {
            return;
        }
        ObjectRecycler.recycleAll(process);
    }
}
