package com.monirapps.boiteabaptiste;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.monirapps.boiteabaptiste.ws.BoiteServices;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by David et Monireh on 11/03/2017.
 */

public class Config extends RealmObject {

  private String title;

  private String color;

  @SerializedName("package")
  private String packageName;

  private long updated;

  private String url;

  private RealmList<Sound> sounds;

  public String getTitle() {
    return title;
  }

  public String getPackageName() {
    return packageName;
  }

  public long getUpdated() {
    return updated;
  }

  public String getUrl() {
    return url;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public RealmList<Sound> getSounds() {
    return sounds;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setUpdated(long updated) {
    this.updated = updated;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void updateConfig(Context context, final Config newConfig) {
    setTitle(newConfig.getTitle());
    setColor(newConfig.getColor());
    setPackageName(newConfig.getPackageName());
    setUpdated(newConfig.getUpdated());
    setUrl(newConfig.getUrl());
    Realm realm = Realm.getDefaultInstance();
    for (Sound newSound : newConfig.getSounds()) {
      final Sound oldSound = realm.where(Sound.class).equalTo("id", newSound.getId()).findFirst();
      if (oldSound != null) {
        if (oldSound.getUpdated() < newSound.getUpdated()) {
          oldSound.setUpdated(newSound.getUpdated());
          oldSound.setTitle(newSound.getTitle());
          oldSound.setColor(newSound.getColor());
          oldSound.setSubtitle(newSound.getSubtitle());
          oldSound.setSound(newSound.getSound());
          oldSound.setSoundDownloaded(false);
          BoiteServices.API.downloadSound(context, oldSound.getId(), oldSound.getSound());
        }
        oldSound.setGlobalHits(newSound.getGlobalHits());
      } else {
        getSounds().add(newSound);
      }
    }
    realm.close();
  }
}
