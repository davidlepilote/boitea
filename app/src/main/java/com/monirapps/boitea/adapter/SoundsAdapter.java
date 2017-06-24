package com.monirapps.boitea.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.adincube.sdk.AdinCube;
import com.adincube.sdk.AdinCubeNativeEventListener;
import com.adincube.sdk.NativeAd;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.monirapps.boitea.R;
import com.monirapps.boitea.MainActivity;
import com.monirapps.boitea.Typefaces;
import com.monirapps.boitea.bo.Sound;
import com.monirapps.boitea.ws.BoiteServices;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.R.attr.shape;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class SoundsAdapter extends RecyclerView.Adapter<SoundsAdapter.SoundViewHolder> {

  public static final String SOUND_TITLE = "sound title";

  public static class SoundViewHolder extends RecyclerView.ViewHolder {

    private CardView cardView;

    private TextView title;

    private TextView subtitle;

    private TextView totalClicks;

    private TextView myClicks;

    private LottieAnimationView favorite;

    private String id;

    public SoundViewHolder(View itemView) {
      super(itemView);
      cardView = (CardView) itemView.findViewById(R.id.card_view);
      if (cardView != null) {
        cardView.setSoundEffectsEnabled(false);
        title = (TextView) itemView.findViewById(R.id.title);
        subtitle = (TextView) itemView.findViewById(R.id.subtitle);
        totalClicks = (TextView) itemView.findViewById(R.id.total_clicks_number);
        myClicks = (TextView) itemView.findViewById(R.id.my_clicks_number);
        favorite = (LottieAnimationView) itemView.findViewById(R.id.favorite);
      }
    }

  }
  public static class NativeAdViewHolder extends SoundViewHolder {

    private ImageView icon;

    private ImageView cover;

    private Button cta;

    private RatingBar rating;

    private TextView title;

    private TextView description;

    private ViewGroup root;

    public NativeAdViewHolder(View itemView) {
      super(itemView);
      root = (ViewGroup) itemView.findViewById(R.id.native_ad_root);
      icon = (ImageView) itemView.findViewById(R.id.icon);
      cover = (ImageView) itemView.findViewById(R.id.cover);
      title = (TextView) itemView.findViewById(R.id.title);
      rating = (RatingBar) itemView.findViewById(R.id.rating);
      description = (TextView) itemView.findViewById(R.id.description);
      cta = (Button) itemView.findViewById(R.id.cta);
      cta.setBackgroundDrawable(buttonNativeAdDrawable);
    }

  }
  private static final int SOUND = 1;

  private static final int NATIVE = 2;

  private final RealmResults<Sound> data;

  private final FirebaseAnalytics firebaseAnalytics;

  private final Context context;

  private final Map<Integer, NativeAd> nativeAds = new TreeMap<>();

  private final Set<Integer> requestedAd = new HashSet<>();

  private final int nativeAdMargin;

  private final int nativeAdCornerRadius;

  private static GradientDrawable buttonNativeAdDrawable = new GradientDrawable();

  static {
    buttonNativeAdDrawable.setShape(GradientDrawable.RECTANGLE);
  }

  public SoundsAdapter(Context context, @Nullable RealmResults<Sound> data) {
    this.context = context;
    this.nativeAdMargin = context.getResources().getDimensionPixelSize(R.dimen.native_ad_margin);
    this.nativeAdCornerRadius = context.getResources().getDimensionPixelSize(R.dimen.native_ad_corner_radius);
    firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    this.data = data;
    buttonNativeAdDrawable.setCornerRadii(new float[] { nativeAdCornerRadius, nativeAdCornerRadius, nativeAdCornerRadius, nativeAdCornerRadius, nativeAdCornerRadius, nativeAdCornerRadius, nativeAdCornerRadius, nativeAdCornerRadius });
    buttonNativeAdDrawable.setColor(ContextCompat.getColor(this.context, R.color.colorPrimary));
  }

  private void requestAd(final int position) {
    if (!requestedAd.contains(position)) {
      requestedAd.add(position);
      AdinCube.Native.load(this.context, 1, new AdinCubeNativeEventListener() {
        @Override
        public void onAdLoaded(List<NativeAd> nativeAdList) {
          final NativeAd nativeAd = nativeAdList.get(0);
          if(nativeAd != null){
            nativeAds.put(position, nativeAd);
            notifyItemChanged(position);
          } else {
            requestedAd.remove(position);
            requestAd(position);
          }
        }
      });
    }
  }

  @Override
  public SoundViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case SOUND:
        return new SoundViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_item, parent, false));
      case NATIVE:
        return new NativeAdViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_item, parent, false));
      default:
        return new SoundViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_item, parent, false));
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (position % 5 == 2) {
      requestAd(position);
      return NATIVE;
    } else {
      return SOUND;
    }
  }

  public void destroy() {
    for (NativeAd nativeAd : nativeAds.values()) {
      AdinCube.Native.destroy(nativeAd);
    }
  }

  public int getRealIndex(int realmPosition){
    return realmPosition + (realmPosition + 2) / 4;
  }

  private int getRealmIndex(int realPosition) {
    return realPosition - (realPosition + 2) / 5;
  }

  @Override
  public void onBindViewHolder(final SoundViewHolder holder, int position) {
    if (position % 5 == 2) {
        bindNativeAdViewHolder(holder, nativeAds.get(position));
    } else {
      bindSoundViewHolder(holder, data.get(getRealmIndex(position)));
    }
  }

  private void bindNativeAdViewHolder(SoundViewHolder holder, NativeAd nativeAd) {
    NativeAdViewHolder nativeAdViewHolder = (NativeAdViewHolder) holder;
    final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) nativeAdViewHolder.root.getLayoutParams();
    if(nativeAd == null) {
      nativeAdViewHolder.itemView.setVisibility(View.GONE);
      layoutParams.topMargin = layoutParams.bottomMargin = 0 ;
      layoutParams.height = 0;
    } else {
      nativeAdViewHolder.itemView.setVisibility(View.VISIBLE);
      layoutParams.topMargin = layoutParams.bottomMargin = nativeAdMargin ;
      layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT;
      nativeAdViewHolder.title.setText(nativeAd.getTitle());
      final String description = nativeAd.getDescription();
      if(TextUtils.isEmpty(description)){
        nativeAdViewHolder.description.setVisibility(View.GONE);
      } else {
        nativeAdViewHolder.description.setText(description);
        nativeAdViewHolder.description.setVisibility(View.VISIBLE);
      }
      final Float rating = nativeAd.getRating();
      if(rating == null){
        nativeAdViewHolder.rating.setVisibility(View.GONE);
      } else {
        nativeAdViewHolder.rating.setVisibility(View.VISIBLE);
        nativeAdViewHolder.rating.setRating(rating);
      }
      nativeAdViewHolder.cta.setText(nativeAd.getCallToAction());
      if(nativeAd.getCover() != null){
        Glide.with(holder.itemView.getContext()).load(nativeAd.getCover().getUrl()).fitCenter().into(nativeAdViewHolder.cover);
      }
      Glide.with(holder.itemView.getContext()).load(nativeAd.getIcon().getUrl()).fitCenter().into(nativeAdViewHolder.icon);
      AdinCube.Native.link(nativeAdViewHolder.root, nativeAd);
    }
  }

  private void bindSoundViewHolder(final SoundViewHolder holder, final Sound sound) {
    holder.myClicks.setText(String.format(Locale.FRENCH, "%d", sound.getPersonalHits()));
    holder.totalClicks.setText(String.format(Locale.FRENCH, "%d", sound.getGlobalHits()));
    holder.id = sound.getId();
    holder.title.setText(sound.getTitle());
    holder.title.setTypeface(Typefaces.GROBOLD.typeface(context));
    if (!TextUtils.isEmpty(sound.getSubtitle())) {
      holder.subtitle.setText(sound.getSubtitle());
      holder.subtitle.setVisibility(View.VISIBLE);
    } else {
      holder.subtitle.setVisibility(View.GONE);
    }
    holder.subtitle.setTypeface(Typefaces.GROBOLD.typeface(context));
    holder.cardView.setCardBackgroundColor(Color.parseColor(sound.getColor()));
    holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(MainActivity.SET_RINGTONE).putExtra(MainActivity.SOUND_PATH, sound.getSound()).putExtra(MainActivity.SOUND_TITLE, sound.getTitle()));
        return true;
      }
    });
    holder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        MainActivity.playSound(view.getContext(), sound.getSound());
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
            holder.totalClicks.setText(String.format(Locale.FRENCH, "%d", currentSound.getGlobalHits()));
            holder.myClicks.setText(String.format(Locale.FRENCH, "%d", currentSound.getPersonalHits()));
          }
        });
        realm.close();
      }
    });
    holder.favorite.setProgress(sound.isFavorite() ? 1f : 0f);
    holder.favorite.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        holder.favorite.cancelAnimation();
        holder.favorite.setProgress(0f);
        final Bundle data = new Bundle();
        data.putString("ID", sound.getId());
        if (!sound.isFavorite()) {
          data.putBoolean("FAV", true);
          holder.favorite.playAnimation();
        } else {
          data.putBoolean("FAV", false);
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
    return data.size() + (data.size() + 1) / 4;
  }

}
