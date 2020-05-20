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

package cc.suitalk.ipcinvoker.sample.event;

import android.annotation.SuppressLint;
import android.os.Bundle;

import cc.suitalk.ipcinvoker.event.IPCDispatcher;
import cc.suitalk.ipcinvoker.event.IPCObservable;
import cc.suitalk.ipcinvoker.event.IPCObserver;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/1/17.
 */

public class IPCEventSample {

    public static final String TAG = "IPCEventSample";

    private final class IPCEventDispatcher extends IPCDispatcher<Bundle> {
    }

    public void aVoid() {
        final IPCObserver<Bundle> observer = new IPCObserver<Bundle>() {
            @Override
            public void onCallback(final Bundle data) {
                @SuppressLint("DefaultLocale")
                String log = String.format("onCallback(actions : %d), timestamp : %d",
                        data.getInt("action"), data.getLong("timestamp"));
                Log.i(TAG, log);
            }
        };
        // register Observer to Dispatcher
        IPCObservable<Bundle> observable = new IPCObservable<>("cc.suitalk.ipcinvoker.sample:push", IPCEventDispatcher.class);
        observable.registerIPCObserver(observer);

        // publish event
        IPCEventDispatcher dispatcher = new IPCEventDispatcher();

        Bundle event = new Bundle();
        event.putInt("action", 1);
        event.putLong("timestamp", System.nanoTime());

        dispatcher.dispatch(event);
    }
}
