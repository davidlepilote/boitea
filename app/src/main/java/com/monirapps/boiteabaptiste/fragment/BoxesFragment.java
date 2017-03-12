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
import com.monirapps.boiteabaptiste.adapter.BoxesAdapter;
import com.monirapps.boiteabaptiste.adapter.SoundsAdapter;
import com.monirapps.boiteabaptiste.bo.SoundBox;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmQuery;
import io.realm.Sort;

public class BoxesFragment extends Fragment implements RealmRecyclerView.OnRefreshListener {

  public static final String CONFIG_RETRIEVED = "config retrieved";

  private BoiteRecyclerView boxes;

  private RealmBasedRecyclerViewAdapter boxesAdapter;

  private Realm realm;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (CONFIG_RETRIEVED.equals(intent.getAction())){
        boxes.setRefreshing(false);
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
    final RealmQuery<SoundBox> data = realm.where(SoundBox.class).notEqualTo("packageName", getContext().getPackageName());
    boxesAdapter = new BoxesAdapter(getContext(), data.findAllSorted("updated", Sort.DESCENDING));

    boxes.setOnRefreshListener(this);
    boxes.setAdapter(boxesAdapter);
  }

  private void bindViews(View root) {
    boxes = (BoiteRecyclerView) root.findViewById(R.id.sounds);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
  }

  @Override
  public void onRefresh() {
    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(MainActivity.REFRESH_CONFIG));
  }
}
