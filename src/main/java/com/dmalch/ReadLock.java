package com.dmalch;

public interface ReadLock {
    void acquireRead();

    void releaseRead();
}
