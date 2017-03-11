package com.monirapps.boiteabaptiste;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.realm.Realm;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public enum BoiteServices {

  API;

  public void downloadSound(final Context context , final String id, final String soundPath){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final InputStream fileSound = context.getAssets().open(soundPath);
          copyInputStreamToFile(fileSound, new File(context.getFilesDir() + "/" + soundPath));
          Realm realm = Realm.getDefaultInstance();
          realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              final Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
              if(sound != null){
                sound.setSoundDownloaded(true);
              }
            }
          });
          realm.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void copyInputStreamToFile(InputStream in, File file ) {
    try {
      OutputStream out = new FileOutputStream(file);
      byte[] buf = new byte[1024];
      int len;
      while((len=in.read(buf))>0){
        out.write(buf,0,len);
      }
      out.close();
      in.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
