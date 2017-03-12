package com.monirapps.boiteabaptiste;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class SoundsFragment extends Fragment implements RealmRecyclerView.OnRefreshListener {

  public static final String ONLY_FAVORITES = "only favorites";

  public static final String REFRESH_LIST = "refresh list";

  private BoiteRecyclerView sounds;

  private RealmBasedRecyclerViewAdapter soundsAdapter;

  private Realm realm;

  private boolean onlyFavorites;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (REFRESH_LIST.equals(intent.getAction())) {
        refreshList();
      }
    }
  };

  public static SoundsFragment newInstance(boolean onlyFavorites) {
    Bundle args = new Bundle();
    args.putBoolean(ONLY_FAVORITES, onlyFavorites);
    SoundsFragment fragment = new SoundsFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    onlyFavorites = getArguments().getBoolean(ONLY_FAVORITES);
    LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, new IntentFilter(REFRESH_LIST));
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    realm.close();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.sounds_fragment, container, false);

    realm = Realm.getDefaultInstance();

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
    if (config != null) {
      if (soundsAdapter != null) {
        soundsAdapter.notifyDataSetChanged();
      }
    }

    refreshList();

    return root;
  }

  private void refreshList() {
    final RealmQuery<Sound> data = realm.where(Sound.class).equalTo("soundDownloaded", true);
    final SortStyle sortingStyle = SortStyle.getByPosition(getActivity().getPreferences(Context.MODE_PRIVATE).getInt(MainActivity.SORTING_STYLE, SortStyle.ALPHA.position));
    if (onlyFavorites) {
      data.equalTo("favorite", true);
    }
    soundsAdapter = new SoundsAdapter(getContext(), data.findAllSorted(sortingStyle.sortingField, sortingStyle.order));
    sounds.setOnRefreshListener(this);
    sounds.setAdapter(soundsAdapter);
  }

  private void bindViews(View root) {
    sounds = (BoiteRecyclerView) root.findViewById(R.id.sounds);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
  }

  @Override
  public void onRefresh() {

  }
}
