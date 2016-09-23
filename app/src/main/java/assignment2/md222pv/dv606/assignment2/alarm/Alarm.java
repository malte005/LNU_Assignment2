package assignment2.md222pv.dv606.assignment2.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import assignment2.md222pv.dv606.assignment2.R;

public class Alarm extends AppCompatActivity {

    private Calendar calendar = Calendar.getInstance();
    private TextView tTime, tAlarmTime;
    public static Switch onOff;

    private SharedPreferences prefs;
    private PendingIntent notifyIntent;

    private TimePickerDialog.OnTimeSetListener alarmSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            updateAlarmTextView();
            onOff.setChecked(false);
            onOff.setChecked(true);

            //save if alarm is checked or not
            setBooleanSwitch2Pref(true);
            setStringTime2Pref(calendar.get(calendar.HOUR_OF_DAY) + ":" + calendar.get(calendar.MINUTE));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        tAlarmTime = (TextView) findViewById(R.id.alarm_time);
        tTime = (TextView) findViewById(R.id.time);
        onOff = (Switch) findViewById(R.id.switch_alarm);

        getSettetAlarm();

        tAlarmTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new TimePickerDialog(
                        Alarm.this,
                        alarmSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                ).show();
            }
        });

        new Thread(setTime).start();

        if (onOff != null) {
            onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton btnView, boolean alarmOn) {
                    AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent intent = new Intent(Alarm.this, AlarmRinging.class);
                    DateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                    intent.putExtra("alarmTime", df.format(calendar.getTime()));
                    notifyIntent = PendingIntent.getActivity(Alarm.this, 1, intent, 0);
                    if (alarmOn) {

                        tAlarmTime.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorMenu));
                        manager.cancel(notifyIntent);

                        manager.set(AlarmManager.RTC_WAKEUP, setAlarmTime(), notifyIntent);

                        setBooleanSwitch2Pref(true);
                    } else {
                        //turn off alarm
                        manager.cancel(notifyIntent);

                        tAlarmTime.setTextColor(Color.LTGRAY);
                        //delete alarm in settings
                        setBooleanSwitch2Pref(false);
                    }
                }
            });
        }
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        getSettetAlarm();
    }

    private long setAlarmTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String alarmTime = sdf.format(date).concat("" + tAlarmTime.getText());

        Calendar c = new GregorianCalendar();

        if(calendar.getTimeInMillis() < c.getTimeInMillis()){
            calendar.add(Calendar.DATE,1);
        }

        System.out.println(":::: " + calendar.getTime());
        System.out.println(":::: " + c.getTime());


        try {
            date = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH).parse(alarmTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();
    }

    // TIME THREAD
    private Runnable setTime = new Runnable() {
        DateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

        public void run() {
            while (true)
                try {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            tTime.setText(df.format(Calendar.getInstance().getTime()));
                        }
                    });
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    };

    private void updateAlarmTextView() {
        DateFormat dateFormatHm = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        tAlarmTime.setText(dateFormatHm.format(calendar.getTime()));
    }

    /**
     *
     */
    private void setAlarm2CurrentTime() {
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        tAlarmTime.setText(df.format(cal.getTime()));
    }

    /**
     *
     */
    private void getSettetAlarm() {
        if (getBooleanSwitchFromPref()) {
            onOff.setChecked(true);
        }

        if (getStringTimeFromPref() != null) {
            tAlarmTime.setText(getStringTimeFromPref());
        } else {
            setAlarm2CurrentTime();
        }
    }

    private String getStringTimeFromPref() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String alarm = prefs.getString("alarmTime", "12:00");
        return alarm;
    }

    private void setStringTime2Pref(String time) {
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString("alarmTime", time);
        prefEditor.apply();
    }

    private void setBooleanSwitch2Pref(boolean active) {
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean("alarmSwitch", active);
        prefEditor.apply();
    }

    private boolean getBooleanSwitchFromPref() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean alarm = prefs.getBoolean("alarmSwitch", false);
        return alarm;
    }
}