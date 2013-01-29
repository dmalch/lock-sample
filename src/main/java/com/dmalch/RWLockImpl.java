package com.dmalch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.*;

public class RWLockImpl implements RWLock {

    private static final transient Logger logger = LoggerFactory.getLogger(RWLockImpl.class);

    private AtomicBoolean lockedForWrite = new AtomicBoolean(false);
    private AtomicBoolean lockedForRead = new AtomicBoolean(false);
    private Thread ownerThread;

    @Override
    public void acquireWrite() {
        if (lockForWrite()) {
        } else if (threadIsOwner()) {
            logger.info("in owner thread continue to work");
        } else {
            waitForWriteLockToBeReleased();
        }
    }

    private void waitForWriteLockToBeReleased() {
        logger.info("waiting for write lock to be released");
        while (lockedForWrite.get()) {
            try {
                sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean lockForWrite() {
        if (lockedForWrite.compareAndSet(false, true)) {
            logger.info("locking for write");
            ownerThread = currentThread();
            return true;
        }
        return false;
    }

    private boolean threadIsOwner() {
        return currentThread().equals(ownerThread);
    }

    @Override
    public void releaseWrite() {
        logger.info("unlocking for write");
        lockedForWrite.set(false);
    }

    @Override
    public void acquireRead() {
        if (lockForRead()) {
        } else if (threadIsOwner()) {
            logger.info("in owner thread continue to work");
        } else {
            waitForReadLockToBeReleased();
        }
    }

    private void waitForReadLockToBeReleased() {
        logger.info("waiting for read lock to be released");
        while (lockedForRead.get()) {
            try {
                sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean lockForRead() {
        if (lockedForRead.compareAndSet(false, true)) {
            logger.info("locking for read");
            lockedForRead.set(true);

            ownerThread = currentThread();
            return true;
        }
        return false;
    }

    @Override
    public void releaseRead() {
        logger.info("unlocking for read");
        lockedForRead.set(false);
    }
}
