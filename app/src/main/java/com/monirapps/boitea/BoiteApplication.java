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

  public static final String GLIDE_UPDATE_VALUE = "glideUpdateValue";
  public static int glideUpdateValue = 0;

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
    AdinCube.Native.Binder.init(getApplicationContext());

    glideUpdateValue = getSharedPreferences(MainActivity.SHARED, MODE_PRIVATE).getInt(GLIDE_UPDATE_VALUE, 0);

    OneSignal.startInit(this).init();
  }
}
