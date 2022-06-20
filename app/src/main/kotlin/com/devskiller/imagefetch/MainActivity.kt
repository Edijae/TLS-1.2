package com.devskiller.imagefetch

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

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
            // END CHANGES
        }
    }

    private fun imageFetchedSuccessfully() = Log.v(LOG_TAG, "image fetched successfully!")

    private fun imageFetchedWithFailure(exception: Exception) = Log.v(LOG_TAG, "image fetched unsuccessfully!:$exception")
}
