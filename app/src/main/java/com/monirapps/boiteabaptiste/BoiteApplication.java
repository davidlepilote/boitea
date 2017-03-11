package com.monirapps.boiteabaptiste;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class BoiteApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    Realm.init(getApplicationContext());
    final RealmConfiguration realmConfiguration = new RealmConfiguration
        .Builder()
        .deleteRealmIfMigrationNeeded()
        .build();
    Realm.setDefaultConfiguration(realmConfiguration);
  }
}
