package com.socialherb.openeyes;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class SetupActivity extends Activity {
    private static final String TAG = "SetupActivity";
    TextView alarm_point_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        Button apply_btn = (Button)findViewById(R.id.apply_btn);
        Button close_btn = (Button)findViewById(R.id.close_btn);
        alarm_point_txt= (TextView)findViewById(R.id.alarmpoint_txt);
        SeekBar eyes_seekbar = (SeekBar)findViewById(R.id.eyes_seekbar);

        eyes_seekbar.setProgress(Constants.ALARM_POINT);
        alarm_point_txt.setText(Constants.ALARM_POINT+"");

        close_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                try {
                    finish();
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        apply_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                try {
                    SaveData.getInstance(getApplicationContext()).setAlarmPoint(Constants.ALARM_POINT);

                    //Common.showSnackBar(findViewById(R.id.activity_setup_layout), getString(R.string.apply_msg));

                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        eyes_seekbar.incrementProgressBy(1);

        SeekBar.OnSeekBarChangeListener SeekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //add code here
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //add code here
            }

            @Override
            public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
                //add code here
                //Log.w(TAG, "eyes_degree -> " + progress);
                Constants.ALARM_POINT=progress;
                alarm_point_txt.setText(Constants.ALARM_POINT+"");
            }
        };

        eyes_seekbar.setOnSeekBarChangeListener(SeekBarListener);

    }
}

