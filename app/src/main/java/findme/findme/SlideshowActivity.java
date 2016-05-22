package findme.findme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntArrayEvaluator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Interpolator;

public class SlideshowActivity extends AppCompatActivity {

    private View[] logos;
    private static final int logosCount = 5;
    private int currentLogo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        logos = new View[logosCount];
        logos[0] = findViewById(R.id.logo1);
        logos[1] = findViewById(R.id.logo2);
        logos[2] = findViewById(R.id.logo3);
        logos[3] = findViewById(R.id.logo4);
        logos[4] = findViewById(R.id.logo5);

        for(int i = 1; i < logosCount; ++i)
            logos[i].setVisibility(View.GONE);
        crossFade();
    }

    private void crossFade() {
        logos[currentLogo+1].setAlpha(0);
        logos[currentLogo+1].setVisibility(View.VISIBLE);

        logos[currentLogo+1].animate()
                .alpha(1)
                .setDuration(500)
                .setStartDelay(1000)
                .setListener(null);

        logos[currentLogo].animate()
                .alpha(0)
                .setDuration(500)
                .setStartDelay(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        logos[currentLogo].setVisibility(View.GONE);
                        ++currentLogo;
                        if(currentLogo == logosCount-1)
                            lastFade();
                        else
                            crossFade();
                    }
                });
    }

    private void lastFade() {
        logos[currentLogo].animate()
                .alpha(0)
                .setDuration(500)
                .setStartDelay(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        logos[currentLogo].setVisibility(View.GONE);
                        Intent intent = new Intent(SlideshowActivity.this, MapsActivity.class);
                        startActivity(intent);
                    }
                });
    }
}
