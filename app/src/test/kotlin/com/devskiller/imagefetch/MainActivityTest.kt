package com.devskiller.imagefetch

import okhttp3.internal.Util
import okhttp3.internal.platform.Platform
import org.junit.Assert.assertFalse
import org.junit.Test
import org.robolectric.Robolectric.buildActivity
import org.robolectric.shadows.ShadowLooper

class MainActivityTest : BaseTest() {

    @Test
    fun testOkHttpClientAdjusted() {
        val activityController = buildActivity(MainActivity::class.java)
                .create()
                .start()
                .postCreate(null)
        ShadowLooper.pauseLooper(getPicassoDispatcherThreadLooper(activityController.get()))
        val activity = activityController.resume().visible().get()

        val adjustedOkHttpClientSocketFactory = getPicassoOkHttpClient(activity).sslSocketFactory()
        val defaultSocketFactory = Platform.get().sslContext.run {
            init(null, arrayOf(Util.platformTrustManager()), null)
            socketFactory
        }

        assertFalse(defaultSocketFactory.javaClass.isInstance(adjustedOkHttpClientSocketFactory))
    }
}
