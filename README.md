# IPCInvoker

[![](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/AlbieLiang/IPCInvoker)



在Android开发过程中，经常需要写一些跨进程的逻辑，一般情况下我们都是通过AIDL接口来调用的，写AIDL接口并不是一件容易的事情，需要写Service，定义特定的业务接口，调用时需要绑定Service等。

IPCInvoker就是一个用来简化跨进程调用的组件，IPCInvoker底层也是通过AIDL实现的，只是把接口分装得更加容易使用。


## 引入组件库

IPCInvoker组件库已经提交到jcenter上了，可以直接dependencies中配置引用

```gradle
dependencies {
    compile 'cc.suitalk.android:ipc-invoker:1.1.2'
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
        IPCInvokerBoot.setup(this, new DefaultInitDelegate() {
            @Override
            public void onAttachServiceInfo(IPCInvokerInitializer initializer) {
                initializer.addIPCService(MainProcessIPCService.PROCESS_NAME, MainProcessIPCService.class);
                initializer.addIPCService(SupportProcessIPCService.PROCESS_NAME, SupportProcessIPCService.class);
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

    public static IPCSampleData invokeIPCLogic(String id, int type, int version) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("type", type);
        bundle.putInt("version", version);
        return IPCInvoker.invokeSync(PushProcessIPCService.PROCESS_NAME, bundle, IPCRemoteInvoke_doSomething.class);
    }

    private static class IPCRemoteInvoke_doSomething implements IPCRemoteSyncInvoke<Bundle, IPCSampleData> {

        @Override
        public IPCSampleData invoke(Bundle data) {
            IPCSampleData result = new IPCSampleData();
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

    public static void invokeIPCLogic(String id, int type, int version, final IPCRemoteInvokeCallback<IPCSampleData> callback) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("type", type);
        bundle.putInt("version", version);
        IPCInvoker.invokeAsync(PushProcessIPCService.PROCESS_NAME, bundle, IPCRemoteInvoke_doSomething.class, new IPCRemoteInvokeCallback<IPCString>() {
            @Override
            public void onCallback(IPCString data) {
                Log.i(TAG, "onCallback : %s", data.value);
                if (callback != null) {
                    callback.onCallback(data);
                }
            }
        });
    }

    private static class IPCRemoteInvoke_doSomething implements IPCRemoteAsyncInvoke<Bundle, IPCString> {

        @Override
        public void invoke(Bundle data, IPCRemoteInvokeCallback<IPCString> callback) {
            callback.onCallback(new IPCString(data.getString("id") + ":" + data.getInt("type") + ":" + data.getInt("version")));
        }
    }
}


```

上述示例中IPCSampleData是一个可序列化的Parcelable，IPCString则是IPCInvoker里面提供的String的Parcelable的包装类，IPCInvoker支持的跨进程调用的数据必须是可序列化的Parcelable（默认支持Bundle）


```java
public class IPCSampleData implements Parcelable {

    public String result;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(result);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IPCSampleData> CREATOR = new Creator<IPCSampleData>() {
        @Override
        public IPCSampleData createFromParcel(Parcel in) {
            IPCSampleData o = new IPCSampleData();
            o.result = in.readString();
            return o;
        }

        @Override
        public IPCSampleData[] newArray(int size) {
            return new IPCSampleData[size];
        }
    };
}
```

## 注意

由于跨进程调用逻辑是通过反射的方式实现的，所以跨进程逻辑的类不能是非静态内部类，这样能有效的减少写程序时因为误引用变量而出现的bug。

更多使用，请移步[Sample工程](https://github.com/AlbieLiang/IPCInvoker/tree/master/ipc-invoker-sample)
