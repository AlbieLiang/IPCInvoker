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

package cc.suitalk.ipcinvoker.activate;

import cc.suitalk.ipcinvoker.extension.IPCDataTransfer;
import cc.suitalk.ipcinvoker.extension.ParcelableTransfer;
import cc.suitalk.ipcinvoker.extension.XParcelableTransfer;

/**
 * Created by albieliang on 2017/7/7.
 */

public abstract class DefaultInitDelegate implements IPCInvokerInitDelegate {

    @Override
    public void onInitialize(IPCInvokerInitializer initializer) {
    }

    @Override
    public void onAddTypeTransfer(TypeTransferInitializer initializer) {
        initializer.addTypeTransfer(new ParcelableTransfer());
        initializer.addTypeTransfer(new IPCDataTransfer());
        initializer.addTypeTransfer(new XParcelableTransfer());
    }
}
