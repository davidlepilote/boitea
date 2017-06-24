package com.monirapps.boitea.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monirapps.boitea.MainActivity;
import com.monirapps.boitea.R;
import com.monirapps.boitea.SortStyle;
import com.monirapps.boitea.adapter.SoundsAdapter;
import com.monirapps.boitea.bo.Sound;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class SoundsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

  public static final String ONLY_FAVORITES = "only favorites";

  public static final String REFRESH_LIST = "refresh list";

  public static final String SET_CHANGED = "set changed";

  public static final String CONFIG_RETRIEVED = "config retrieved";

  public static final String HIT = "hit";

  private RecyclerView sounds;

  private SwipeRefreshLayout pullToRefresh;

  private SoundsAdapter soundsAdapter;

  private View emptyView;

  private Realm realm;

  private boolean onlyFavorites;

  private RealmResults<Sound> data;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (REFRESH_LIST.equals(intent.getAction())) {
        refreshList();
        pullToRefresh.setRefreshing(false);
      }
      if (CONFIG_RETRIEVED.equals(intent.getAction())){
        pullToRefresh.setRefreshing(false);
        soundsAdapter.notifyItemRangeChanged(0, data.size());
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
    intentFilter.addAction(HIT);
    intentFilter.addAction(CONFIG_RETRIEVED);
    LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, intentFilter);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    soundsAdapter.destroy();
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

  private void  refreshList() {

    if(data != null){
      data.removeAllChangeListeners();
    }

    final RealmQuery<Sound> query = realm.where(Sound.class).equalTo("soundDownloaded", true);
    final SortStyle sortingStyle = SortStyle.getByPosition(getContext().getSharedPreferences(MainActivity.SHARED, Context.MODE_PRIVATE).getInt(MainActivity.SORTING_STYLE, SortStyle.TOTAL_CLICKS.position));
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
          //soundsAdapter.notifyDataSetChanged();
          return;
        }

        // There is a swap between two items
        if (changeSet.getDeletions().length == 1 && changeSet.getInsertions().length == 1) {
          final int from = soundsAdapter.getRealIndex(changeSet.getDeletions()[0]);
          final int to = soundsAdapter.getRealIndex(changeSet.getInsertions()[0]);
          soundsAdapter.notifyItemMoved(from, to);
          //soundsAdapter.notifyItemChanged(from);
          //soundsAdapter.notifyItemChanged(to);
        } else {
          // For deletions, the adapter has to be notified in reverse order.

          int[] deletions = changeSet.getDeletions();
          for (int i = deletions.length - 1; i >= 0; i--) {
            soundsAdapter.notifyItemRemoved(soundsAdapter.getRealIndex(deletions[i]));
          }

          int[] insertions = changeSet.getInsertions();
          for (Integer insertion : insertions) {
            soundsAdapter.notifyItemInserted(soundsAdapter.getRealIndex(insertion));
          }

          // If not visible, just refresh the list
          if (!getUserVisibleHint()) {
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

    sounds.setLayoutManager(new LinearLayoutManager(getContext()) {
      @Override
      public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
          super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
          e.printStackTrace();
        }
      }
    });
    final ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(soundsAdapter, 0.8f);
    scaleInAnimationAdapter.setFirstOnly(false);
    scaleInAnimationAdapter.setDuration(200);
    sounds.setAdapter(scaleInAnimationAdapter);
  }

  private void bindViews(View root) {
    sounds = (RecyclerView) root.findViewById(R.id.sounds);
    emptyView = root.findViewById(R.id.empty_view);
    pullToRefresh = (SwipeRefreshLayout) root.findViewById(R.id.pull_to_refresh);
    pullToRefresh.setOnRefreshListener(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if(data != null){
      data.removeAllChangeListeners();
    }
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
  }

  @Override
  public void onRefresh() {
    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(MainActivity.REFRESH_CONFIG));
    pullToRefresh.setRefreshing(true);
  }
}
