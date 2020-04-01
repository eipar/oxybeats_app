package oxybeats.app.com.fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import oxybeats.app.com.R;
import oxybeats.app.com.activities.LoginActivity;
import oxybeats.app.com.activities.MainActivity;
import oxybeats.app.com.classes.PatientData;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    private PatientData data;

    private EditText txtMail;
    private EditText txtPass;
    private EditText txtName;
    private EditText txtBirth;
    private TextView txtNameView;

    private Spinner spnrGender;

    private DatePickerDialog picker;

    private FloatingActionButton fabEdit;
    private boolean fabAction = true;

    ArrayAdapter<CharSequence> adapter;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(getArguments() != null){
            data = getArguments().getParcelable("patientData");
        }
        ((MainActivity)getActivity()).setBottomNavigation(true);
        ((MainActivity)getActivity()).setHomeEnabled(false);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        txtMail = view.findViewById(R.id.txtAccountUsername);
        txtPass = view.findViewById(R.id.txtAccountPassword);
        txtName = view.findViewById(R.id.txtAccountName);
        txtBirth = view.findViewById(R.id.txtAccountBirthday);
        txtNameView = view.findViewById(R.id.txtAccountNameView);
        spnrGender = view.findViewById(R.id.spnrAccountGender);
        fabEdit = view.findViewById(R.id.fabAccountModify);

        txtBirth.setInputType(InputType.TYPE_NULL);

        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.genderTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrGender.setAdapter(adapter);

        updateData();

        txtMail.setEnabled(false);
        txtPass.setEnabled(false);
        txtName.setEnabled(false);
        txtBirth.setEnabled(false);
        spnrGender.setEnabled(false);

        txtBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                picker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        txtBirth.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fabAction){
                    //lapiz -> ok
                    //Quiere editar
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setMessage("Do you want to edit you information?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fabEdit.setImageResource(R.drawable.ic_ok);
                                    txtMail.setEnabled(true);
                                    txtPass.setEnabled(true);
                                    txtName.setEnabled(true);
                                    txtBirth.setEnabled(true);
                                    spnrGender.setEnabled(true);
                                    fabAction = false;
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fabEdit.setImageResource(R.drawable.ic_edit);
                                    txtMail.setEnabled(false);
                                    txtPass.setEnabled(false);
                                    txtName.setEnabled(false);
                                    txtBirth.setEnabled(false);
                                    spnrGender.setEnabled(false);
                                    fabAction = true;
                                }
                            });
                    alert.show();


                }else{

                    if(!textCheckEmpty()){
                        final String[] errorMsg = {"Error with"};
                        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Updating...");
                        progressDialog.show();

                        String aux = data.getUser();
                        if(!aux.equals(txtMail.getText().toString())){
                            //Cambio el mail

                            final String mail = txtMail.getText().toString();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            user.updateEmail(mail)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                                                String aux = data.getUser();
                                                Query query = myRef.child("users").orderByChild("user").equalTo(aux.toLowerCase());

                                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        String key = new String();
                                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                                            key = snapshot.getKey();
                                                        }
                                                        myRef.child("users").child(key).child("user").setValue(mail);
                                                        data.setUser(mail);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Log.e("TAGLOG", "Error query Login", databaseError.toException());
                                                        errorMsg[0] = errorMsg[0] + " mail";
                                                    }
                                                });

                                            }else{
                                                Log.d("TAGLOG", "Could NOT Update mail");
                                                errorMsg[0] = errorMsg[0] + " mail";
                                            }
                                        }
                                    });

                        }

                        aux = data.getPassword();
                        if(!aux.equals(txtPass.getText().toString())){
                            //Cambio la pass
                            final String pass = txtPass.getText().toString();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            user.updatePassword(pass)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                                                String aux = data.getUser();
                                                Query query = myRef.child("users").orderByChild("user").equalTo(aux.toLowerCase());

                                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        String key = new String();
                                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                                            key = snapshot.getKey();
                                                        }
                                                        myRef.child("users").child(key).child("password").setValue(pass);
                                                        data.setPassword(pass);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Log.e("TAGLOG", "Could NOT Update pass", databaseError.toException());
                                                        errorMsg[0] = errorMsg[0] + " password";
                                                    }
                                                });

                                            }else{
                                                Log.d("TAGLOG", "Could NOT Update pass");
                                                errorMsg[0] = errorMsg[0] + " password";
                                            }
                                        }
                                    });
                        }

                        final String gender = spnrGender.getItemAtPosition(spnrGender.getSelectedItemPosition()).toString();
                        if(!data.getGender().equals(gender)){
                            //cambio genero
                            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                            String auxUsr = data.getUser();
                            Query query = myRef.child("users").orderByChild("user").equalTo(auxUsr.toLowerCase());

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String key = new String();
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        key = snapshot.getKey();
                                    }
                                    myRef.child("users").child(key).child("gender").setValue(gender);
                                    data.setGender(gender);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("TAGLOG", "Could NOT Update gender", databaseError.toException());
                                    errorMsg[0] = errorMsg[0] + " gender";
                                }
                            });
                        }

                        final String name = txtName.getText().toString();
                        if(!data.getName().equals(name)){
                            //cambio genero
                            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                            String auxUsr = data.getUser();
                            Query query = myRef.child("users").orderByChild("user").equalTo(auxUsr.toLowerCase());

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String key = new String();
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        key = snapshot.getKey();
                                    }
                                    myRef.child("users").child(key).child("name").setValue(name);
                                    data.setName(name);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("TAGLOG", "Could NOT Update name", databaseError.toException());
                                    errorMsg[0] = errorMsg[0] + " name";
                                }
                            });
                        }

                        final String birth = txtBirth.getText().toString();
                        if(!data.getBirth().equals(birth)){
                            //cambio genero
                            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                            String auxUsr = data.getUser();
                            Query query = myRef.child("users").orderByChild("user").equalTo(auxUsr.toLowerCase());

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String key = new String();
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        key = snapshot.getKey();
                                    }
                                    myRef.child("users").child(key).child("birth").setValue(birth);
                                    data.setBirth(birth);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("TAGLOG", "Could NOT Update birth", databaseError.toException());
                                    errorMsg[0] = errorMsg[0] + " birthday";
                                }
                            });
                        }

                        if(errorMsg[0].length() > 10){
                            Snackbar snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), errorMsg[0], Snackbar.LENGTH_LONG);
                            TextView textsnack = snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textsnack.setTextColor(Color.RED);
                            textsnack.setTextSize(18);
                            snackBar.show();
                        }else{
                            Snackbar snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Profile Updated", Snackbar.LENGTH_LONG);
                            TextView textsnack = snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textsnack.setTextColor(Color.GREEN);
                            textsnack.setTextSize(18);
                            textsnack.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            snackBar.show();

                            //Termino de editar
                            fabEdit.setImageResource(R.drawable.ic_edit);
                            txtMail.setEnabled(false);
                            txtPass.setEnabled(false);
                            txtName.setEnabled(false);
                            txtBirth.setEnabled(false);
                            spnrGender.setEnabled(false);
                            fabAction = true;
                        }

                        progressDialog.dismiss();
                        updateData();
                    }

                }


            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private boolean textCheckEmpty(){
        boolean fin = false;

        if(TextUtils.isEmpty(txtMail.getText())){
            fin = true;
            txtMail.setError("Mail needed");
        }else{
            if(TextUtils.isEmpty(txtPass.getText())){
                fin = true;
                txtPass.setError("Password needed");
            }else{
                if(TextUtils.isEmpty(txtName.getText())){
                    fin = true;
                    txtName.setError("Name neeeded");
                }else{
                    if(TextUtils.isEmpty(txtBirth.getText())){
                        fin = true;
                        txtBirth.setError("Birthday needed");
                    }
                }
            }
        }

        return fin;
    }

    private void updateData(){
        txtMail.setText(data.getUser());
        txtPass.setText(data.getPassword());
        txtName.setText(data.getName());
        txtNameView.setText(data.getName());
        txtBirth.setText(data.getBirth());
        spnrGender.setSelection(adapter.getPosition(data.getGender()));
    }

}
