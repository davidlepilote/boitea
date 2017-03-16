package com.monirapps.boiteabaptiste.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adincube.sdk.nativead.NativeAdViewBinding;
import com.adincube.sdk.nativead.recycler.NativeAdRecyclerViewAdapter;
import com.adincube.sdk.nativead.stream.NativeAdStreamPositions;
import com.monirapps.boiteabaptiste.BoiteRecyclerView;
import com.monirapps.boiteabaptiste.MainActivity;
import com.monirapps.boiteabaptiste.R;
import com.monirapps.boiteabaptiste.SortStyle;
import com.monirapps.boiteabaptiste.adapter.SoundsAdapter;
import com.monirapps.boiteabaptiste.bo.Sound;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmQuery;

public class SoundsFragment extends Fragment implements RealmRecyclerView.OnRefreshListener {

  public static final String ONLY_FAVORITES = "only favorites";

  public static final String REFRESH_LIST = "refresh list";

  public static final String SET_CHANGED = "set changed";

  public static final String CONFIG_RETRIEVED = "config retrieved";

  private BoiteRecyclerView sounds;

  private RealmBasedRecyclerViewAdapter soundsAdapter;

  private NativeAdRecyclerViewAdapter<SoundsAdapter.SoundViewHolder> nativeAdapter;

  private Realm realm;

  private boolean onlyFavorites;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (REFRESH_LIST.equals(intent.getAction())) {
        refreshList();
      }
      if (SET_CHANGED.equals(intent.getAction())){
        if(!onlyFavorites && !getUserVisibleHint()){
          soundsAdapter.notifyDataSetChanged();
        }
      }
      if (CONFIG_RETRIEVED.equals(intent.getAction())){
        sounds.setRefreshing(false);
        soundsAdapter.notifyDataSetChanged();
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
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(REFRESH_LIST);
    intentFilter.addAction(SET_CHANGED);
    intentFilter.addAction(CONFIG_RETRIEVED);
    LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, intentFilter);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    nativeAdapter.destroy();
    realm.close();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(onlyFavorites ? R.layout.favorites_fragment : R.layout.sounds_fragment, container, false);

    realm = Realm.getDefaultInstance();

    bindViews(root);

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

    NativeAdViewBinding binding = new NativeAdViewBinding.Builder(R.layout.native_ad_item)
        .withTitleViewId(R.id.title)
        .withCallToActionViewId(R.id.cta)
        .withDescriptionViewId(R.id.description)
        .withRatingViewId(R.id.rating)
        .withIconViewId(R.id.icon)
        .withCoverViewId(R.id.cover)
        .build();

    NativeAdStreamPositions positions = new NativeAdStreamPositions.Builder()
        .withPredefinedPositions(2) // position starts at 0.
        .withRepeatFrequency(4)
        .build();

    nativeAdapter = new NativeAdRecyclerViewAdapter<SoundsAdapter.SoundViewHolder>(getContext(), soundsAdapter, binding, positions);

    sounds.setOnRefreshListener(this);
    sounds.setAdapter(soundsAdapter);
    sounds.setAdapter(nativeAdapter);
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
  public void onResume() {
    super.onResume();
    nativeAdapter.refreshAds();
  }

  @Override
  public void onRefresh() {
    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(MainActivity.REFRESH_CONFIG));
  }
}
