package com.monirapps.boitea.ws;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.google.firebase.crash.FirebaseCrash;
import com.monirapps.boitea.BoiteApplication;
import com.monirapps.boitea.BuildConfig;
import com.monirapps.boitea.bo.Config;
import com.monirapps.boitea.bo.Sound;
import com.monirapps.boitea.bo.SoundBox;
import com.monirapps.boitea.fragment.SoundsFragment;

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

  public Call<Config> getConfig() {
    return restApi.getConfig();
  }

  public Call<List<SoundBox>> getBoxes() {
    return restApi.getBoxes();
  }

  public void hit(final String id) {
    restApi.hit(id, BuildConfig.ENDPOINT_SUFFIX).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (!response.isSuccessful()) {
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
      public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
        if (response.isSuccessful()) {
          new Thread(new Runnable() {
            @Override
            public void run() {
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
                }
              });
              realm.close();
            }
          }).start();
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

  public static void copyInputStreamToFile(InputStream in, File file) {
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

  public static void bindPicture(final Context context, String url, final ImageView imageView, long updated) {
    Glide.with(context)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .signature(new StringSignature("" + updated))
        .thumbnail(Glide
            .with(context)
            .load(url)
            .dontAnimate()
            .transform(new CircleTransform(context))
            .signature(new StringSignature("" + updated)))
        .centerCrop()
        .dontAnimate()
        .transform(new CircleTransform(context))
        .into(imageView);
  }

  static class CircleTransform extends BitmapTransformation {
    CircleTransform(Context context) {
      super(context);
    }

    @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
      return circleCrop(pool, toTransform);
    }

    private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
      if (source == null) return null;

      int size = Math.min(source.getWidth(), source.getHeight());
      int x = (source.getWidth() - size) / 2;
      int y = (source.getHeight() - size) / 2;

      Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

      Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
      if (result == null) {
        result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
      }

      Canvas canvas = new Canvas(result);
      Paint paint = new Paint();
      paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
      paint.setAntiAlias(true);
      float r = size / 2f;
      canvas.drawCircle(r, r, r, paint);
      return result;
    }

    @Override public String getId() {
      return getClass().getName();
    }
  }
}
