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

package cc.suitalk.ipcinvoker.model;

import android.support.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by albieliang on 2017/7/9.
 */

public class InvokeFuture<T> implements Future<T>, OnResultObserver<T> {

    final Object lock = new Object();

    volatile boolean hasResult;
    volatile T result;
    OnResultObserver observer;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return hasResult;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (!hasResult) {
            synchronized (lock) {
                if (!hasResult) {
                    lock.wait();
                }
            }
        }
        return result;
    }

    @Override
    public T get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!hasResult) {
            synchronized (lock) {
                if (!hasResult) {
                    lock.wait(unit.toMillis(timeout));
                }
            }
        }
        return result;
    }

    @Override
    public void onResult(T result) {
        this.result = result;
        hasResult = true;
        synchronized (lock) {
            lock.notifyAll();
        }
        if (observer != null) {
            onResult(result);
        }
    }

    public void setOnResultObserver(OnResultObserver<T> observer) {
        this.observer = observer;
    }

}