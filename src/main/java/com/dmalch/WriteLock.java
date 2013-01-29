package com.dmalch;

public interface WriteLock {
    void acquireWrite();

    void releaseWrite();
}
