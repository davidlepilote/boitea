package com.monirapps.boiteabaptiste;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SoundsFragment extends Fragment implements RealmRecyclerView.OnRefreshListener {

  private BoiteRecyclerView sounds;

  private RealmBasedRecyclerViewAdapter soundsAdapter;

  private Realm realm;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    realm = Realm.getDefaultInstance();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.sounds_fragment, container, false);

    bindViews(root);

    realm.where(Config.class).findAll().addChangeListener(new RealmChangeListener<RealmResults<Config>>() {
      @Override
      public void onChange(RealmResults<Config> element) {
        if (element.size() > 0) {
          if (soundsAdapter != null) {
            soundsAdapter.notifyDataSetChanged();
          }
        }
      }
    });

    // We make sure here that if there is a config, the loading WILL stop (bug in Realm Listener ?)
    final Config config = realm.where(Config.class).findFirst();
    if(config != null){
      if (soundsAdapter != null) {
        soundsAdapter.notifyDataSetChanged();
      }
    }

    soundsAdapter = new SoundsAdapter(getContext(), realm.where(Sound.class).equalTo("soundDownloaded", true).findAll());
    sounds.setOnRefreshListener(this);
    sounds.setAdapter(soundsAdapter);

    return root;
  }

  private void bindViews(View root) {
    sounds = (BoiteRecyclerView) root.findViewById(R.id.sounds);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    realm.close();
  }

  @Override
  public void onRefresh() {

  }
}
