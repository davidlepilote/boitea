package com.monirapps.boiteabaptiste;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private RecyclerView baptistes;

  private RecyclerView.Adapter baptistesAdapter;

  private static MediaPlayer mediaPlayer = new MediaPlayer();

  public static class BaptisteHolder extends RecyclerView.ViewHolder {

    private CardView cardView;

    private TextView description;

    public BaptisteHolder(View itemView) {
      super(itemView);
      cardView = (CardView) itemView.findViewById(R.id.card_view);
      description = (TextView) itemView.findViewById(R.id.description);
    }
  }

  public static class BaptisteAdapter extends RecyclerView.Adapter<BaptisteHolder> {

    private final List<Baptiste> baptistes;

    public BaptisteAdapter(List<Baptiste> baptistes) {
      this.baptistes = baptistes;
    }

    @Override
    public BaptisteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new BaptisteHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.baptiste_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final BaptisteHolder holder, int position) {
      final Baptiste baptiste = baptistes.get(position);
      holder.description.setText(baptiste.description);
      holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), baptiste.person.color));
      holder.cardView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          MainActivity.playBaptiste(view.getContext(), baptiste.pathToSound);
        }
      });
    }

    @Override
    public int getItemCount() {
      return baptistes.size();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportActionBar().setTitle("La boîte à Baptiste");

    baptistes = (RecyclerView) findViewById(R.id.baptistes);

    List<Baptiste> baptisteList = getBaptistes();

    baptistes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    baptistesAdapter = new BaptisteAdapter(baptisteList);
    baptistes.setAdapter(baptistesAdapter);
  }

  private List<Baptiste> getBaptistes() {
    List<Baptiste> baptistes = new ArrayList<>();
    BufferedReader reader;

    try {
      final InputStream file = getAssets().open("boite_a_baptiste.txt");
      reader = new BufferedReader(new InputStreamReader(file));
      String line = reader.readLine();
      while (line != null) {
        final String[] elements = line.split(":");
        baptistes.add(new Baptiste(Baptiste.Person.lookup(elements[0]), elements[1], elements[2]));
        line = reader.readLine();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    Collections.sort(baptistes);
    return baptistes;
  }

  public static void playBaptiste(Context context, String resource) {
    try {
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = new MediaPlayer();
      AssetFileDescriptor descriptor = context.getAssets().openFd(resource);
      mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
      descriptor.close();

      mediaPlayer.prepare();
      mediaPlayer.setVolume(1f, 1f);
      mediaPlayer.setLooping(false);
      mediaPlayer.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
