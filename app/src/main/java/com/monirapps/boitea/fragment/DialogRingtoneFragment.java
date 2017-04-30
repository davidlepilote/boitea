package com.monirapps.boitea.fragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.os.*;

import com.adincube.sdk.AdinCube;
import com.adincube.sdk.AdinCubeRewardedEventListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.monirapps.boitea.MainActivity;
import com.monirapps.boitea.R;
import com.monirapps.boitea.Typefaces;
import com.monirapps.boitea.adapter.SoundsAdapter;
import com.monirapps.boitea.ws.BoiteServices;

import android.app.*;
import android.content.*;
import android.provider.*;
import android.support.v4.content.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.mateware.snacky.Snacky;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.*;

/**
 * Created by David et Monireh on 22/04/2017.
 */

@RuntimePermissions
public class DialogRingtoneFragment extends DialogFragment {

  private String soundUri;
  private String soundTitle;

  private Button getRingtone;

  private TextView authorization;

  private RadioButton ringtone;
  private RadioButton notification;
  private RadioButton alarm;

  private CheckBox setAsDefault;

  public static final String DIALOG_RINGTONE = "dialog_ringtone";

  private FirebaseAnalytics firebaseAnalytics;

  public static DialogRingtoneFragment instantiate(String soundUri, String soundTitle) {
    DialogRingtoneFragment fragment = new DialogRingtoneFragment();
    fragment.setRetainInstance(true);
    fragment.soundUri = soundUri;
    fragment.soundTitle = soundTitle;
    return fragment;
  }

  /**
   * The system calls this to get the DialogFragment's layout, regardless
   * of whether it's being displayed as a dialog or an embedded fragment.
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout to use as dialog or embedded fragment
    final View root = inflater.inflate(R.layout.dialog_ringtone, container, false);

    firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
    firebaseAnalytics.logEvent("OPEN_SAVE_SOUND", new Bundle());

    getContext().getSharedPreferences(MainActivity.SHARED, Context.MODE_PRIVATE).edit().putBoolean(MainActivity.DID_CLICKED_NOTIF, true).apply();

    findViews(root);

    ringtone.setChecked(true);

    authorization.setVisibility(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ? View.GONE : View.VISIBLE);

    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.RECTANGLE);
    int radius = getContext().getResources().getDimensionPixelSize(R.dimen.native_ad_corner_radius);
    drawable.setCornerRadii(new float[] { radius, radius, radius, radius, radius, radius, radius, radius });
    drawable.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
    getRingtone.setBackgroundDrawable(drawable);
    getRingtone.setTypeface(Typefaces.GROBOLD.typeface(getContext()));
    getRingtone.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogRingtoneFragmentPermissionsDispatcher.displayAdWithCheck(DialogRingtoneFragment.this);
      }
    });

    setAsDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          DialogRingtoneFragmentPermissionsDispatcher.askForDefaultSoundPermissionWithCheck(DialogRingtoneFragment.this);
        }
      }
    });

    return root;
  }

  /**
   * The system calls this only when creating the layout in a dialog.
   */
  @Override
  @NonNull
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // The only reason you might override this method when using onCreateView() is
    // to modify any dialog characteristics. For example, the dialog includes a
    // title by default, but your custom layout might not need it. So here you can
    // remove the dialog title, but you must call the superclass to get the Dialog.
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  private void findViews(View root) {
    getRingtone = (Button) root.findViewById(R.id.get_ringtone);
    ringtone = (RadioButton) root.findViewById(R.id.ringtone);
    notification = (RadioButton) root.findViewById(R.id.notification);
    alarm = (RadioButton) root.findViewById(R.id.alarm);
    setAsDefault = (CheckBox) root.findViewById(R.id.default_button);
    authorization = (TextView) root.findViewById(R.id.authorization);
  }

  private boolean canSaveSound = false;

  @Override
  public void onResume() {
    super.onResume();
    if(canSaveSound) {
      canSaveSound = false;
      DialogRingtoneFragmentPermissionsDispatcher.saveSoundWithCheck(this);
    }
  }

  @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
  void saveSound() {
    final File fileSound = new File(getContext().getFilesDir() + "/" + soundUri);

    final File ringtone = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES) + "/" + soundUri);

    try {
      BoiteServices.copyInputStreamToFile(new FileInputStream(fileSound), ringtone);
    } catch (IOException exception) {
      exception.printStackTrace();
    }

    final boolean ringtoneChecked = DialogRingtoneFragment.this.ringtone.isChecked();
    final boolean notificationChecked = DialogRingtoneFragment.this.notification.isChecked();
    final boolean alarmChecked = DialogRingtoneFragment.this.alarm.isChecked();

    final Bundle data = new Bundle();
    data.putString("TYPE", ringtoneChecked ? "RINGTONE" : notificationChecked ? "NOTIFICATION" : "ALARM");
    firebaseAnalytics.logEvent("SAVE_SOUND", data);

    ContentValues values = new ContentValues();
    values.put(MediaStore.MediaColumns.DATA, ringtone.getAbsolutePath());
    values.put(MediaStore.MediaColumns.TITLE, soundTitle);
    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
    values.put(MediaStore.MediaColumns.SIZE, ringtone.length());
    values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
    values.put(MediaStore.Audio.Media.IS_RINGTONE, ringtoneChecked);
    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, notificationChecked);
    values.put(MediaStore.Audio.Media.IS_ALARM, alarmChecked);
    values.put(MediaStore.Audio.Media.IS_MUSIC, false);

    Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtone
        .getAbsolutePath());
    getContext().getContentResolver().delete(
        uri,
        MediaStore.MediaColumns.DATA + "=\""
            + ringtone.getAbsolutePath() + "\"", null);
    Uri newUri = getContext().getContentResolver().insert(uri, values);

    if (setAsDefault.isChecked()) {
      int soundType = ringtoneChecked ? RingtoneManager.TYPE_RINGTONE : notificationChecked ? RingtoneManager.TYPE_NOTIFICATION : RingtoneManager.TYPE_ALARM;
      DialogRingtoneFragmentPermissionsDispatcher.setDefaultRingtoneWithCheck(DialogRingtoneFragment.this, newUri, soundType);
    }

    dismiss();

    Snacky.builder()
        .setActivty(getActivity())
        .setText(getString(R.string.sound_added))
        .setTextColor(Color.WHITE)
        .setDuration(Snacky.LENGTH_LONG)
        .success()
        .show();
  }

  @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
  void displayAd() {
    if (AdinCube.Rewarded.isReady(getActivity())) {
      AdinCube.Rewarded.show(getActivity());
      AdinCube.Rewarded.setEventListener(new AdinCubeRewardedEventListener() {
        @Override
        public void onAdCompleted() {
          firebaseAnalytics.logEvent("AD_WATCHED", new Bundle());
          canSaveSound = true;
        }
      });
    } else {
      Snacky.builder()
          .setActivty(getActivity())
          .setText(getString(R.string.error_ad))
          .setTextColor(Color.WHITE)
          .setDuration(Snacky.LENGTH_LONG)
          .error()
          .show();
    }


  }

  @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
  void showRationaleForRingtone(final PermissionRequest request) {
    showWarningScackBar(getString(R.string.authorize_write));
    request.proceed();
  }

  @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
  void showDeniedForRingtone() {
    showWarningScackBar(getString(R.string.authorize_write));
  }

  @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
  void showNeverAskForRingtone() {
    Intent intent = new Intent();
    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
    intent.setData(uri);
    startActivity(intent);
    showWarningScackBar(getString(R.string.authorize_write_settings));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    DialogRingtoneFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
  }

  @NeedsPermission(Manifest.permission.WRITE_SETTINGS)
  void askForDefaultSoundPermission() {
  }

  @NeedsPermission(Manifest.permission.WRITE_SETTINGS)
  void setDefaultRingtone(Uri ringtone, @IntRange(from = 0, to = 4) int soundType) {
    try {
      RingtoneManager.setActualDefaultRingtoneUri(
          getContext(), soundType,
          ringtone);
    } catch (Throwable ignored) {
      ignored.printStackTrace();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    DialogRingtoneFragmentPermissionsDispatcher.onActivityResult(this, requestCode);
  }

  private void showWarningScackBar(String text){
    Snacky.builder()
        .setActivty(getActivity())
        .setText(text)
        .setTextColor(Color.WHITE)
        .setDuration(Snacky.LENGTH_LONG)
        .warning()
        .show();
  }

  private void showRationaleForDefault() {
    setAsDefault.setChecked(false);
    showWarningScackBar(getString(R.string.authorize_settings));
  }

  @OnShowRationale(Manifest.permission.WRITE_SETTINGS)
  void defaultRingtoneOnShowRationale(final PermissionRequest request) {
    showRationaleForDefault();
  }

  @OnPermissionDenied(Manifest.permission.WRITE_SETTINGS)
  void defaultRingtoneOnPermissionDenied() {
    showRationaleForDefault();
  }

  @OnNeverAskAgain(Manifest.permission.WRITE_SETTINGS)
  void defaultRingtoneOnNeverAskAgain() {
    showRationaleForDefault();
  }
}
