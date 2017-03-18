package com.monirapps.boitea.ws;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.crash.FirebaseCrash;
import com.monirapps.boitea.bo.Config;
import com.monirapps.boitea.bo.Sound;
import com.monirapps.boitea.bo.SoundBox;
import com.monirapps.boitea.fragment.SoundsFragment;
import com.monirapps.boiteabaptiste.BuildConfig;

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

  public static final String BASE_URL = "http://laboitea.api.monirapps.com/";

  public static final String SUFFIX = BuildConfig.ENDPOINT_SUFFIX;

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

  public void hit(final String id){
    restApi.hit(id, BuildConfig.ENDPOINT_SUFFIX).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if(!response.isSuccessful()){
          FirebaseCrash.report(new IllegalArgumentException(response.code() + ": Hit on " + id + " failed"));
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        FirebaseCrash.report(t);
      }
    });
  }

  public void downloadSound(final Context context, final String id, final String soundPath) {
    restApi.getSound(soundPath).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if(response.isSuccessful()){
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
        } else {
          FirebaseCrash.report(new IllegalArgumentException(response.code() + " : Sound " + soundPath + " not downloaded"));
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        FirebaseCrash.report(t);
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
