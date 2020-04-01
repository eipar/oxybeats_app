package oxybeats.app.com.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import oxybeats.app.com.R;
import oxybeats.app.com.adapters.LoginAdapter;
import oxybeats.app.com.classes.PatientData;
import oxybeats.app.com.fragments.LoginFragment;
import oxybeats.app.com.fragments.RegisterFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        ViewPager viewPager = findViewById(R.id.viewPagerLogin);

        LoginAdapter adapter = new LoginAdapter(getSupportFragmentManager());
        adapter.addFragment(new LoginFragment());
        adapter.addFragment(new RegisterFragment());

        viewPager.setAdapter(adapter);

    }

}
