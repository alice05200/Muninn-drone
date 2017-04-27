package studio.bachelor.muninn;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_view);
        final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Thread thread1 = new Thread(){
            @Override
            public void run() {
                try {
                    ImageView imageView = (ImageView)findViewById(R.id.splash_pic);
                    imageView.startAnimation(animationFadeIn);
                    TextView textView = (TextView)findViewById(R.id.textView);
                    textView.startAnimation(animationFadeIn);
                    /*sleep(1000);
                    textView.setText("Loading.");
                    sleep(1000);
                    textView.setText("Loading..");
                    sleep(1000);
                    textView.setText("Loading...");*/
                    sleep(3000);
                    Intent intent = new Intent(getApplicationContext(), MuninnActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread1.start();
        /*ImageView imageView = (ImageView)findViewById(R.id.splash_pic);
        imageView.startAnimation(animationFadeIn);
        TextView textView = (TextView)findViewById(R.id.textView);
        textView.startAnimation(animationFadeIn);
        ((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MuninnActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ((Button)findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HuginnActivity.class);
                startActivity(intent);
                finish();
            }
        });*/
    }
}
