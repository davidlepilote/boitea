package com.monirapps.boiteabaptiste.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.monirapps.boiteabaptiste.fragment.SoundsFragment;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class SoundsAdapter extends RealmBasedRecyclerViewAdapter<Sound, SoundsAdapter.SoundViewHolder> {

  private final RealmResults<Sound> data;

  public SoundsAdapter(Context context, @Nullable RealmResults<Sound> data) {
    super(context, data, true, true);
    this.data = data;
  }

  public static class SoundViewHolder extends RealmViewHolder {

    private CardView cardView;

    private TextView title;

    private TextView subtitle;

    private TextView totalClicks;

    private TextView myClicks;

    private View favorite;

    public SoundViewHolder(View itemView) {
      super(itemView);
      cardView = (CardView) itemView.findViewById(R.id.card_view);
      title = (TextView) itemView.findViewById(R.id.title);
      subtitle = (TextView) itemView.findViewById(R.id.subtitle);
      totalClicks = (TextView) itemView.findViewById(R.id.total_clicks_number);
      myClicks = (TextView) itemView.findViewById(R.id.my_clicks_number);
      favorite = itemView.findViewById(R.id.favorite);
    }
  }

  @Override
  public SoundViewHolder onCreateRealmViewHolder(ViewGroup parent, int viewType) {
    return new SoundViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_item, parent, false));
  }

  @Override
  public void onBindRealmViewHolder(SoundViewHolder holder, final int position) {
    final Sound sound = data.get(position);
    holder.title.setText(sound.getTitle());
    if(TextUtils.isEmpty(sound.getSubtitle()) == false){
      holder.subtitle.setText(sound.getSubtitle());
      holder.subtitle.setVisibility(View.VISIBLE);
    } else {
      holder.subtitle.setVisibility(View.GONE);
    }
    holder.favorite.setSelected(sound.isFavorite());
    holder.myClicks.setText(String.format(Locale.FRENCH, "%d", sound.getPersonalHits()));
    holder.totalClicks.setText(String.format(Locale.FRENCH, "%d", sound.getGlobalHits()));
    holder.cardView.setCardBackgroundColor(Color.parseColor(sound.getColor()));
    holder.cardView.setSoundEffectsEnabled(false);
    holder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        MainActivity.playBaptiste(view.getContext(), sound.getSound());
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            final Sound currentSound = realm.where(Sound.class).equalTo("id", sound.getId()).findFirst();
            currentSound.setPersonalHits(currentSound.getPersonalHits() + 1);
            currentSound.setGlobalHits(currentSound.getGlobalHits() + 1);
          }
        });
        realm.close();
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(SoundsFragment.SET_CHANGED));
       }
    });
    holder.favorite.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        view.setSelected(!view.isSelected());
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            realm.where(Sound.class).equalTo("id", sound.getId()).findFirst().setFavorite(view.isSelected());
          }
        });
        realm.close();
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(SoundsFragment.SET_CHANGED));
      }
    });
  }

}
