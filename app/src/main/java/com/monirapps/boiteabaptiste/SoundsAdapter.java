package com.monirapps.boiteabaptiste;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class SoundsAdapter extends RealmBasedRecyclerViewAdapter<Sound, SoundsAdapter.SoundViewHolder> {

  private final Realm realm = Realm.getDefaultInstance();

  private final RealmResults<Sound> data;

  public SoundsAdapter(Context context, @Nullable RealmResults<Sound> data) {
    super(context, data, true, true);
    this.data = data;
  }

  public static class SoundViewHolder extends RealmViewHolder {

    private CardView cardView;

    private TextView description;

    public SoundViewHolder(View itemView) {
      super(itemView);
      cardView = (CardView) itemView.findViewById(R.id.card_view);
      description = (TextView) itemView.findViewById(R.id.description);
    }
  }

  @Override
  public SoundViewHolder onCreateRealmViewHolder(ViewGroup parent, int viewType) {
    return new SoundViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.baptiste_item, parent, false));
  }

  @Override
  public void onBindRealmViewHolder(SoundViewHolder holder, int position) {
    final Sound sound = data.get(position);
    holder.description.setText(sound.getTitle());
    holder.cardView.setCardBackgroundColor(Color.parseColor(sound.getColor()));
    holder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        MainActivity.playBaptiste(view.getContext(), sound.getSound());
      }
    });
  }

}
