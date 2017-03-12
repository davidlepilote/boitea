package com.monirapps.boiteabaptiste;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

  public static final String SHOW_SORT_ITEM = "show sort item";
  public static final String HIDE_SORT_ITEM = "hide sort item";
  public static final String SORTING_STYLE = "sorting style";

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if(SHOW_SORT_ITEM.equals(intent.getAction())){
        if (sortItem != null) {
          sortItem.setVisible(true);
        }
      }
      if(HIDE_SORT_ITEM.equals(intent.getAction())){
        if (sortItem != null) {
          sortItem.setVisible(false);
        }
      }
    }
  };

  public static class BoitesPagerAdapter extends FragmentStatePagerAdapter {

    private enum FragmentStyle {
      FAVORITES(0, true, MainActivity.SHOW_SORT_ITEM),
      SOUNDS(1, false, MainActivity.SHOW_SORT_ITEM),
      OTHER_BOXES(2, false, MainActivity.HIDE_SORT_ITEM);

      private static Map<Integer, FragmentStyle> lookup = new HashMap<>();

      static {
        for (FragmentStyle fragmentStyle : FragmentStyle.values()) {
          lookup.put(fragmentStyle.position, fragmentStyle);
        }
      }

      public static FragmentStyle getFromPosition(int position){
        return lookup.get(position);
      }

      final int position;

      final boolean onlyFavorites;

      final String sortItemVisibility;

      FragmentStyle(int position, boolean onlyFavorites, String sortItemVisibility) {
        this.position = position;
        this.onlyFavorites = onlyFavorites;
        this.sortItemVisibility = sortItemVisibility;
      }
    }

    private Context context;

    private static String[] titles = new String[]{"Favoris", "Sons", "Autres boîtes"};

    public BoitesPagerAdapter(FragmentManager fm, Context context) {
      super(fm);
      this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
      final FragmentStyle currentFragmentStyle = FragmentStyle.getFromPosition(position);
      return SoundsFragment.newInstance(currentFragmentStyle.onlyFavorites);
    }

    @Override
    public int getCount() {
      return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return titles[position];
    }
  }

  private static MediaPlayer mediaPlayer = new MediaPlayer();

  private ViewPager pager;

  private TabLayout tabs;

  private View loader;

  private ActionBar actionBar;

  private MenuItem sortItem;

  private Realm realm;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    bindViews();

    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(SHOW_SORT_ITEM);
    intentFilter.addAction(HIDE_SORT_ITEM);
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

    realm = Realm.getDefaultInstance();

    realm.where(Config.class).findAll().addChangeListener(new RealmChangeListener<RealmResults<Config>>() {
      @Override
      public void onChange(RealmResults<Config> element) {
        if (element.size() > 0) {
          final Config config = element.get(0);
          endLoading(config);
        }
      }
    });

    // We make sure here that if there is a config, the loading WILL stop (bug in Realm Listener ?)
    final Config config = realm.where(Config.class).findFirst();
    if(config != null){
      endLoading(config);
    }

    pager.setAdapter(new BoitesPagerAdapter(getSupportFragmentManager(), getApplicationContext()));
    tabs.setupWithViewPager(pager);
    pager.setCurrentItem(1);
    pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        final BoitesPagerAdapter.FragmentStyle currentFragmentStyle = BoitesPagerAdapter.FragmentStyle.getFromPosition(position);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(currentFragmentStyle.sortItemVisibility));
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });

    retrieveConfig();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.toolbar_menu, menu);
    sortItem = menu.findItem(R.id.sort);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.sort:
        showSortDialog();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void showSortDialog() {
    AlertDialog levelDialog;

    final CharSequence[] items = SortStyle.getTitles();

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.sort_by);
    final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    final int chosenStyle = preferences.getInt(SORTING_STYLE, 0);
    builder.setSingleChoiceItems(items, chosenStyle, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int item) {
        final SortStyle sortingStyle = SortStyle.getByPosition(item);
        preferences.edit().putInt(SORTING_STYLE, sortingStyle.position).commit();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(SoundsFragment.REFRESH_LIST));
        dialog.dismiss();
      }
    });
    levelDialog = builder.create();
    levelDialog.show();

  }

  private void endLoading(Config config) {
    loader.setVisibility(View.GONE);
    if (config.getTitle() != null) {
      actionBar.setTitle(config.getTitle());
    }
    for (Sound sound : config.getSounds()) {
      if(sound.isSoundDownloaded() == false){
        BoiteServices.API.downloadSound(getApplicationContext(), sound.getId(), sound.getSound());
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    realm.close();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
  }

  private void bindViews() {
    loader = findViewById(R.id.loader);
    pager = (ViewPager) findViewById(R.id.viewpager);
    tabs = (TabLayout) findViewById(R.id.tabs);
    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    actionBar = getSupportActionBar();
  }

  private void retrieveConfig() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final InputStreamReader reader = new InputStreamReader(getAssets().open("config"), "UTF-8");
          final Config config = new Gson().fromJson(reader, Config.class);
          Realm realm = Realm.getDefaultInstance();
          realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              final Config savedConfig = realm.where(Config.class).findFirst();
              if (savedConfig == null) {
                realm.copyToRealm(config);
              } else {
                if (true || savedConfig.getUpdated() < config.getUpdated()) {
                  savedConfig.updateConfig(getApplicationContext(), config);
                }
              }
            }
          });
          realm.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }).start();
  }

  public static void playBaptiste(Context context, String resource) {
    try {
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = new MediaPlayer();
      AssetFileDescriptor descriptor = context.getAssets().openFd(resource);
      mediaPlayer.setDataSource(new FileInputStream(context.getFilesDir() + "/" + resource).getFD());
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
