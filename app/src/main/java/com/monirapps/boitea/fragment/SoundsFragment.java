package com.monirapps.boitea.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adincube.sdk.nativead.NativeAdViewBinding;
import com.adincube.sdk.nativead.recycler.NativeAdRecyclerViewAdapter;
import com.adincube.sdk.nativead.stream.NativeAdStreamPositions;
import com.monirapps.boitea.BoiteRecyclerView;
import com.monirapps.boitea.MainActivity;
import com.monirapps.boitea.R;
import com.monirapps.boitea.SortStyle;
import com.monirapps.boitea.adapter.SoundsAdapter;
import com.monirapps.boitea.bo.Sound;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class SoundsFragment extends Fragment implements RealmRecyclerView.OnRefreshListener {

  public static final String ONLY_FAVORITES = "only favorites";

  public static final String REFRESH_LIST = "refresh list";

  public static final String SET_CHANGED = "set changed";

  public static final String CONFIG_RETRIEVED = "config retrieved";

  public static final String HIT = "hit";

  private RecyclerView sounds;

  private SoundsAdapter soundsAdapter;

  private View emptyView;

  private NativeAdRecyclerViewAdapter<SoundsAdapter.SoundViewHolder> nativeAdapter;

  private Realm realm;

  private boolean onlyFavorites;

  private RealmResults<Sound> data;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (REFRESH_LIST.equals(intent.getAction())) {
        refreshList();
      }
//      if (HIT.equals(intent.getAction())) {
//        final int position = intent.getIntExtra(HIT, -1);
//        if (position != -1) {
//          soundsAdapter.notifyItemChanged(position);
//        } else {
//          soundsAdapter.notifyDataSetChanged();
//        }
//      }
//      if (SET_CHANGED.equals(intent.getAction())) {
//        if (!onlyFavorites && !getUserVisibleHint()) {
//          soundsAdapter.notifyDataSetChanged();
//        }
//      }
//      if (CONFIG_RETRIEVED.equals(intent.getAction())) {
//        //sounds.setRefreshing(false);
//        soundsAdapter.notifyDataSetChanged();
//      }
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
    intentFilter.addAction(HIT);
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
    View root = inflater.inflate(R.layout.sounds_fragment, container, false);

    realm = Realm.getDefaultInstance();

    bindViews(root);

    refreshList();

    return root;
  }

  private void refreshList() {

    final RealmQuery<Sound> query = realm.where(Sound.class).equalTo("soundDownloaded", true);
    final SortStyle sortingStyle = SortStyle.getByPosition(getContext().getSharedPreferences(MainActivity.SHARED, Context.MODE_PRIVATE).getInt(MainActivity.SORTING_STYLE, SortStyle.ALPHA.position));
    if (onlyFavorites) {
      query.equalTo("favorite", true);
    }
    data = query.findAllSorted(sortingStyle.sortingField, sortingStyle.order);
    emptyView.setVisibility(onlyFavorites && data.size() == 0 ? View.VISIBLE : View.INVISIBLE);

    data.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Sound>>() {
      @Override
      public void onChange(RealmResults<Sound> collection, OrderedCollectionChangeSet changeSet) {
        // `null`  means the async query returns the first time.

        emptyView.setVisibility(onlyFavorites && collection.size() == 0 ? View.VISIBLE : View.INVISIBLE);

        if (changeSet == null) {
          soundsAdapter.notifyDataSetChanged();
          return;
        }

        // There is a swap between two items
        if(changeSet.getDeletions().length == 1 && changeSet.getInsertions().length == 1){
          soundsAdapter.notifyItemMoved(changeSet.getDeletions()[0], changeSet.getInsertions()[0]);
          soundsAdapter.notifyItemChanged(changeSet.getDeletions()[0]);
          soundsAdapter.notifyItemChanged(changeSet.getInsertions()[0]);
        } else {
          // For deletions, the adapter has to be notified in reverse order.

          OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
          for (int i = deletions.length - 1; i >= 0; i--) {
            OrderedCollectionChangeSet.Range range = deletions[i];
            soundsAdapter.notifyItemRemoved(range.startIndex);
            //soundsAdapter.notifyItemRangeRemoved(range.startIndex, range.length);
          }

          OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
          for (OrderedCollectionChangeSet.Range range : insertions) {
            soundsAdapter.notifyItemRangeInserted(range.startIndex, range.length);
          }

          // If not visible, just refresh the list
          if(!getUserVisibleHint()){
            soundsAdapter.notifyDataSetChanged();
          }

//          OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
//          for (OrderedCollectionChangeSet.Range range : modifications) {
//            soundsAdapter.notifyItemRangeChanged(range.startIndex, range.length);
//          }
        }
      }
    });

    soundsAdapter = new SoundsAdapter(getContext(), data);

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

    sounds.setLayoutManager(new LinearLayoutManager(getContext()));
    final ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(soundsAdapter, 0.8f);
    scaleInAnimationAdapter.setFirstOnly(false);
    scaleInAnimationAdapter.setDuration(200);
    sounds.setAdapter(scaleInAnimationAdapter);
  }

  private void bindViews(View root) {
    sounds = (RecyclerView) root.findViewById(R.id.sounds);
    emptyView = root.findViewById(R.id.empty_view);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (nativeAdapter != null) {
      //nativeAdapter.refreshAds();
    }
  }

  @Override
  public void onRefresh() {
    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(MainActivity.REFRESH_CONFIG));
  }
}
