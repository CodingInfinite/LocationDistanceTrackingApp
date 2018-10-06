package com.spartons.distancetrackingapp.util

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

class AppRxSchedulers(
        var disk: Scheduler = Schedulers.single(),
        var network: Scheduler = Schedulers.io(),
        var newThread: Scheduler = Schedulers.newThread(),
        var computation: Scheduler = Schedulers.computation(),
        var mainThread: Scheduler = AndroidSchedulers.mainThread()
) {
    fun threadPoolSchedulers(): Scheduler {
        val threadCount = Runtime.getRuntime().availableProcessors()
        val threadPoolExecutorService = Executors.newFixedThreadPool(threadCount)
        return Schedulers.from(threadPoolExecutorService)
    }
}