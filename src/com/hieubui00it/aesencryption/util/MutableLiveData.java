package com.hieubui00it.aesencryption.util;

import org.jetbrains.annotations.Nullable;

/**
 * @author hieubui00.it
 */

public class MutableLiveData<D> extends LiveData<D> {

    public MutableLiveData() {

    }

    public void postValue(@Nullable D value) {
        this.value = value;
        listObservers.forEach(observer -> observer.onChange(value));
    }
}
