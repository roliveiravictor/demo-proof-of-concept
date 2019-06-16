package com.example.proofofconcept;

import android.os.Bundle;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class BackgroundColorChanger extends AppCompatActivity {

    private RelativeLayout relativeLayout;

    private final int[] colors = {R.color.white,
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.yellow};

    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            final int random = ThreadLocalRandom.current().nextInt(0, colors.length);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    relativeLayout.setBackgroundResource(colors[random]);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_color_changer);
        findViews();
        colorChanger();
    }

    private void findViews() {
        relativeLayout = findViewById(R.id.colorfulLayout);
    }

    private void colorChanger() {
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }
}
