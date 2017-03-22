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

import com.monirapps.boitea.BuildConfig;
import com.monirapps.boitea.MainActivity;
import com.monirapps.boitea.R;
import com.monirapps.boitea.adapter.BoxesAdapter;
import com.monirapps.boitea.bo.SoundBox;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;

public class BoxesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

  public static final String CONFIG_RETRIEVED = "config retrieved";

  private RecyclerView boxes;

  private SwipeRefreshLayout pullToRefresh;

  private BoxesAdapter boxesAdapter;

  private Realm realm;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (CONFIG_RETRIEVED.equals(intent.getAction())){
        pullToRefresh.setRefreshing(false);
        boxesAdapter.notifyDataSetChanged();
      }
    }
  };

  public static BoxesFragment newInstance() {
    BoxesFragment fragment = new BoxesFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(CONFIG_RETRIEVED);
    LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, intentFilter);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    realm.close();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // Reuse sounds fragment as it is a simple recyclerview
    View root = inflater.inflate(R.layout.sounds_fragment, container, false);

    realm = Realm.getDefaultInstance();

    bindViews(root);

    refreshList();

    return root;
  }

  private void refreshList() {
    final RealmQuery<SoundBox> data = realm.where(SoundBox.class)
        .notEqualTo("packageName", BuildConfig.APPLICATION_ID)
        .equalTo("validated", true)
        .equalTo("banned", false);
    boxesAdapter = new BoxesAdapter(getContext(), data.findAllSorted("updated", Sort.DESCENDING));

    boxes.setLayoutManager(new LinearLayoutManager(getContext()));
    boxes.setAdapter(boxesAdapter);
  }

  private void bindViews(View root) {
    boxes = (RecyclerView) root.findViewById(R.id.sounds);
    root.findViewById(R.id.empty_view).setVisibility(View.GONE);
    pullToRefresh = (SwipeRefreshLayout) root.findViewById(R.id.pull_to_refresh);
    pullToRefresh.setOnRefreshListener(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
  }

  @Override
  public void onRefresh() {
    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(MainActivity.REFRESH_CONFIG));
    pullToRefresh.setRefreshing(true);
  }
}
