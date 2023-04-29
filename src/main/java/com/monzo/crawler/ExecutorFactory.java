package com.monzo.crawler;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class ExecutorFactory {

    private final int threads;

    public ExecutorFactory(int threads) {
        this.threads = threads;
    }

    public ExecutorService getExecutor(){
        return newFixedThreadPool(threads);
    }

}
