package com.devskiller.imagefetch

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.lang.ref.WeakReference
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.*

class MainActivity : AppCompatActivity() {

    companion object {

        private const val LOG_TAG = "OkHttp_TLS_MainActivity"
    }

    private class ImageCallback(parent: MainActivity) : Callback {

        private val weakParent = WeakReference(parent)

        override fun onSuccess() {
            weakParent.get()?.imageFetchedSuccessfully()
        }

        override fun onError(e: Exception?) {
            e?.run {
                weakParent.get()?.imageFetchedWithFailure(this)
            }
        }
    }

    private var picasso: Picasso? = null
    private val sslContextPreparator: (SSLContext, X509TrustManager) -> Unit = SslContextPreparator::prepareSslContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        picasso = Picasso.Builder(this)
                .indicatorsEnabled(false)
                .loggingEnabled(false)
                .downloader(OkHttp3Downloader(OkHttpClient.Builder()
                        .enableTls12OnPreLollipop()
                        .build()))
                .build()
    }

    override fun onResume() {
        super.onResume()

        picasso!!.load("https://opensource.org/files/osi_keyhole_300X300_90ppi_0.png")
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .tag(LOG_TAG)
                .into(image_holder, ImageCallback(this))
    }

    override fun onPause() {
        picasso!!.cancelTag(LOG_TAG)

        super.onPause()
    }

    override fun onStop() {
        picasso = null

        super.onStop()
    }

    private fun OkHttpClient.Builder.enableTls12OnPreLollipop(): OkHttpClient.Builder = apply {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            // START CHANGES
            val manager = CustomTrustManager()
            val context = SSLContext.getInstance("TLSv1.2")
            sslContextPreparator(context,manager)
            sslSocketFactory(Tls12SocketFactory(context.socketFactory), manager)
            connectionSpecs(arrayListOf(
                ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build(),
                ConnectionSpec.COMPATIBLE_TLS,
                ConnectionSpec.CLEARTEXT)
            )
            // END CHANGES
        }
        return this
    }
    private fun imageFetchedSuccessfully() = Log.v(LOG_TAG, "image fetched successfully!")

    private fun imageFetchedWithFailure(exception: Exception) = Log.v(LOG_TAG, "image fetched unsuccessfully!:$exception")
}
