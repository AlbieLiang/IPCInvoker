// AIDL_IPCInvokeBridge.aidl
package cc.suitalk.ipcinvoker.aidl;

import android.os.Bundle;

import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeCallback;

// Declare any non-default types here with import statements

interface AIDL_IPCInvokeBridge {
    /**
     * The clazz must the implements of IPCASyncInvokeTask.
     */
    oneway void invokeASync(in Bundle data, String clazz, AIDL_IPCInvokeCallback callback);

    /**
     * The clazz must the implements of IPCSyncInvokeTask.
     */
    Bundle invokeSync(in Bundle data, String clazz);
}
