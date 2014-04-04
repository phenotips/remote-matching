package org.phenotips.remote.adapters;

/**
 * Since the original design pattern was not possible to implement, this interface is used to uphold consistency of
 * implementation.
 */
public interface WrapperInterface<T>
{
    T wrap();
}
