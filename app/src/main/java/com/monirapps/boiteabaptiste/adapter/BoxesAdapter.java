package com.monirapps.boiteabaptiste.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.monirapps.boiteabaptiste.MainActivity;
import com.monirapps.boiteabaptiste.R;
import com.monirapps.boiteabaptiste.bo.Sound;
import com.monirapps.boiteabaptiste.bo.SoundBox;
import com.monirapps.boiteabaptiste.fragment.SoundsFragment;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class BoxesAdapter extends RealmBasedRecyclerViewAdapter<SoundBox, BoxesAdapter.SoundBoxViewHolder> {

  private final RealmResults<SoundBox> data;

  public BoxesAdapter(Context context, @Nullable RealmResults<SoundBox> data) {
    super(context, data, true, true);
    this.data = data;
    addFooter();
  }

  public static class SoundBoxViewHolder extends RealmViewHolder {

    private CardView cardView;

    private TextView title;

    public SoundBoxViewHolder(View itemView) {
      super(itemView);
      cardView = (CardView) itemView.findViewById(R.id.card_view);
      title = (TextView) itemView.findViewById(R.id.title);
    }
  }

  @Override
  public SoundBoxViewHolder onCreateRealmViewHolder(ViewGroup parent, int viewType) {
    return new SoundBoxViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_item, parent, false));
  }

  @Override
  public void onBindRealmViewHolder(SoundBoxViewHolder holder, final int position) {
    final SoundBox sound = data.get(position);
    holder.title.setText(sound.getTitle());
    holder.cardView.setCardBackgroundColor(Color.parseColor(sound.getColor()));
    holder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + sound.getPackageName()));
        getContext().startActivity(intent);
      }
    });
  }

  @Override
  public SoundBoxViewHolder onCreateFooterViewHolder(ViewGroup parent) {
    return new SoundBoxViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_item, parent, false));
  }

  @Override
  public void onBindFooterViewHolder(SoundBoxViewHolder holder, int position) {
    holder.title.setText("FOOTER");
    holder.cardView.setCardBackgroundColor(Color.BLACK);
    holder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

      }
    });
  }
}
