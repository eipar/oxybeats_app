package oxybeats.app.com.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

import oxybeats.app.com.R;
import oxybeats.app.com.classes.PatientData;

public class SplashActivity extends Activity {
    private ImageView imgHeart;
    private Animation animFirst;
    private Animation animSecond;
    private Animation animThird;
    private Animation animFourth;

    private Handler handler = new Handler();
    private final int[] counter = {0};

    private SharedPreferences shrdPref;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        imgHeart = findViewById(R.id.imgHeart);
        animFirst = AnimationUtils.loadAnimation(getBaseContext(), R.anim.scale_up);
        animSecond = AnimationUtils.loadAnimation(getBaseContext(), R.anim.scale_down);
        animThird = AnimationUtils.loadAnimation(getBaseContext(), R.anim.scale_up);
        animFourth = AnimationUtils.loadAnimation(getBaseContext(), R.anim.scale_down);

        imgHeart.setImageResource(R.drawable.heart_256);
        imgHeart.startAnimation(animFirst);

        animFirst.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imgHeart.setAnimation(animSecond);
                imgHeart.startAnimation(animSecond);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                imgHeart.setAnimation(animSecond);
                imgHeart.startAnimation(animSecond);
            }
        });

        animSecond.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imgHeart.setAnimation(animThird);
                imgHeart.startAnimation(animThird);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                imgHeart.setAnimation(animThird);
                imgHeart.startAnimation(animThird);
            }
        });

        animThird.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                counter[0]++;
                imgHeart.setAnimation(animFourth);
                imgHeart.startAnimation(animFourth);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                imgHeart.setAnimation(animFourth);
                imgHeart.startAnimation(animFourth);
            }
        });

        animFourth.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                repeatAnim();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                repeatAnim();
            }
        });

    }

    public void repeatAnim(){
        Timer timer = new Timer();
        TimerTask mTimerTask = new TimerTask(){
            public void run(){
                handler.post(new Runnable(){
                    public void run(){
                        repeatAnimation();
                    }
                });
            }
        };
        timer.schedule(mTimerTask, 300);
    }

    public void repeatAnimation(){
        if(counter[0] > 1){
            counter[0] = 0;

            //LoginAsync asyncTask = new LoginAsync();
            //asyncTask.execute();

            auth = FirebaseAuth.getInstance();
            shrdPref = PreferenceManager.getDefaultSharedPreferences(this);

            if(checkSharedPreferences()){
                if(auth.getCurrentUser() != null){
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                    String email = auth.getCurrentUser().getEmail();
                    Query query = myRef.child("users").orderByChild("user").equalTo(email);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            PatientData patient = new PatientData();
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                patient = snapshot.getValue(PatientData.class);
                            }
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            intent.putExtra("PatientData", patient);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("TAGLOG", "Error query Login", databaseError.toException());
                        }
                    });
                }
            }else{
                Intent i = new Intent(getBaseContext(),LoginActivity.class);
                startActivity(i);
                finish();
            }

        }else{
            imgHeart.clearAnimation();
            imgHeart.setAnimation(animFirst);
            imgHeart.startAnimation(animFirst);
        }

    }

    private boolean checkSharedPreferences(){
        String autoLogin = shrdPref.getString("logRemember", "false");
        boolean aux = false;

        if(autoLogin.equals("true")){
            aux = true;
        }

        return aux;
    }


}
