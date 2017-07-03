package com.monirapps.boitea.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.monirapps.boitea.MainActivity;
import com.monirapps.boitea.R;
import com.monirapps.boitea.Typefaces;
import com.monirapps.boitea.bo.SoundBox;
import com.monirapps.boitea.ws.BoiteServices;

import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class BoxesAdapter extends RecyclerView.Adapter<BoxesAdapter.SoundBoxViewHolder> {

  public static class SoundBoxViewHolder extends RecyclerView.ViewHolder {

    private CardView cardView;

    private TextView title;

    private TextView subtitle;

    private ImageView icon;

    public SoundBoxViewHolder(View itemView) {
      super(itemView);
      cardView = (CardView) itemView.findViewById(R.id.card_view);
      title = (TextView) itemView.findViewById(R.id.title);
      subtitle = (TextView) itemView.findViewById(R.id.subtitle);
      icon = (ImageView) itemView.findViewById(R.id.icon);
    }
  }

  private static final int SOUND_BOX = 1;

  private static final int FOOTER = 2;

  private final FirebaseAnalytics firebaseAnalytics;

  private final RealmResults<SoundBox> data;

  private final Context context;

  public BoxesAdapter(Context context, RealmResults<SoundBox> data) {
    this.context = context;
    this.data = data;
    firebaseAnalytics = FirebaseAnalytics.getInstance(context);
  }

  private Context getContext() {
    return context;
  }

  @Override
  public SoundBoxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case SOUND_BOX:
        return new SoundBoxViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.box_item, parent, false));
      case FOOTER:
        return new SoundBoxViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_item, parent, false));
      default:
        return new SoundBoxViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.box_item, parent, false));
    }
  }

  @Override
  public int getItemViewType(int position) {
    return position == data.size() ? FOOTER : SOUND_BOX;
  }

  @Override
  public void onBindViewHolder(final SoundBoxViewHolder holder, int position) {
    if (position == data.size()) {
      holder.cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.footer_background));
      holder.cardView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          Intent intent = new Intent(Intent.ACTION_SENDTO);
          intent.setData(Uri.parse(getContext().getString(R.string.mailto)));
          intent.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.mail_subject));
          if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
          }
        }
      });
    } else {
      final SoundBox soundBox = data.get(position);
      try {
        holder.title.setText(soundBox.getTitle());
        holder.title.setTypeface(Typefaces.GROBOLD.typeface(getContext()));
        holder.subtitle.setText(soundBox.getSubtitle());
        holder.subtitle.setTypeface(Typefaces.GROBOLD.typeface(getContext()));
        BoiteServices.bindPicture(getContext(), BoiteServices.BASE_URL + soundBox.getIcon(), holder.icon, soundBox.getUpdated());
        try {
          holder.cardView.setCardBackgroundColor(Color.parseColor(soundBox.getColor()));
        } catch (IllegalArgumentException exception) {
          FirebaseCrash.report(new IllegalArgumentException("Color not parseable : " + soundBox.getColor(), exception));
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(BoiteServices.BASE_URL + soundBox.getDir() + "/index"));
            final Bundle data = new Bundle();
            data.putString("BOX", soundBox.getDir());
            firebaseAnalytics.logEvent("BOX", data);
            getContext().startActivity(intent);
          }
        });
      } catch (NullPointerException exception) {
        FirebaseCrash.report(new NullPointerException("soundbox has a problem of null field : " + soundBox.getPackageName()));
      }
    }
  }

  @Override
  public int getItemCount() {
    return data.size() + 1;
  }
}
