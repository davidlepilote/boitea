package com.monirapps.boitea;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.support.multidex.MultiDexApplication;

import com.adincube.sdk.AdinCube;
import com.onesignal.OneSignal;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class BoiteApplication extends MultiDexApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    Realm.init(getApplicationContext());
    final RealmConfiguration realmConfiguration = new RealmConfiguration
        .Builder()
        .deleteRealmIfMigrationNeeded()
        .build();
    Realm.setDefaultConfiguration(realmConfiguration);

    AdinCube.setAppKey(getString(R.string.adincube_app_key));

    OneSignal.startInit(this).init();
  }
}
