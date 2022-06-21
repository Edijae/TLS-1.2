package com.devskiller.imagefetch

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class Tls12SocketFactory(val delegate: SSLSocketFactory): SSLSocketFactory(){

    private  val TLS_V12_ONLY = arrayOf("TLSv1.2")

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

    fun patch(socket:Socket):Socket{
        if(socket is SSLSocket){
            socket.enabledProtocols = TLS_V12_ONLY
        }
        return socket;
    }
}