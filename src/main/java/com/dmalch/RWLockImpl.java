package com.dmalch;

public class RWLockImpl implements RWLock {
    private boolean lockedForWrite = false;
    private boolean lockedForRead = false;

    @Override
    public void acquireWrite() {
        if (!lockedForWrite) {
            lockedForWrite = true;
        } else {
            while (lockedForWrite) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void releaseWrite() {
        lockedForWrite = false;
    }

    @Override
    public void acquireRead() {
        if (!lockedForRead) {
            lockedForRead = true;
        } else {
            while (lockedForRead) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void releaseRead() {
        lockedForRead = false;
    }
}
