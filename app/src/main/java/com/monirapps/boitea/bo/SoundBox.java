package com.monirapps.boitea.bo;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by David et Monireh on 12/03/2017.
 */

public class SoundBox extends RealmObject {

  private String title;

  private String subtitle;

  private String color;

  @PrimaryKey
  @SerializedName("package")
  private String packageName;

  private long updated;

  private String icon;

  private boolean banned;

  private boolean validated;

  public String getTitle() {
    return title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getColor() {
    return color;
  }

  public String getPackageName() {
    return packageName;
  }

  public long getUpdated() {
    return updated;
  }

  public String getIcon() {
    return icon;
  }

  public boolean isBanned() {
    return banned;
  }

  public boolean isValidated() {
    return validated;
  }
}
