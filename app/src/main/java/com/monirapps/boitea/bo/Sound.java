package com.monirapps.boitea.bo;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by David et Monireh on 14/01/2017.
 */

public class Sound extends RealmObject implements Comparable<Sound> {

  @PrimaryKey
  private String id;

  private long updated;

  private String title;

  private String subtitle;

  private String color;

  private String sound;

  private int globalHits;

  private int personalHits;

  private boolean favorite;

  private boolean soundDownloaded;

  public String getId() {
    return id;
  }

  public long getUpdated() {
    return updated;
  }

  public String getTitle() {
    return title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getColor() {
    return color;
  }

  public String getSound() {
    return sound;
  }

  public int getGlobalHits() {
    return globalHits;
  }

  public int getPersonalHits() {
    return personalHits;
  }

  public void setGlobalHits(int globalHits) {
    this.globalHits = globalHits;
  }

  public void setPersonalHits(int personalHits) {
    this.personalHits = personalHits;
  }

  public boolean isFavorite() {
    return favorite;
  }

  public void setFavorite(boolean favorite) {
    this.favorite = favorite;
  }

  public void setUpdated(long updated) {
    this.updated = updated;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public void setSound(String sound) {
    this.sound = sound;
  }

  public boolean isSoundDownloaded() {
    return soundDownloaded;
  }

  public void setSoundDownloaded(boolean soundDownloaded) {
    this.soundDownloaded = soundDownloaded;
  }

  @Override
  public int compareTo(Sound sound) {
    return Long.valueOf(this.id).compareTo(Long.valueOf(sound.id));
  }
}
