package com.monirapps.boitea;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.support.multidex.MultiDexApplication;

import com.adincube.sdk.AdinCube;
import com.onesignal.OneSignal;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

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
        .schemaVersion(1L)
        .migration(new RealmMigration() {
          @Override
          public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            // 1 : added deeplink to sound
            final RealmSchema schema = realm.getSchema();
            if (oldVersion == 0) {
              schema.create("Promo")
                  .addField("deeplink", String.class, FieldAttribute.PRIMARY_KEY)
                  .addField("title", String.class)
                  .addField("subtitle", String.class)
                  .addField("color", String.class)
                  .addField("picture", String.class);
            }
          }
        })
        .build();
    Realm.setDefaultConfiguration(realmConfiguration);

    AdinCube.setAppKey(getString(R.string.adincube_app_key));
    AdinCube.Native.Binder.init(getApplicationContext());

    glideUpdateValue = getSharedPreferences(MainActivity.SHARED, MODE_PRIVATE).getInt(GLIDE_UPDATE_VALUE, 0);

    OneSignal.startInit(this).init();
  }
}
