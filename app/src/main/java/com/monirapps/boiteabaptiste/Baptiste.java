package com.monirapps.boiteabaptiste;

import android.support.v7.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David et Monireh on 14/01/2017.
 */

public class Baptiste implements Comparable<Baptiste> {

  @Override
  public int compareTo(Baptiste baptiste) {
    return this.description.compareTo(baptiste.description);
  }

  public enum Person {
    BAPTISTE(R.color.baptiste, "bap"),
    MAMAN(R.color.mere, "mere"),
    FRERE(R.color.frere, "frere");

    public final int color;

    public final String person;

    Person(int color, String person) {
      this.color = color;
      this.person = person;
    }

    private static Map<String, Person> lookup = new HashMap<>();

    static{
      for (Person person : Person.values()) {
        lookup.put(person.person, person);
      }
    }

    public static Person lookup(String person){
      return lookup.get(person);
    }
  }

  public final Person person;

  public final String description;

  public final String pathToSound;

  public Baptiste(Person person, String description, String pathToSound) {
    this.person = person;
    this.description = description;
    this.pathToSound = pathToSound;
  }
}
