package com.dmalch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static java.lang.Thread.*;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class WriteLockTest {

    public static final int INITIAL_VALUE = 5;
    public static final int NEW_VALUE = 10;
    public static final long TIMEOUT = 100L;

    private Integer resource;

    private ExecutorService executorService;
    private WriteLock writeLock;

    @Before
    public void setUp() throws Exception {
        executorService = newFixedThreadPool(2);
        writeLock = new RWLockImpl();
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void testWriteWhenLocked() throws Exception {
        givenResource();

        whenResourceLocked();

        thenWriteIsNotPossible();
    }

    @Test
    public void testWriteWhenUnlocked() throws Exception {
        givenResource();

        thenWriteIsPossible();
    }

    @Test
    public void testWriteWhenLockedAndUnlocked() throws Exception {
        givenResource();

        whenResourceLockedAndUnlocked();

        thenWriteIsPossible();
    }

    private void whenResourceLocked() throws InterruptedException {
        lockResource();

        sleep(TIMEOUT);
    }

    private void whenResourceLockedAndUnlocked() throws InterruptedException {
        lockResource();

        sleep(TIMEOUT);

        unlockResource();

        sleep(TIMEOUT);
    }

    private void thenWriteIsNotPossible() throws ExecutionException, InterruptedException {
        writeLockedValue(NEW_VALUE);

        sleep(TIMEOUT);

        assertThat(resource, equalTo(INITIAL_VALUE));
    }

    private void thenWriteIsPossible() throws InterruptedException, ExecutionException {
        writeLockedValue(NEW_VALUE);

        sleep(TIMEOUT);

        assertThat(resource, equalTo(NEW_VALUE));
    }

    private void lockResource() {
        executorService.submit(acquireLock());
    }

    private void unlockResource() {
        executorService.submit(releaseLock());
    }


    private void writeLockedValue(final int newValue) throws InterruptedException, ExecutionException {
        executorService.submit(writeValue(newValue));
    }

    private Runnable writeValue(final int newValue) {
        return new Runnable() {
            @Override
            public void run() {
                writeLock.acquireWrite();
                resource = newValue;
                writeLock.releaseWrite();
            }
        };
    }

    private Runnable acquireLock() {
        return new Runnable() {
            @Override
            public void run() {
                writeLock.acquireWrite();
            }
        };
    }

    private Runnable releaseLock() {
        return new Runnable() {
            @Override
            public void run() {
                writeLock.releaseWrite();
            }
        };
    }

    private void givenResource() {
        resource = INITIAL_VALUE;
    }
}
