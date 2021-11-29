package com.hieubui00it.aesencryption.util;

import org.jetbrains.annotations.Nullable;

/**
 * @author hieubui00.it
 */

public interface Observer<D> {

    /**
     * Called when the data is changed.
     * @param data The new data
     */
    void onChange(@Nullable D data);
}
