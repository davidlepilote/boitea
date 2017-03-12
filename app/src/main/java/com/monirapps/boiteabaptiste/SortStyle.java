package com.monirapps.boiteabaptiste;

import java.util.HashMap;
import java.util.Map;

import io.realm.Sort;

/**
 * Created by David et Monireh on 12/03/2017.
 */

public enum SortStyle {
  ALPHA(0, "Ordre alphabétique", "title", Sort.ASCENDING),
  TOTAL_CLICKS(1, "Clics totaux", "globalHits", Sort.DESCENDING),
  MY_CLICKS(2, "Mes clics", "personalHits", Sort.DESCENDING);

  private static Map<Integer, SortStyle> lookup = new HashMap<>();

  static {
    for (SortStyle sortStyle : SortStyle.values()) {
      lookup.put(sortStyle.position, sortStyle);
    }
  }

  public static String[] getTitles(){
    String[] titles = new String[SortStyle.values().length];
    for (int i = 0; i < SortStyle.values().length; i++) {
      titles[i] = SortStyle.values()[i].title;
    }
    return titles;
  }

  public static SortStyle getByPosition(int position){
    return lookup.get(position);
  }

  public final int position;

  public final String title;

  public final String sortingField;

  public final Sort order;

  SortStyle(int position, String title, String sortingField, Sort order) {
    this.position = position;
    this.title = title;
    this.sortingField = sortingField;
    this.order = order;
  }
}
