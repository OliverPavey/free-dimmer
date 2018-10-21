package com.github.oliverpavey.freedimmer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {

    final static String DIMMER_ON = "DIMMER_ON";
    final static String DIMMER_TIME = "DIMMER_TIME";
    final static Integer DIMMER_TIME_DEFAULT = 2230;

    static String dimmerTimeToStr(int time) {
        return String.format("%02d:%02d", time / 100, time % 100);
    }
    static int dimmerTime(int hour, int minute) {
        return hour * 100 + minute;
    }
    static int dimmerHour(int time) {
        return time / 100;
    }
    static int dimmerMinute(int time) {
        return time % 100;
    }

    private Toolbar toolbar;
    private Switch switchOnOff;
    private TextView tvDimmerTime;
    private Button changeTime;
    private FloatingActionButton fab;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        switchOnOff = (Switch) findViewById(R.id.switchOnOff);
        tvDimmerTime = (TextView) findViewById(R.id.tvDimmerTime);
        changeTime = (Button) findViewById(R.id.buttonChangeTime);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        pref = this.getPreferences(Context.MODE_PRIVATE);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelp();
            }
        });

        boolean dimmerOn = pref.getBoolean(DIMMER_ON, false);
        int dimmerTime = pref.getInt(DIMMER_TIME, DIMMER_TIME_DEFAULT);
        switchOnOff.setChecked(dimmerOn);
        tvDimmerTime.setText(dimmerTimeToStr(dimmerTime));

        switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableDimmer(isChecked);
            }
        });

        changeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTime();
            }
        });
    }

    private void enableDimmer(boolean enable) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(DIMMER_ON, enable);
        editor.commit();
        if (enable != switchOnOff.isChecked()) {
            switchOnOff.setChecked(enable);
        }
        setupTimer();
    }

    private void changeTime() {
        int dimmerTimeValue = pref.getInt(DIMMER_TIME, DIMMER_TIME_DEFAULT);
        int hour = dimmerHour(dimmerTimeValue);
        int minute = dimmerMinute(dimmerTimeValue);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                int newTime = dimmerTime( selectedHour , selectedMinute );
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(DIMMER_TIME, newTime);
                editor.commit();
                int dimmerTimeValue = pref.getInt(DIMMER_TIME, DIMMER_TIME_DEFAULT);
                tvDimmerTime.setText(dimmerTimeToStr(dimmerTimeValue));
                setupTimer();
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle(getString(R.string.title_set_dimmer_time));
        mTimePicker.show();
    }

    private void showHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mnuEnable) {
            enableDimmer(true);
            return true;
        }
        if (id == R.id.mnuDisable) {
            enableDimmer(false);
            return true;
        }
        if (id == R.id.mnuSetTime) {
            changeTime();
            return true;
        }
        if (id == R.id.mnuHelp) {
            showHelp();
            return true;
        }
        if (id == R.id.mnuExit) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupTimer() {
        boolean dimmerOn = pref.getBoolean(DIMMER_ON, false);
        int dimmerTimeValue = pref.getInt(DIMMER_TIME, DIMMER_TIME_DEFAULT);

        Context context = getApplication().getApplicationContext();
        Intent intent = new Intent(context, DimBrightnessIntent.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(alarmIntent); // Will cancel all alarms with the DimBrightnessIntent.

        if (dimmerOn) {
            if ( !DimBrightnessIntent.checkSystemWritePermission(this) ) {
                enableDimmer(false);
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, dimmerHour(dimmerTimeValue));
                calendar.set(Calendar.MINUTE, dimmerMinute(dimmerTimeValue));

                alarmManager.setRepeating(AlarmManager.RTC,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        alarmIntent);
            }
        }
    }
}
