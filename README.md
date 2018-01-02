# IPCInvoker

[![license](http://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat)](https://github.com/AlbieLiang/IPCInvoker/blob/master/LICENSE)
[![Release Version](https://img.shields.io/badge/release-1.1.7-red.svg)](https://github.com/AlbieLiang/IPCInvoker/releases)
[![wiki](https://img.shields.io/badge/wiki-1.1.7-red.svg)](https://github.com/AlbieLiang/IPCInvoker/wiki) 
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/AlbieLiang/IPCInvoker/pulls)



在Android开发过程中，经常需要写一些跨进程的逻辑，一般情况下我们都是通过AIDL接口来调用的，写AIDL接口并不是一件容易的事情，需要写Service，定义特定的业务接口，调用时需要绑定Service等。

IPCInvoker就是一个用来简化跨进程调用的组件，IPCInvoker底层也是通过AIDL实现的，只是把接口分装得更加容易使用。


## 引入组件库

IPCInvoker组件库已经提交到jcenter上了，可以直接dependencies中配置引用

```gradle
dependencies {
    compile 'cc.suitalk.android:ipc-invoker:1.1.7'
}
```


## 在项目中使用

### 为每个需要支持IPCInvoker的进程创建一个Service


这里以PushProcessIPCService为示例，代码如下：


```java

public class PushProcessIPCService extends BaseIPCService {

    public static final String PROCESS_NAME = "cc.suitalk.ipcinvoker.sample:push";

    @Override
    public String getProcessName() {
        return PROCESS_NAME;
    }
}

```
在manifest.xml中配置service

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="cc.suitalk.ipcinvoker.sample">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <service
            android:name=".service.PushProcessIPCService"
            android:process=":push"
            android:exported="false"/>
    </application>
</manifest>

```

### 在项目的Application中setup IPCInvoker

这里需要在你的项目所有需要支持跨进程调用的进程中调用`IPCInvoker.setup(Application, IPCInvokerInitDelegate)`方法，并在传入的IPCInvokerInitDelegate接口实现中将该进程需要支持访问的其它进程相应的Service的class添加到IPCInvoker当中，示例如下：

```java

public class IPCInvokerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize IPCInvoker
        IPCInvokerBoot.setup(this, new DefaultInitDelegate() {
            @Override
            public void onAttachServiceInfo(IPCInvokerInitializer initializer) {
//              initializer.addIPCService(MainProcessIPCService.PROCESS_NAME, MainProcessIPCService.class);
//              initializer.addIPCService(SupportProcessIPCService.PROCESS_NAME, SupportProcessIPCService.class);
                initializer.addIPCService(PushProcessIPCService.PROCESS_NAME, PushProcessIPCService.class);
            }

            @Override
            public void onAddTypeTransfer(TypeTransferInitializer initializer) {
                super.onAddTypeTransfer(initializer);
                initializer.addTypeTransfer(new TestTypeTransfer());
            }
        });
    }
}
```
### 在项目代码中通过IPCInvoker调用跨进程逻辑

#### 通过IPCInvoker同步调用跨进程逻辑

```java

public class IPCInvokeSample_InvokeByType {

    private static final String TAG = "IPCInvokerSample.IPCInvokeSample_InvokeByType";

    public static void invokeSync() {
        Bundle bundle = new Bundle();
        bundle.putString("name", "AlbieLiang");
        bundle.putInt("pid", android.os.Process.myPid());
        IPCString result = IPCInvoker.invokeSync(PushProcessIPCService.PROCESS_NAME, bundle, IPCRemoteInvoke_BuildString.class);
        Log.i(TAG, "invoke result : %s", result);
    }

    private static class IPCRemoteInvoke_BuildString implements IPCRemoteSyncInvoke<Bundle, IPCString> {

        @Override
        public IPCString invoke(Bundle data) {
            String msg = String.format("name:%s|fromPid:%s|curPid:%s", data.getString("name"), data.getInt("pid"), android.os.Process.myPid());
            Log.i(TAG, "build String : %s", msg);
            return new IPCString(msg);
        }
    }
}

```


#### 通过IPCInvoker异步调用跨进程逻辑

```java

public class IPCInvokeSample_InvokeByType {

    private static final String TAG = "IPCInvokerSample.IPCInvokeSample_InvokeByType";

    public static void invokeAsync() {
        Bundle bundle = new Bundle();
        bundle.putString("name", "AlbieLiang");
        bundle.putInt("pid", android.os.Process.myPid());
        IPCInvoker.invokeAsync(PushProcessIPCService.PROCESS_NAME, bundle, IPCRemoteInvoke_PrintSomething.class, new IPCRemoteInvokeCallback<IPCString>() {
            @Override
            public void onCallback(IPCString data) {
                Log.i(TAG, "onCallback : %s", data.value);
            }
        });
    }

    private static class IPCRemoteInvoke_PrintSomething implements IPCRemoteAsyncInvoke<Bundle, IPCString> {

        @Override
        public void invoke(Bundle data, IPCRemoteInvokeCallback<IPCString> callback) {
            String result = String.format("name:%s|fromPid:%s|curPid:%s", data.getString("name"), data.getInt("pid"), android.os.Process.myPid());
            callback.onCallback(new IPCString(result));
        }
    }
}
```

上述示例中IPCString是IPCInvoker里面提供的String的Parcelable的包装类，IPCInvoker支持的跨进程调用的数据必须是可序列化的Parcelable（默认支持Bundle）。

IPCInvoker支持自定义实现的Parcelable类作为跨进程调用的数据结构，同时也支持非Parcelable的扩展类型数据，详细请参考[XIPCInvoker扩展系列接口](https://github.com/AlbieLiang/IPCInvoker/wiki/XIPCInvoker%E6%89%A9%E5%B1%95%E7%B3%BB%E5%88%97%E6%8E%A5%E5%8F%A3)


__此外，IPCInvoker还支持跨进程事件监听和分发等丰富的功能，详细使用说明请参考[wiki](https://github.com/AlbieLiang/IPCInvoker/wiki) ，更多使用示例请移步[Sample工程](https://github.com/AlbieLiang/IPCInvoker/tree/master/ipc-invoker-sample)__


