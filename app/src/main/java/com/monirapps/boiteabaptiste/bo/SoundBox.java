package com.monirapps.boiteabaptiste.bo;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by David et Monireh on 12/03/2017.
 */

public class SoundBox extends RealmObject {

  private String title;

  private String color;

  @PrimaryKey
  @SerializedName("package")
  private String packageName;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getPackageName() {
    return packageName;
  }
}
