package com.example.bazsi.networklayer.interfaces;

import android.net.nsd.NsdServiceInfo;

import java.net.Socket;

/**
 * Created by BalazsAdmin on 2/20/2018.
 */

public interface ServiceDiscoverListener {
    public void serviceFound(Socket socket, NsdServiceInfo mService);
}
