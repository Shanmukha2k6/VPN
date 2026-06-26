package com.blobatic.shieldfoxvpn

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VPNApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(this) {}
    }
}
