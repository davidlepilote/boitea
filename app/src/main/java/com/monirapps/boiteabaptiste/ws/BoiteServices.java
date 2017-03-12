package com.monirapps.boiteabaptiste.ws;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.monirapps.boiteabaptiste.bo.Config;
import com.monirapps.boiteabaptiste.bo.Sound;
import com.monirapps.boiteabaptiste.fragment.SoundsFragment;
import com.monirapps.boiteabaptiste.bo.SoundBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public enum BoiteServices {

  API;

  public static final String BASE_URL = "http://david-fournier.fr/boite/";

  public static final String SUFFIX = "baptiste/";

  private RESTApi restApi;

  BoiteServices() {
    restApi = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RESTApi.class);
  }

  public Call<Config> getConfig(){
    return restApi.getConfig();
  }

  public Call<List<SoundBox>> getBoxes(){
    return restApi.getBoxes();
  }

  public void downloadSound(final Context context, final String id, final String soundPath) {
    restApi.getSound(soundPath).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        final InputStream fileSound = response.body().byteStream();
        copyInputStreamToFile(fileSound, new File(context.getFilesDir() + "/" + soundPath));
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            final Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
            if (sound != null) {
              sound.setSoundDownloaded(true);
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(SoundsFragment.REFRESH_LIST));
          }
        });
        realm.close();
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {

      }
    });
  }

  private void copyInputStreamToFile(InputStream in, File file) {
    try {
      OutputStream out = new FileOutputStream(file);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      out.close();
      in.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
