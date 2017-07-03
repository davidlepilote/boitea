package com.monirapps.boitea.bo;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by David et Monireh on 03/07/2017.
 */

public class Promo extends RealmObject {

  @PrimaryKey
  private String deeplink;

  private String title;

  private String subtitle;

  private String color;

  private String picture;

  public String getDeeplink() {
    return deeplink;
  }

  public void setDeeplink(String deeplink) {
    this.deeplink = deeplink;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }
}
