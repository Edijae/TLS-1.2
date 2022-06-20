package com.devskiller.imagefetch

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Looper.getMainLooper
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.mock.ClasspathResources.resource
import okhttp3.mock.MockInterceptor
import okhttp3.mock.eq
import okhttp3.mock.rule
import okhttp3.mock.url
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.powermock.api.support.membermodification.MemberMatcher
import org.robolectric.Robolectric.buildActivity
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowLog
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PicassoVerifyTest : BaseTest() {

    companion object {

        private const val ASSET_NAME = "osi_keyhole_300X300_90ppi_0.png"
        private const val ASSET_PATH = "https://opensource.org/files/$ASSET_NAME"
    }

    private var activityController: ActivityController<MainActivity>? = null

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        activityController = buildActivity(MainActivity::class.java).create()
    }

    @After
    fun tearDown() {
        activityController = null
    }

    @Test
    fun testHandshakeReceived() {
        val countDownLatch = CountDownLatch(1)

        val activity = activityController!!.start().get()

        addPicassoOkHttpClientInterceptor(activity, MockInterceptor().apply {

            rule(url eq ASSET_PATH) {
                respond(resource(ASSET_NAME))
            }
        })

        val target = object : Target {

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) = Unit

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) = fail()

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) = countDownLatch.countDown()
        }
        getPicasso(activity).load(ASSET_PATH)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(target)

        val picassoBatchDelay = getPicassoDispatcherBatchDelay(activity).toLong()
        countDownLatch.await(picassoBatchDelay, TimeUnit.MILLISECONDS)
        shadowOf(getPicassoDispatcherThreadLooper(activity)).runOneTask()
        shadowOf(getMainLooper()).runOneTask()

        assertTrue(countDownLatch.await(picassoBatchDelay, TimeUnit.MILLISECONDS))
    }

    private fun getPicassoDispatcherBatchDelay(activity: MainActivity): Int {
        val dispatcher = getPicassoDispatcher(activity)
        return (MemberMatcher.field(Class.forName("com.squareup.picasso.Dispatcher"), "BATCH_DELAY").get(dispatcher) as Int)
    }

    @Suppress("UNCHECKED_CAST")
    private fun addPicassoOkHttpClientInterceptor(activity: MainActivity, interceptor: Interceptor) {
        val okHttpClient = getPicassoOkHttpClient(activity)
        val interceptorsField = MemberMatcher.field(OkHttpClient::class.java, "interceptors")
        val interceptors = (interceptorsField.get(okHttpClient) as List<Interceptor>)
        interceptorsField.set(okHttpClient, interceptors.plus(interceptor))
    }
}
