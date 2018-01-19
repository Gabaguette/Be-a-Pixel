package example.gab.beapixel;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.gab.beapixel.R;

import static java.security.AccessController.getContext;

public class Home extends AppCompatActivity {

    ImageView iv1;
    ImageView iv2;
    ScrollView scrollview;
    LinearLayout linearLayout;
    static float scale;
    static int pixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        scale = getApplicationContext().getResources().getDisplayMetrics().density;
        pixels = (int) (70 * scale + 0.5f);
        ////////////////HOME IMAGE/////////////////////
        iv1 = (ImageView) findViewById(R.id.idimagehome);
        iv2 = (ImageView) findViewById(R.id.idlogo);
        countDownTimerHomeActivity();
        ///////////////SCROLL BAR/////////////////////
        scrollview = (ScrollView) findViewById(R.id.ScrollView01);
        linearLayout = (LinearLayout) findViewById(R.id.LinearLayout01);
        creerBouton("Zénith",20,20);

    }

    /////////////BUTTON INITIALISATION/////////////

    public void creerBouton(String nom, final int longueur, final int largeur){
        Button bt = new Button(this);
        bt.setText(nom);
        bt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        bt.setHeight(pixels);
        linearLayout.addView(bt);
        bt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Home.this,ActivityRun.class);
                i.putExtra("longueur",longueur);
                i.putExtra("largeur", largeur);
                startActivity(i);

            }




        });

    }

    //////////////////HOME////////////////////////

    public void countDownTimerHomeActivity() {
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                iv1.setVisibility(ImageView.INVISIBLE);
                iv2.setVisibility(TextView.INVISIBLE);
            }
        }.start();

    }

    //////////////////HANDLE ACTION BARS////////////////////

    public boolean onTouchEvent(MotionEvent event) {
        setImmersiveMode();
        return super.onTouchEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setImmersiveMode();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        setImmersiveMode();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveMode();
        }
    }

    protected void setImmersiveMode() {
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.INVISIBLE);
    }
}
