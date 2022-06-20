package com.devskiller.imagefetch

import okhttp3.internal.platform.Platform
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.doNothing
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions
import org.powermock.api.mockito.PowerMockito.verifyStatic
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import org.robolectric.Robolectric.buildActivity
import org.robolectric.shadows.ShadowLooper
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.InetAddress
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

@PowerMockIgnore(
        "android.*",
        "androidx.*",
        "javax.net.ssl.*",
        "okhttp3.mockwebserver.*",
        "org.robolectric.*"
)
@PrepareForTest(Platform::class, SSLContext::class)
class MainActivityVerifyTest : BaseTest() {

    companion object {

        private const val DEFAULT_HOST = "https://google.com"
        private const val DEFAULT_PORT = 4009
        private const val DEFAULT_LOCAL_PORT = 1002
        private const val DEFAULT_AUTO_CLOSE = true
        private const val TLSV12 = "TLSv1.2"
        private val TLSV12_ARRAY = arrayOf(TLSV12)
    }

    private lateinit var sslSocketMock: SSLSocket
    private lateinit var sslContextMock: SSLContext
    private lateinit var sslSocketFactoryMock: SSLSocketFactory
    private lateinit var platformMock: Platform
    private lateinit var inetAddressMock: InetAddress
    private lateinit var localInetAddressMock: InetAddress
    private lateinit var platformBuildCertificateChainCleanerArgumentCaptor: ArgumentCaptor<X509TrustManager>
    private lateinit var platformConfigureSslSocketFactoryArgumentCaptor: ArgumentCaptor<SSLSocketFactory>

    @JvmField
    @Rule
    var powerMockRule = PowerMockRule()

    private var activity: MainActivity? = null

    @Before
    fun setUp() {
        mockStatic(SSLContext::class.java)
        mockStatic(Platform::class.java)

        sslSocketMock = mock(SSLSocket::class.java)
        sslContextMock = mock(SSLContext::class.java)
        sslSocketFactoryMock = mock(SSLSocketFactory::class.java)
        platformMock = mock(Platform::class.java)
        inetAddressMock = mock(InetAddress::class.java)
        localInetAddressMock = mock(InetAddress::class.java)
        platformBuildCertificateChainCleanerArgumentCaptor = ArgumentCaptor.forClass(X509TrustManager::class.java)
        platformConfigureSslSocketFactoryArgumentCaptor = ArgumentCaptor.forClass(SSLSocketFactory::class.java)

        `when`(Platform.get()).thenReturn(platformMock)
        `when`(platformMock.buildCertificateChainCleaner(platformBuildCertificateChainCleanerArgumentCaptor.capture()))
                .thenReturn(null)
        doNothing().`when`(platformMock).configureSslSocketFactory(platformConfigureSslSocketFactoryArgumentCaptor.capture())
        `when`(SSLContext.getInstance(eq("TLSv1.2"))).thenReturn(sslContextMock)
        `when`(sslContextMock.socketFactory).thenReturn(sslSocketFactoryMock)

        val activityController = buildActivity(MainActivity::class.java).apply {
            prepareSslContextPreparator(get())
        }.create().start()
        ShadowLooper.pauseLooper(getPicassoDispatcherThreadLooper(activityController.get()))
        activity = activityController.postCreate(null).resume().visible().get()
    }

    @After
    fun tearDown() {
        activity = null

        verifyStatic(SSLContext::class.java)
        SSLContext.getInstance(eq(TLSV12))
        verify(sslContextMock).socketFactory
        verifyStatic(Platform::class.java, atLeastOnce())
        Platform.get()
        verify(platformMock).buildCertificateChainCleaner(platformBuildCertificateChainCleanerArgumentCaptor.value)
        verify(platformMock).configureSslSocketFactory(platformConfigureSslSocketFactoryArgumentCaptor.value)

        verifyNoMoreInteractions(
                SSLContext::class.java,
                Platform::class.java,
                sslSocketMock,
                sslContextMock,
                sslSocketFactoryMock,
                platformMock,
                inetAddressMock,
                localInetAddressMock
        )
    }

    @Test
    fun testSSLSocketFactoryCreateSocketWithSocketAndHostAndPortAndAutoClose() {
        `when`(sslSocketFactoryMock.createSocket(eq(sslSocketMock), eq(DEFAULT_HOST), eq(DEFAULT_PORT), eq(DEFAULT_AUTO_CLOSE)))
                .thenReturn(sslSocketMock)
        doNothing().`when`(sslSocketMock).enabledProtocols = eq(TLSV12_ARRAY)

        getPicassoOkHttpClient(activity!!).sslSocketFactory()
                .createSocket(sslSocketMock, DEFAULT_HOST, DEFAULT_PORT, DEFAULT_AUTO_CLOSE)

        verify(sslSocketFactoryMock).createSocket(sslSocketMock, DEFAULT_HOST, DEFAULT_PORT, DEFAULT_AUTO_CLOSE)
        verify(sslSocketMock).enabledProtocols = TLSV12_ARRAY
    }

    @Test
    fun testSSLSocketFactoryCreateSocketWithHostAndPort() {
        `when`(sslSocketFactoryMock.createSocket(eq(DEFAULT_HOST), eq(DEFAULT_PORT)))
                .thenReturn(sslSocketMock)
        doNothing().`when`(sslSocketMock).enabledProtocols = eq(TLSV12_ARRAY)

        getPicassoOkHttpClient(activity!!).sslSocketFactory()
                .createSocket(DEFAULT_HOST, DEFAULT_PORT)

        verify(sslSocketFactoryMock).createSocket(DEFAULT_HOST, DEFAULT_PORT)
        verify(sslSocketMock).enabledProtocols = TLSV12_ARRAY
    }

    @Test
    fun testSSLSocketFactoryCreateSocketWithHostAndPortAndLocalHostAndLocalPort() {
        `when`(sslSocketFactoryMock.createSocket(eq(DEFAULT_HOST), eq(DEFAULT_PORT), eq(localInetAddressMock), eq(DEFAULT_LOCAL_PORT)))
                .thenReturn(sslSocketMock)
        doNothing().`when`(sslSocketMock).enabledProtocols = eq(TLSV12_ARRAY)

        getPicassoOkHttpClient(activity!!).sslSocketFactory()
                .createSocket(DEFAULT_HOST, DEFAULT_PORT, localInetAddressMock, DEFAULT_LOCAL_PORT)

        verify(sslSocketFactoryMock).createSocket(DEFAULT_HOST, DEFAULT_PORT, localInetAddressMock, DEFAULT_LOCAL_PORT)
        verify(sslSocketMock).enabledProtocols = TLSV12_ARRAY
    }

    @Test
    fun testSSLSocketFactoryCreateSocketWithHostAddressAndPort() {
        `when`(sslSocketFactoryMock.createSocket(eq(inetAddressMock), eq(DEFAULT_PORT)))
                .thenReturn(sslSocketMock)
        doNothing().`when`(sslSocketMock).enabledProtocols = eq(TLSV12_ARRAY)

        getPicassoOkHttpClient(activity!!).sslSocketFactory()
                .createSocket(inetAddressMock, DEFAULT_PORT)

        verify(sslSocketFactoryMock).createSocket(inetAddressMock, DEFAULT_PORT)
        verify(sslSocketMock).enabledProtocols = TLSV12_ARRAY
    }

    @Test
    fun testSSLSocketFactoryCreateSocketWithHostAddressAndPortAndLocalHostAndLocalPort() {
        `when`(sslSocketFactoryMock.createSocket(eq(inetAddressMock), eq(DEFAULT_PORT), eq(localInetAddressMock), eq(DEFAULT_LOCAL_PORT)))
                .thenReturn(sslSocketMock)
        doNothing().`when`(sslSocketMock).enabledProtocols = eq(TLSV12_ARRAY)

        getPicassoOkHttpClient(activity!!).sslSocketFactory()
                .createSocket(inetAddressMock, DEFAULT_PORT, localInetAddressMock, DEFAULT_LOCAL_PORT)

        verify(sslSocketFactoryMock).createSocket(inetAddressMock, DEFAULT_PORT, localInetAddressMock, DEFAULT_LOCAL_PORT)
        verify(sslSocketMock).enabledProtocols = TLSV12_ARRAY
    }

    private fun prepareSslContextPreparator(activity: MainActivity) {
        val sslContextPreparatorField = activity::class.java.getDeclaredField("sslContextPreparator")
        sslContextPreparatorField.isAccessible = true
        val modifiersField = Field::class.java.getDeclaredField("modifiers")
        modifiersField.isAccessible = true
        modifiersField.setInt(sslContextPreparatorField, sslContextPreparatorField.modifiers and Modifier.FINAL.inv())
        sslContextPreparatorField.set(activity, dummySslContextPreparator())
        sslContextPreparatorField.isAccessible = false
    }

    private fun dummySslContextPreparator() = { _: SSLContext, _: X509TrustManager ->
    }
}
