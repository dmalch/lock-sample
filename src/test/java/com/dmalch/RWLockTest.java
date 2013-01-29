package com.dmalch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class RWLockTest {

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
    public void testReadWhenLockedForWrite() throws Exception {
        givenResource();

        whenResourceLockedForWrite();

        thenReadIsPossible();
        thenWriteIsNotPossible();
    }

    @Test(expected = TimeoutException.class)
    public void testWriteWhenLockedForRead() throws Exception {
        givenResource();

        whenResourceLockedForRead();

        thenWriteIsPossible();
        thenReadIsNotPossible();
    }

    private void thenReadIsPossible() throws InterruptedException, ExecutionException, TimeoutException {
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
                final Integer ret = resource;
                rwLock.releaseRead();

                return ret;
            }
        };
    }

    private void whenResourceLockedForWrite() throws InterruptedException {
        lockResourceForWrite();

        sleep(TIMEOUT);
    }

    private void whenResourceLockedForRead() throws InterruptedException {
        lockResourceForRead();

        sleep(TIMEOUT);
    }

    private void thenWriteIsNotPossible() throws ExecutionException, InterruptedException {
        writeLockedValue(NEW_VALUE);

        sleep(TIMEOUT);

        assertThat(resource, equalTo(INITIAL_VALUE));
    }

    private void thenReadIsNotPossible() throws ExecutionException, InterruptedException, TimeoutException {
        final Future<Integer> integerFuture = readLockedValue();

        assertThat(integerFuture.get(TIMEOUT, MILLISECONDS), not(equalTo(INITIAL_VALUE)));
    }

    private void thenWriteIsPossible() throws InterruptedException, ExecutionException {
        writeLockedValue(NEW_VALUE);

        sleep(TIMEOUT);

        assertThat(resource, equalTo(NEW_VALUE));
    }

    private void lockResourceForWrite() {
        executorService.submit(acquireLockForWrite());
    }

    private void lockResourceForRead() {
        executorService.submit(acquireLockForRead());
    }

    private void writeLockedValue(final int newValue) throws InterruptedException, ExecutionException {
        concurrentExecutorService.submit(writeValue(newValue));
        new Thread(writeValue(newValue)).start();
    }

    private Runnable writeValue(final int newValue) {
        return new Runnable() {
            @Override
            public void run() {
                rwLock.acquireWrite();
                resource = newValue;
                rwLock.releaseWrite();
            }
        };
    }

    private Runnable acquireLockForWrite() {
        return new Runnable() {
            @Override
            public void run() {
                rwLock.acquireWrite();
            }
        };
    }

    private Runnable acquireLockForRead() {
        return new Runnable() {
            @Override
            public void run() {
                rwLock.acquireRead();
            }
        };
    }

    private void givenResource() {
        resource = INITIAL_VALUE;
    }
}
