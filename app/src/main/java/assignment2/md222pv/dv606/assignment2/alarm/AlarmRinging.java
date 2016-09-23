package assignment2.md222pv.dv606.assignment2.alarm;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import assignment2.md222pv.dv606.assignment2.R;


public class AlarmRinging extends AppCompatActivity {

    private View mContentView;
    Ringtone ringtone;
    private TextView alarmV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_alarm_ringing);
        alarmV = (TextView)findViewById(R.id.aTime);

        alarmV.setText("Alarm time: " + getIntent().getStringExtra("alarmTime"));

        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeAlarm();
            }
        });

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();

        if (Alarm.onOff != null) {
            Alarm.onOff.setChecked(false);
        }
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void closeAlarm() {
        ringtone.stop();
        this.finish();
    }
}
