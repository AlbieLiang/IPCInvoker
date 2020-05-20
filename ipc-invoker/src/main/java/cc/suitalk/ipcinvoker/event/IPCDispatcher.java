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

package cc.suitalk.ipcinvoker.event;

import android.os.Bundle;

import cc.suitalk.ipcinvoker.inner.InnerIPCEventBus;

/**
 * Created by albieliang on 2017/7/20.
 */

public abstract class IPCDispatcher<T> {

    protected String genKey(T data) {
        return IPCObservable.genKey(getClass(), data.getClass());
    }
    
    public final void dispatch(T data) {
        if (data == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(IPCObservable.INNER_KEY_DATA, new WrapperParcelable(data));
        InnerIPCEventBus.getImpl().dispatch(genKey(data), bundle);
    }
}
