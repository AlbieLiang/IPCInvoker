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

/**
 * Created by albieliang on 2017/6/18.
 */

public abstract class IPCDispatcher {

    protected String getName() {
        return getClass().getName();
    }

    public final void dispatch(IPCData event) {
        if (event == null) {
            return;
        }
        IPCEventBus.getImpl().dispatch(getName(), event);
    }
}
