package com.monirapps.boitea.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.monirapps.boitea.R;
import com.monirapps.boitea.fragment.SoundsFragment;
import com.monirapps.boitea.MainActivity;
import com.monirapps.boitea.Typefaces;
import com.monirapps.boitea.bo.Sound;
import com.monirapps.boitea.ws.BoiteServices;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class SoundsAdapter extends RecyclerView.Adapter<SoundsAdapter.SoundViewHolder> {

  public static class SoundViewHolder extends RecyclerView.ViewHolder {

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

  public static final String POSITION = "position";

  private final RealmResults<Sound> data;

  private final FirebaseAnalytics firebaseAnalytics;

  private final Context context;

  public SoundsAdapter(Context context, @Nullable RealmResults<Sound> data) {
    this.context = context;
    firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    this.data = data;
  }

  @Override
  public SoundViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new SoundViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_item, parent, false));
  }

  @Override
  public void onBindViewHolder(final SoundViewHolder holder, int pos) {
    final int position = holder.getAdapterPosition();
    final Sound sound = data.get(position);
    holder.title.setText(sound.getTitle());
    holder.title.setTypeface(Typefaces.GROBOLD.typeface(context));
    if(TextUtils.isEmpty(sound.getSubtitle()) == false){
      holder.subtitle.setText(sound.getSubtitle());
      holder.subtitle.setVisibility(View.VISIBLE);
    } else {
      holder.subtitle.setVisibility(View.GONE);
    }
    holder.subtitle.setTypeface(Typefaces.GROBOLD.typeface(context));
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
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(SoundsFragment.HIT).putExtra(SoundsFragment.HIT, position));
      }
    });
    holder.favorite.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        holder.favorite.cancelAnimation();
        holder.favorite.setProgress(0f);
        final Bundle data = new Bundle();
        data.putString("ID", sound.getId());
        if(!sound.isFavorite()){
          data.putBoolean("FAV", true);
          holder.favorite.playAnimation();
        } else {
          data.putBoolean("FAV", false);
          LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(SoundsFragment.SET_CHANGED).putExtra(POSITION, position));
        }
        firebaseAnalytics.logEvent("FAV", data);
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

  @Override
  public int getItemCount() {
    return data.size();
  }

}
