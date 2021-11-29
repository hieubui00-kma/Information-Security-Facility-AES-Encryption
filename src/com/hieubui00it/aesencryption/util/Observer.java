package com.hieubui00it.aesencryption.util;

import org.jetbrains.annotations.Nullable;

/**
 * A simple callback that can receive from {@link LiveData}.
 *
 * @param <D> The type of the parameter
 *
 * @see LiveData LiveData - for a usage description.
 */
public interface Observer<D> {

    /**
     * Called when the data is changed.
     * @param data  The new data
     */
    void onChange(@Nullable D data);
}
