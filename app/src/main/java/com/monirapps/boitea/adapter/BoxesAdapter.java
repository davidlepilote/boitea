package com.monirapps.boitea.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.crash.FirebaseCrash;
import com.monirapps.boiteabaptiste.R;
import com.monirapps.boitea.Typefaces;
import com.monirapps.boitea.bo.SoundBox;
import com.monirapps.boitea.ws.BoiteServices;

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

  @Override
  public SoundBoxViewHolder onCreateRealmViewHolder(ViewGroup parent, int viewType) {
    return new SoundBoxViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.box_item, parent, false));
  }

  @Override
  public void onBindRealmViewHolder(final SoundBoxViewHolder holder, final int position) {
    final SoundBox soundBox = data.get(position);
    try {
      holder.title.setText(soundBox.getTitle());
      holder.title.setTypeface(Typefaces.GROBOLD.typeface(getContext()));
      holder.subtitle.setText(soundBox.getSubtitle());
      holder.subtitle.setTypeface(Typefaces.GROBOLD.typeface(getContext()));
      Glide.with(getContext()).load(BoiteServices.BASE_URL + soundBox.getIcon()).asBitmap().centerCrop().into(new BitmapImageViewTarget(holder.icon) {
        @Override
        protected void setResource(Bitmap resource) {
          RoundedBitmapDrawable circularBitmapDrawable =
              RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
          circularBitmapDrawable.setCircular(true);
          holder.icon.setImageDrawable(circularBitmapDrawable);
        }
      });
      try {
        holder.cardView.setCardBackgroundColor(Color.parseColor(soundBox.getColor()));
      } catch (IllegalArgumentException exception) {
        FirebaseCrash.report(new IllegalArgumentException("Color not parseable : " + soundBox.getColor(), exception));
      }
      holder.cardView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse("market://details?id=" + soundBox.getPackageName()));
          getContext().startActivity(intent);
        }
      });
    } catch (NullPointerException exception) {
      FirebaseCrash.report(new NullPointerException("soundbox has a problem of null field : " + soundBox.getPackageName()));
    }
  }

  @Override
  public SoundBoxViewHolder onCreateFooterViewHolder(ViewGroup parent) {
    return new SoundBoxViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_item, parent, false));
  }

  @Override
  public void onBindFooterViewHolder(SoundBoxViewHolder holder, int position) {
    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.footer_background));
    holder.cardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:boxes@monirapps.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion de boîte");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
          getContext().startActivity(intent);
        }
      }
    });
  }
}
