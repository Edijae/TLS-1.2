package com.devskiller.imagefetch

import okhttp3.TlsVersion
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class Tls12SocketFactory(private val delegate: SSLSocketFactory): SSLSocketFactory(){

    override fun getDefaultCipherSuites(): Array<String>{
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String>{
        return delegate.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host:String, port:Int, autoclose:Boolean):Socket?{
        return patch(delegate.createSocket(s,host, port, autoclose))
    }

    @Throws(IOException::class)
    override fun createSocket(host:String, port:Int):Socket?{
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host:String, port:Int, localHost: InetAddress, localPort:Int):Socket?{
        return patch(delegate.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host:InetAddress, port:Int):Socket?{
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address:InetAddress, port:Int, localAddress:InetAddress,localPort:Int):Socket?{
        return patch(delegate.createSocket(address, port, localAddress, localPort))
    }

    private fun patch(socket:Socket):Socket{
        if(socket is SSLSocket){
            socket.enabledProtocols = arrayOf(TlsVersion.TLS_1_2.javaName())
        }
        return socket;
    }
}