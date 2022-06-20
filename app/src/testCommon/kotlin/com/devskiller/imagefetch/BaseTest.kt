package com.devskiller.imagefetch

import android.os.Build
import android.os.Looper
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import org.junit.runner.RunWith
import org.powermock.api.support.membermodification.MemberMatcher.field
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.KITKAT])
abstract class BaseTest {

    protected fun getPicasso(activity: MainActivity): Picasso = (field(MainActivity::class.java, "picasso").get(activity) as Picasso)

    protected fun getPicassoOkHttpClient(activity: MainActivity): OkHttpClient {
        val picasso = getPicasso(activity)
        val dispatcher = field(Picasso::class.java, "dispatcher").get(picasso)
        val downloader = field(Class.forName("com.squareup.picasso.Dispatcher"), "downloader").get(dispatcher)
        return (field(Class.forName("com.squareup.picasso.OkHttp3Downloader"), "client").get(downloader) as OkHttpClient)
    }

    protected fun getPicassoDispatcher(activity: MainActivity): Any = field(Picasso::class.java, "dispatcher").get(getPicasso(activity))!!

    protected fun getPicassoDispatcherThreadLooper(activity: MainActivity): Looper {
        val dispatcher = getPicassoDispatcher(activity)
        val dispatcherThread = (field(Class.forName("com.squareup.picasso.Dispatcher"), "dispatcherThread").get(dispatcher) as Thread)
        return (field(Class.forName("com.squareup.picasso.Dispatcher\$DispatcherThread"), "mLooper").get(dispatcherThread) as Looper)
    }
}
