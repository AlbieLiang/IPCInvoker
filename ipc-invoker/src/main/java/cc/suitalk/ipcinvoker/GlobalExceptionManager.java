package cc.suitalk.ipcinvoker;

import java.util.HashSet;
import java.util.Set;

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.exception.OnExceptionObserver;

/**
 * author:
 * time: 2020/5/28 15:57
 */
public class GlobalExceptionManager {
    private static final Set<OnExceptionObserver> observers = new HashSet<>();

    synchronized static boolean registerObserver(@NonNull OnExceptionObserver observer) {
        if (observer == null) {
            return false;
        }
        return observers.add(observer);
    }

    synchronized static boolean unregisterObserver(@NonNull OnExceptionObserver observer) {
        if (observer == null) {
            return false;
        }
        return observers.remove(observer);
    }

    synchronized static void dispatchException(@NonNull Exception e) {
        for (OnExceptionObserver observer : observers) {
            observer.onExceptionOccur(e);
        }
    }

}
