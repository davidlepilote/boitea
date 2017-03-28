package com.monirapps.boitea;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.monirapps.boitea.bo.Config;
import com.monirapps.boitea.bo.Sound;
import com.monirapps.boitea.bo.SoundBox;
import com.monirapps.boitea.fragment.BoxesFragment;
import com.monirapps.boitea.fragment.SoundsFragment;
import com.monirapps.boitea.ws.BoiteServices;

import java.io.FileInputStream;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

  public static final String SHOW_SORT_ITEM = "show sort item";
  public static final String HIDE_SORT_ITEM = "hide sort item";
  public static final String SORTING_STYLE = "sorting style";
  public static final String REFRESH_CONFIG = "sorting style";
  public static final String SHARED = "SHARED";

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (SHOW_SORT_ITEM.equals(intent.getAction())) {
        if (sortItem != null) {
          sortItem.setVisible(true);
        }
      }
      if (HIDE_SORT_ITEM.equals(intent.getAction())) {
        if (sortItem != null) {
          sortItem.setVisible(false);
        }
      }
      if (REFRESH_CONFIG.equals(intent.getAction())) {
        retrieveConfig();
      }
    }
  };

  @Override
  public void onBackPressed() {
    if (pager.getCurrentItem() == 1) {
      super.onBackPressed();
    } else {
      pager.setCurrentItem(1);
    }
  }

  public static class BoitesPagerAdapter extends FragmentStatePagerAdapter {

    private enum FragmentStyle {
      FAVORITES(true, MainActivity.SHOW_SORT_ITEM, "Favoris"),
      SOUNDS(false, MainActivity.SHOW_SORT_ITEM, "Sons"),
      OTHER_BOXES(false, MainActivity.HIDE_SORT_ITEM, "Autres boîtes");

      final boolean onlyFavorites;

      final String sortItemVisibility;

      final String title;

      FragmentStyle(boolean onlyFavorites, String sortItemVisibility, String title) {
        this.onlyFavorites = onlyFavorites;
        this.sortItemVisibility = sortItemVisibility;
        this.title = title;
      }
    }

    private Context context;

    public BoitesPagerAdapter(FragmentManager fm, Context context) {
      super(fm);
      this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
      if(position == 2){
        return BoxesFragment.newInstance();
      } else {
        return SoundsFragment.newInstance(FragmentStyle.values()[position].onlyFavorites);
      }
    }

    @Override
    public int getCount() {
      return FragmentStyle.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return FragmentStyle.values()[position].title;
    }
  }

  private static MediaPlayer mediaPlayer = new MediaPlayer();

  private ViewPager pager;

  private TabLayout tabs;

  private View loader;

  private ActionBar actionBar;

  private Toolbar toolbar;

  private TextView title;

  private ImageView icon;

  private MenuItem sortItem;

  private Realm realm;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
//    LayoutInflaterCompat.setFactory(getLayoutInflater(), new LayoutInflaterFactory() {
//      @Override
//      public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
//        if("TextView".equals(name)){
//          TextView textView = new TextView(context, attrs);
//          textView.setTypeface(Typefaces.MONTSERRAT.typeface(context));
//          return textView;
//        } else {
//          return null;
//        }
//      }
//    });

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    bindViews();

    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(SHOW_SORT_ITEM);
    intentFilter.addAction(HIDE_SORT_ITEM);
    intentFilter.addAction(REFRESH_CONFIG);
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

    realm = Realm.getDefaultInstance();

    // We make sure here that if there is a config, the loading WILL stop (bug in Realm Listener ?)
    final Config config = realm.where(Config.class).findFirst();
    if (config != null) {
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
        final BoitesPagerAdapter.FragmentStyle currentFragmentStyle = BoitesPagerAdapter.FragmentStyle.values()[position];
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
    AlertDialog sortingDialog;

    final CharSequence[] items = SortStyle.getTitles();

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.sort_by);
    final SharedPreferences preferences = getSharedPreferences(SHARED, MODE_PRIVATE);
    final int chosenStyle = preferences.getInt(SORTING_STYLE, SortStyle.TOTAL_CLICKS.ordinal());
    builder.setSingleChoiceItems(items, chosenStyle, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int item) {
        final SortStyle sortingStyle = SortStyle.getByPosition(item);
        preferences.edit().putInt(SORTING_STYLE, sortingStyle.position).commit();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(SoundsFragment.REFRESH_LIST));
        dialog.dismiss();
      }
    });
    sortingDialog = builder.create();
    sortingDialog.show();
  }

  private void endLoading(Config config) {
    loader.setVisibility(View.GONE);
    if (config != null) {
      title.setText(config.getTitle());
      title.setTypeface(Typefaces.GROBOLD.typeface(getApplicationContext()));
      final int color = Color.parseColor(config.getColor());
      toolbar.setBackgroundColor(color);
      tabs.setTabTextColors(ContextCompat.getColor(getApplicationContext(), R.color.footer_background), color);
      tabs.setSelectedTabIndicatorColor(color);
      BoiteServices.bindPicture(getApplicationContext(), BoiteServices.BASE_URL + config.getIcon(), icon);
      for (Sound sound : config.getSounds()) {
        if (sound.isSoundDownloaded() == false) {
          BoiteServices.API.downloadSound(getApplicationContext(), sound.getId(), sound.getSound());
        }
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
    title = (TextView) findViewById(R.id.title);
    icon = (ImageView) findViewById(R.id.icon);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    actionBar = getSupportActionBar();
    actionBar.setDisplayShowTitleEnabled(false);
  }

  private void retrieveConfig() {
    final SharedPreferences preferences = getSharedPreferences(SHARED, MODE_PRIVATE);
    BoiteApplication.glideUpdateValue++;
    preferences.edit().putInt(BoiteApplication.GLIDE_UPDATE_VALUE, BoiteApplication.glideUpdateValue % 1_000_000).commit();
    retrieveOtherBoxes();
    BoiteServices.API.getConfig().enqueue(new Callback<Config>() {
      @Override
      public void onResponse(Call<Config> call, Response<Config> response) {
        final Config config = response.body();
        if(config != null){
          Realm realm = Realm.getDefaultInstance();
          realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
              Config savedConfig = realm.where(Config.class).findFirst();
              if (savedConfig == null) {
                realm.copyToRealm(config);
              }
              savedConfig = realm.where(Config.class).findFirst();
              savedConfig.updateConfig(getApplicationContext(), config);
              endLoading(savedConfig);
            }
          });
          realm.close();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(SoundsFragment.CONFIG_RETRIEVED));
      }

      @Override
      public void onFailure(Call<Config> call, Throwable t) {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(SoundsFragment.CONFIG_RETRIEVED));
      }
    });
  }

  private void retrieveOtherBoxes(){
    BoiteServices.API.getBoxes().enqueue(new Callback<List<SoundBox>>() {
      @Override
      public void onResponse(Call<List<SoundBox>> call, Response<List<SoundBox>> response) {
        final List<SoundBox> boxes = response.body();
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
          @Override
          public void execute(Realm realm) {
            realm.copyToRealmOrUpdate(boxes);
          }
        });
        realm.close();
      }

      @Override
      public void onFailure(Call<List<SoundBox>> call, Throwable t) {
        t.printStackTrace();
      }
    });
  }

  public static void playSound(Context context, String resource) {
    try {
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setDataSource(new FileInputStream(context.getFilesDir() + "/" + resource).getFD());
      mediaPlayer.prepare();
      mediaPlayer.setVolume(1f, 1f);
      mediaPlayer.setLooping(false);
      mediaPlayer.start();
    } catch (Exception e) {
      e.printStackTrace();

    }
  }

}
