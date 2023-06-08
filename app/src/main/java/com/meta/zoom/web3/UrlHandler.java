package com.meta.zoom.web3;

import android.net.Uri;

public interface UrlHandler {

    String getScheme();

    String handle(Uri uri);
}