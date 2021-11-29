package com.hieubui00it.aesencryption.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LiveData<D> {
    protected D value;

    protected final List<Observer<D>> listObservers = new ArrayList<>();

    public LiveData() {
        this.value = null;
    }

    public LiveData(D value) {
        this.value = value;
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

    @Nullable
    public D getValue() {
        return value;
    }
}
