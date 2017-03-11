package com.monirapps.boiteabaptiste;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements RealmRecyclerView.OnRefreshListener {

  private BoiteRecyclerView sounds;

  private RealmBasedRecyclerViewAdapter soundsAdapter;

  private static MediaPlayer mediaPlayer = new MediaPlayer();

  private View loader;

  private Realm realm;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    bindViews();

    realm = Realm.getDefaultInstance();

    realm.where(Config.class).findAll().addChangeListener(new RealmChangeListener<RealmResults<Config>>() {
      @Override
      public void onChange(RealmResults<Config> element) {
        if (element.size() > 0) {
          final Config config = element.get(0);
          endLoading(config);
        }
      }
    });

    // We make sure here that if there is a config, the loading WILL stop (bug in Realm Listener ?)
    final Config config = realm.where(Config.class).findFirst();
    if(config != null){
      endLoading(config);
    }

    soundsAdapter = new SoundsAdapter(getApplicationContext(), realm.where(Sound.class).equalTo("soundDownloaded", true).findAll());
    sounds.setOnRefreshListener(this);
    sounds.setAdapter(soundsAdapter);

    retrieveConfig();
  }

  private void endLoading(Config config) {
    loader.setVisibility(View.GONE);
    if (config.getTitle() != null) {
      getSupportActionBar().setTitle(config.getTitle());
    }
    for (Sound sound : config.getSounds()) {
      if(sound.isSoundDownloaded() == false){
        BoiteServices.API.downloadSound(getApplicationContext(), sound.getId(), sound.getSound());
      }
    }
    if (soundsAdapter != null) {
      soundsAdapter.notifyDataSetChanged();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    realm.close();
  }

  private void bindViews() {
    loader = findViewById(R.id.loader);
    sounds = (BoiteRecyclerView) findViewById(R.id.sounds);
  }

  private void retrieveConfig() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final InputStreamReader reader = new InputStreamReader(getAssets().open("config"), "UTF-8");
          final Config config = new Gson().fromJson(reader, Config.class);
          Realm realm = Realm.getDefaultInstance();
          realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              final Config savedConfig = realm.where(Config.class).findFirst();
              if (savedConfig == null) {
                realm.copyToRealm(config);
              } else {
                if (true || savedConfig.getUpdated() < config.getUpdated()) {
                  savedConfig.updateConfig(getApplicationContext(), config);
                }
              }
            }
          });
          realm.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }).start();
  }

  public static void playBaptiste(Context context, String resource) {
    try {
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = new MediaPlayer();
      AssetFileDescriptor descriptor = context.getAssets().openFd(resource);
      mediaPlayer.setDataSource(new FileInputStream(context.getFilesDir() + "/" + resource).getFD());
      descriptor.close();

      mediaPlayer.prepare();
      mediaPlayer.setVolume(1f, 1f);
      mediaPlayer.setLooping(false);
      mediaPlayer.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Override
  public void onRefresh() {

  }
}
