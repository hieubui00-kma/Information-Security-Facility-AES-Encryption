package com.hieubui00it.aesencryption.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hieubui00.it
 */

public class LiveData<D> {
    protected D value;

    protected final List<Observer<D>> listObservers = new ArrayList<>();

    public LiveData() {
        this.value = null;
    }

    public void observer(Observer<D> observer) {
        if (listObservers.contains(observer)) {
            observer.onChange(value);
            return;
        }

        listObservers.add(observer);
        if (value != null) {
            observer.onChange(value);
        }
    }
}
