package oxybeats.app.com.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import oxybeats.app.com.R;
import oxybeats.app.com.activities.MainActivity;
import oxybeats.app.com.classes.PatientData;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private EditText txtUsr;
    private EditText txtPass;
    private Button btnLog;
    private Button btnForgot;
    private CheckBox checkRemember;

    private FirebaseAuth auth;

    private SharedPreferences shrdPref;
    private SharedPreferences.Editor editPref;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        txtUsr = view.findViewById(R.id.txtLoginUsr);
        txtPass = view.findViewById(R.id.txtLoginPass);
        btnLog = view.findViewById(R.id.btnLogin);
        btnForgot = view.findViewById(R.id.btnLoginForgot);
        checkRemember = view.findViewById(R.id.checkLoginRemember);

        auth = FirebaseAuth.getInstance();

        shrdPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editPref = shrdPref.edit();

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = txtUsr.getText().toString();
                String pass = txtPass.getText().toString();

                if(TextUtils.isEmpty(email)){
                    txtUsr.setError("User needed!");
                }else {
                    if (TextUtils.isEmpty(pass)) {
                        txtPass.setError("Pass needed!");
                    } else {

                        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog);
                        progressDialog.setIndeterminate(false);
                        progressDialog.setMessage("Authenticating...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if(!task.isSuccessful()){
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Authentication failed!", Toast.LENGTH_SHORT).show();
                                        }else{

                                            auth.getCurrentUser().reload();
                                            final FirebaseUser usr = auth.getCurrentUser();

                                            if(usr.isEmailVerified()){

                                                if(checkRemember.isChecked()){
                                                    editPref.putString("logRemember", "true");
                                                    editPref.commit();
                                                }

                                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                                                Query query = myRef.child("users").orderByChild("user").equalTo(email.toLowerCase());

                                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        progressDialog.dismiss();

                                                        PatientData patient = new PatientData();
                                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                                            patient = snapshot.getValue(PatientData.class);
                                                        }
                                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                                        intent.putExtra("PatientData", patient);
                                                        startActivity(intent);
                                                        getActivity().finish();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Log.e("TAGLOG", "Error query Login", databaseError.toException());
                                                        Toast.makeText(getActivity(), "Error query", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }else{
                                                Toast.makeText(getActivity(), "User not verified", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                                usr.sendEmailVerification();
                                            }


                                        }
                                    }
                                });
                    }
                }

            }
        });

        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to reset password?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final AlertDialog.Builder buildMail = new AlertDialog.Builder(getActivity());
                                buildMail.setTitle("Enter your mail here:");

                                final EditText input = new EditText(getActivity());
                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                buildMail.setView(input);

                                buildMail.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog);
                                                progressDialog.setIndeterminate(true);
                                                progressDialog.setMessage("Sending Mail...");
                                                progressDialog.show();

                                                auth.sendPasswordResetEmail(input.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                progressDialog.dismiss();
                                                                if(!task.isSuccessful()){
                                                                    Toast.makeText(getActivity(), "Failed to reset Password", Toast.LENGTH_SHORT).show();
                                                                }else{
                                                                    Toast.makeText(getActivity(), "Go check your mail!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                buildMail.show();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();
            }
        });
    }

}
