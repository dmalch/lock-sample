package com.dmalch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class WriteNestedLockTest {

    private static final transient Logger logger = LoggerFactory.getLogger(WriteNestedLockTest.class);

    public static final int INITIAL_VALUE = 5;
    public static final int NEW_VALUE = 10;
    public static final long TIMEOUT = 100L;

    private Integer resource;

    private ExecutorService executorService;
    private ExecutorService concurrentExecutorService;
    private WriteLock writeLock;

    @Before
    public void setUp() throws Exception {
        executorService = newSingleThreadExecutor();
        concurrentExecutorService = newSingleThreadExecutor();
        writeLock = new RWLockImpl();
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void testWriteWhenNestedLock() throws Exception {
        givenResource();

        thenWriteInNestedLocksIsPossible();
    }

    private void thenWriteInNestedLocksIsPossible() throws InterruptedException {
        writeInNestedLocksValue(NEW_VALUE);

        sleep(TIMEOUT);

        assertThat(resource, equalTo(NEW_VALUE));
    }

    private void writeInNestedLocksValue(final int newValue) {
        concurrentExecutorService.submit(writeValueInNestedLock(newValue));

    }

    private Runnable writeValueInNestedLock(final int newValue) {
        return new Runnable() {
            @Override
            public void run() {
                writeLock.acquireWrite();
                writeLock.acquireWrite();
                resource = newValue;
                writeLock.releaseWrite();
                writeLock.releaseWrite();
            }
        };
    }

    private void whenResourceLocked() throws InterruptedException {
        lockResource();

        sleep(TIMEOUT);
    }

    private void lockResource() {
        executorService.submit(acquireLock());
    }


    private Runnable acquireLock() {
        return new Runnable() {
            @Override
            public void run() {
                writeLock.acquireWrite();
            }
        };
    }

    private void givenResource() {
        resource = INITIAL_VALUE;
    }
}
