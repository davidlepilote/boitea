package com.monirapps.boitea;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by David et Monireh on 13/03/2017.
 */

public enum Typefaces {

  GROBOLD("GROBOLD.ttf"),
  MONTSERRAT("Montserrat-Regular.otf");

  private final String typefaceName;

  private Typeface typeface;

  public Typeface typeface(Context context){
    if(typeface == null){
      typeface = Typeface.createFromAsset(context.getAssets(), typefaceName);
    }
    return typeface;
  }

  Typefaces(String typefaceName) {
    this.typefaceName = typefaceName;
  }
}
