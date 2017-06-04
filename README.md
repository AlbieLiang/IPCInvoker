# IPCInvoker

在Android开发过程中，经常需要写一些跨进程的逻辑，一般情况下我们都是通过AIDL接口来调用的，写AIDL接口并不是一件容易的事情，需要写Service，定义特定的业务接口，调用时需要绑定Service等。

IPCInvoker就是一个用来简化跨进程调用的组件，IPCInvoker底层也是通过AIDL实现的，只是把接口分装得更加容易使用。


## 引入组件库
```gradle

dependencies {
    compile 'cc.suitalk.android:ipcinvoker:1.0.0'
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
        android:theme="@style/AppTheme" />
    <service
        android:name=".service.SupportProcessIPCService"
        android:process=":support"
        android:exported="false"/>
    <service
        android:name=".service.MainProcessIPCService"
        android:exported="false"/>
    <service
        android:name=".service.PushProcessIPCService"
        android:process=":push"
        android:exported="false"/>
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
        IPCInvoker.setup(this, new IPCInvokerInitDelegate() {
            @Override
            public void onAttachServiceInfo(IPCInvokerInitializer initializer) {
                initializer.addIPCService(MainProcessIPCService.PROCESS_NAME, MainProcessIPCService.class);
                initializer.addIPCService(SupportProcessIPCService.PROCESS_NAME, SupportProcessIPCService.class);
                initializer.addIPCService(PushProcessIPCService.PROCESS_NAME, PushProcessIPCService.class);
            }
        });
    }
}
```
### 在项目代码中通过IPCInvoker调用跨进程逻辑

#### 通过IPCInvoker同步调用跨进程逻辑

```java

public class IPCInvokeSample_InvokeByType {

    public static IPCData invokeIPCLogic(String id, int debugType, int pkgVersion) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("type", debugType);
        bundle.putInt("version", 0);
        return IPCInvoker.invokeSync(PushProcessIPCService.PROCESS_NAME, bundle, IPCRemoteInvoke_PrintSomething.class);
    }

    private static class IPCRemoteInvoke_PrintSomething implements IPCRemoteSyncInvoke<Bundle, IPCData> {

        @Override
        public IPCData invoke(Bundle data) {
            IPCData result = new IPCData();
            result.result = data.getString("id") + ":" + data.getInt("type") + ":" + data.getInt("version");
            return result;
        }
    }
}


```


#### 通过IPCInvoker异步调用跨进程逻辑

```java

public class IPCInvokeSample_InvokeByType {

    private static final String TAG = "IPCInvokerSample.IPCInvokeSample_InvokeByType";

    public static void invokeIPCLogic(String id, int debugType, int pkgVersion, final IPCRemoteInvokeCallback<IPCData> callback) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("type", debugType);
        bundle.putInt("version", 0);
        IPCInvoker.invokeASync(PushProcessIPCService.PROCESS_NAME, bundle, IPCRemoteInvoke_PrintSomething.class, new IPCRemoteInvokeCallback<IPCData>() {
            @Override
            public void onCallback(IPCData data) {
                Log.i(TAG, "onCallback : %s", data.result);
                if (callback != null) {
                    callback.onCallback(data);
                }
            }
        });
    }

    private static class IPCRemoteInvoke_PrintSomething implements IPCRemoteASyncInvoke<Bundle, IPCData> {

        @Override
        public void invoke(Bundle data, IPCRemoteInvokeCallback<IPCData> callback) {
            IPCData result = new IPCData();
            result.result = data.getString("id") + ":" + data.getInt("type") + ":" + data.getInt("version");
            callback.onCallback(result);
        }
    }
}


```

上述示例中IPCData是一个可序列化的Parcelable，IPCInvoker支持的跨进程调用的数据必须是可序列化的Parcelable（默认支持Bundle）


```java
public class IPCData implements Parcelable {

    public String result;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(result);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IPCData> CREATOR = new Creator<IPCData>() {
        @Override
        public IPCData createFromParcel(Parcel in) {
            IPCData o = new IPCData();
            o.result = in.readString();
            return o;
        }

        @Override
        public IPCData[] newArray(int size) {
            return new IPCData[size];
        }
    };
}
```

## 注意

由于跨进程调用逻辑是通过反射的方式实现的，所以跨进程逻辑的类不能是非静态内部类，这样能有效的减少写程序时因为误引用变量而出现的bug。

更多使用，请移步[Sample工程](https://github.com/AlbieLiang/IPCInvoker/tree/master/ipc-invoker-sample)
