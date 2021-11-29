package com.hieubui00it.aesencryption.util;

import org.jetbrains.annotations.Nullable;

public class MutableLiveData<D> extends LiveData<D> {

    public MutableLiveData() {

    }

    public MutableLiveData(D value) {
        super(value);
    }

    public void postValue(@Nullable D value) {
        this.value = value;
        listObservers.forEach(observer -> observer.onChange(value));
    }
}
