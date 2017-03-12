package com.monirapps.boiteabaptiste;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;

import com.adincube.sdk.nativead.recycler.NativeAdRecyclerViewAdapter;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.RealmBasedRecyclerViewAdapter;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class BoiteRecyclerView extends RealmRecyclerView {

  public BoiteRecyclerView(Context context) {
    super(context);
    init();
  }

  public BoiteRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public BoiteRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public BoiteRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int bufferItems) {
    super(context, attrs, defStyleAttr, bufferItems);
    init();
  }

  private void init() {
    ((SimpleItemAnimator) getRecycleView().getItemAnimator()).setSupportsChangeAnimations(false);
  }

  public void setAdapter(final NativeAdRecyclerViewAdapter adapter){
    getRecycleView().setAdapter(adapter);
  }
}
