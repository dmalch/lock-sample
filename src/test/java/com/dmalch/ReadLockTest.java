package com.dmalch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReadLockTest {

    public static final int INITIAL_VALUE = 5;
    public static final int NEW_VALUE = 10;
    public static final long TIMEOUT = 100L;

    private Integer resource;

    private ExecutorService executorService;
    private ReadLock readLock;

    @Before
    public void setUp() throws Exception {
        executorService = newFixedThreadPool(2);
        readLock = new RWLockImpl();
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdown();
    }

    @Test(expected = TimeoutException.class)
    public void testReadWhenLocked() throws Exception {
        givenResource();

        whenResourceLocked();

        thenReadIsNotPossible();
    }

    @Test
    public void testReadWhenUnlocked() throws Exception {
        givenResource();

        thenReadIsPossible();
    }

    @Test
    public void testReadWhenLockedAndUnlocked() throws Exception {
        givenResource();

        whenResourceLockedAndUnlocked();

        thenReadIsPossible();
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

    private void thenReadIsNotPossible() throws ExecutionException, InterruptedException, TimeoutException {
        final Future<Integer> integerFuture = readLockedValue();

        assertThat(integerFuture.get(TIMEOUT, MILLISECONDS), not(equalTo(INITIAL_VALUE)));
    }

    private void thenReadIsPossible() throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Integer> integerFuture = readLockedValue();

        assertThat(integerFuture.get(TIMEOUT, MILLISECONDS), equalTo(INITIAL_VALUE));
    }

    private void lockResource() {
        executorService.submit(acquireLock());
    }

    private void unlockResource() {
        executorService.submit(releaseLock());
    }

    private Future<Integer> readLockedValue() throws InterruptedException, ExecutionException {
        return executorService.submit(readValue());
    }

    private Callable<Integer> readValue() {
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                readLock.acquireRead();
                final Integer ret = resource;
                readLock.releaseRead();

                return ret;
            }
        };
    }

    private Runnable acquireLock() {
        return new Runnable() {
            @Override
            public void run() {
                readLock.acquireRead();
            }
        };
    }

    private Runnable releaseLock() {
        return new Runnable() {
            @Override
            public void run() {
                readLock.releaseRead();
            }
        };
    }

    private void givenResource() {
        resource = INITIAL_VALUE;
    }
}
