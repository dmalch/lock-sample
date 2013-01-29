package com.dmalch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class WriteNestedLockTest {

    public static final int INITIAL_VALUE = 5;
    public static final int NEW_VALUE = 10;
    public static final long TIMEOUT = 100L;

    private Integer resource;

    private ExecutorService executorService;
    private ExecutorService concurrentExecutorService;
    private RWLock rwLock;

    @Before
    public void setUp() throws Exception {
        executorService = newSingleThreadExecutor();
        concurrentExecutorService = newSingleThreadExecutor();
        rwLock = new RWLockImpl();
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

    @Test
    public void testReadWhenNestedLock() throws Exception {
        givenResource();

        thenReadInNestedLocksIsPossible();
    }

    private void thenReadInNestedLocksIsPossible() throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Integer> integerFuture = readLockedValue();

        assertThat(integerFuture.get(TIMEOUT, MILLISECONDS), equalTo(INITIAL_VALUE));
    }

    private Future<Integer> readLockedValue() throws InterruptedException, ExecutionException {
        return executorService.submit(readValue());
    }

    private Callable<Integer> readValue() {
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                rwLock.acquireRead();
                rwLock.acquireRead();
                final Integer ret = resource;
                rwLock.releaseRead();
                rwLock.releaseRead();

                return ret;
            }
        };
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
                rwLock.acquireWrite();
                rwLock.acquireWrite();
                resource = newValue;
                rwLock.releaseWrite();
                rwLock.releaseWrite();
            }
        };
    }


    private Runnable acquireLock() {
        return new Runnable() {
            @Override
            public void run() {
                rwLock.acquireWrite();
            }
        };
    }

    private void givenResource() {
        resource = INITIAL_VALUE;
    }
}
