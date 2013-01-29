package com.dmalch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.*;

public class RWLockImpl implements RWLock {

    private static final transient Logger logger = LoggerFactory.getLogger(RWLockImpl.class);

    private boolean lockedForWrite = false;
    private boolean lockedForRead = false;
    private Thread ownerThread;

    @Override
    public void acquireWrite() {
        if (!lockedForWrite) {
            logger.info("locking for write");
            lockedForWrite = true;

            ownerThread = currentThread();
        } else if (threadIsOwner()) {
            logger.info("in owner thread continue to work");
        } else {
            logger.info("waiting for lock to be released");
            while (lockedForWrite) {
                try {
                    sleep(100L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private boolean threadIsOwner() {
        return currentThread().equals(ownerThread);
    }

    @Override
    public void releaseWrite() {
        logger.info("unlocking for write");
        lockedForWrite = false;
    }

    @Override
    public void acquireRead() {
        if (!lockedForRead) {
            logger.info("locking for read");
            lockedForRead = true;
        } else {
            while (lockedForRead) {
                try {
                    sleep(100L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void releaseRead() {
        logger.info("unlocking for read");
        lockedForRead = false;
    }
}
