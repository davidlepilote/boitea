package com.monirapps.boitea.bo;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.monirapps.boitea.ws.BoiteServices;

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

  private String icon;

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

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  // Must be done inside a transaction
  public void updateConfig(Context context, final Config newConfig) {
    setTitle(newConfig.getTitle());
    setColor(newConfig.getColor());
    setPackageName(newConfig.getPackageName());
    setUpdated(newConfig.getUpdated());
    setIcon(newConfig.getIcon());
    setUrl(newConfig.getUrl());
    Realm realm = Realm.getDefaultInstance();
    for (Sound sound : realm.where(Sound.class).findAll()) {
      sound.setDeleted(true);
    }
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
        oldSound.setDeleted(false);
        oldSound.setGlobalHits(newSound.getGlobalHits());
      } else {
        newSound.setDeleted(false);
        getSounds().add(newSound);
      }
    }
    realm.where(Sound.class).equalTo("deleted", true).findAll().deleteAllFromRealm();
    realm.close();
  }
}
