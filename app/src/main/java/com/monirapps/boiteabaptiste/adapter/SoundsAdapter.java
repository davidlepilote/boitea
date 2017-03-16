package com.monirapps.boiteabaptiste.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.monirapps.boiteabaptiste.MainActivity;
import com.monirapps.boiteabaptiste.R;
import com.monirapps.boiteabaptiste.Typefaces;
import com.monirapps.boiteabaptiste.bo.Sound;
import com.monirapps.boiteabaptiste.fragment.SoundsFragment;
import com.monirapps.boiteabaptiste.ws.BoiteServices;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class SoundsAdapter extends RealmBasedRecyclerViewAdapter<Sound, SoundsAdapter.SoundViewHolder> {

  public static final String POSITION = "position";

  private final RealmResults<Sound> data;

  private final FirebaseAnalytics firebaseAnalytics;

  public SoundsAdapter(Context context, @Nullable RealmResults<Sound> data) {
    super(context, data, true, true);
    firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    this.data = data;
  }

  public static class SoundViewHolder extends RealmViewHolder {

    private CardView cardView;

    private TextView title;

    private TextView subtitle;

    private TextView totalClicks;

    private TextView myClicks;

    private LottieAnimationView favorite;

    public SoundViewHolder(View itemView) {
      super(itemView);
      cardView = (CardView) itemView.findViewById(R.id.card_view);
      title = (TextView) itemView.findViewById(R.id.title);
      subtitle = (TextView) itemView.findViewById(R.id.subtitle);
      totalClicks = (TextView) itemView.findViewById(R.id.total_clicks_number);
      myClicks = (TextView) itemView.findViewById(R.id.my_clicks_number);
      favorite = (LottieAnimationView) itemView.findViewById(R.id.favorite);
    }
  }

  @Override
  public SoundViewHolder onCreateRealmViewHolder(ViewGroup parent, int viewType) {
    return new SoundViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_item, parent, false));
  }

  @Override
  public void onBindRealmViewHolder(final SoundViewHolder holder, final int position) {
    final Sound sound = data.get(position);
    holder.title.setText(sound.getTitle());
    holder.title.setTypeface(Typefaces.GROBOLD.typeface(getContext()));
    if(TextUtils.isEmpty(sound.getSubtitle()) == false){
      holder.subtitle.setText(sound.getSubtitle());
      holder.subtitle.setVisibility(View.VISIBLE);
    } else {
      holder.subtitle.setVisibility(View.GONE);
    }
    holder.subtitle.setTypeface(Typefaces.GROBOLD.typeface(getContext()));
    holder.favorite.setProgress(sound.isFavorite() ? 1f : 0f);
    holder.myClicks.setText(String.format(Locale.FRENCH, "%d", sound.getPersonalHits()));
    holder.totalClicks.setText(String.format(Locale.FRENCH, "%d", sound.getGlobalHits()));
    holder.cardView.setCardBackgroundColor(Color.parseColor(sound.getColor()));
    holder.cardView.setSoundEffectsEnabled(false);
    holder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        MainActivity.playBaptiste(view.getContext(), sound.getSound());
        BoiteServices.API.hit(sound.getId());
        final Bundle data = new Bundle();
        data.putString("ID", sound.getId());
        firebaseAnalytics.logEvent("HIT", data);
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
        holder.favorite.cancelAnimation();
        holder.favorite.setProgress(0f);
        if(!sound.isFavorite()){
          holder.favorite.playAnimation();
        } else {
          LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(SoundsFragment.SET_CHANGED).putExtra(POSITION, position));
        }
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            realm.where(Sound.class).equalTo("id", sound.getId()).findFirst().setFavorite(!sound.isFavorite());
          }
        });
        realm.close();
      }
    });
  }

}
