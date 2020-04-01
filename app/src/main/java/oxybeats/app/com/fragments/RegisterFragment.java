package oxybeats.app.com.fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.Calendar;

import oxybeats.app.com.R;
import oxybeats.app.com.classes.PatientData;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {
    private EditText txtUser;
    private EditText txtPass;
    private EditText txtName;
    private EditText txtBirth;

    private DatePickerDialog picker;

    private Spinner spnrGender;
    private Button btnRegister;

    private FirebaseAuth auth;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.register_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState){
        txtUser = view.findViewById(R.id.txtRegisterUsr);
        txtPass = view.findViewById(R.id.txtRegisterPass);
        txtName = view.findViewById(R.id.txtRegisterName);
        txtBirth = view.findViewById(R.id.txtRegisterBirth);
        btnRegister = view.findViewById(R.id.btnRegister);
        spnrGender = view.findViewById(R.id.spnrRegisterGender);

        txtBirth.setInputType(InputType.TYPE_NULL);
        clearText();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.genderTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrGender.setAdapter(adapter);


        auth = FirebaseAuth.getInstance();

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

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(txtUser.getText())){
                    txtUser.setError("User needed!");
                }else{
                    if(TextUtils.isEmpty(txtPass.getText())){
                        txtPass.setError("Password needed!");
                    }else{
                        if(TextUtils.isEmpty(txtName.getText())){
                            txtName.setError("Name needed!");
                        }else{
                            if(TextUtils.isEmpty(txtBirth.getText())){
                                txtBirth.setError("Birth needed!");
                            }else{
                                if(txtPass.getText().length() < 6){
                                    txtPass.setError("Password lenght must be more than 6 characters");
                                }else{

                                    final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog);
                                    progressDialog.setIndeterminate(true);
                                    progressDialog.setMessage("Creating User...");
                                    progressDialog.show();

                                    final PatientData patient = new PatientData(null, null, null, null, null);
                                    patient.setUser(txtUser.getText().toString());
                                    patient.setPassword(txtPass.getText().toString());
                                    patient.setName(txtName.getText().toString());
                                    patient.setBirth(txtBirth.getText().toString());
                                    patient.setGender(spnrGender.getItemAtPosition(spnrGender.getSelectedItemPosition()).toString());

                                    auth.createUserWithEmailAndPassword(patient.getUser(), patient.getPassword())
                                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    progressDialog.dismiss();

                                                    if(!task.isSuccessful()){
                                                        Toast.makeText(getActivity(), "Creating new account failed!", Toast.LENGTH_SHORT).show();
                                                    }else{

                                                        auth.getCurrentUser().sendEmailVerification();

                                                        Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();

                                                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                                                        DatabaseReference dbRef = db.getReference().child("users");

                                                        dbRef.push().setValue(patient);

                                                        clearText();
                                                        ViewPager view = getActivity().findViewById(R.id.viewPagerLogin);
                                                        view.setCurrentItem(0);

                                                    }
                                                }
                                            });

                                }
                            }
                        }
                    }
                }

            }
        });

    }

    private void clearText(){
        txtUser.setText("");
        txtPass.setText("");
        txtName.setText("");
        txtBirth.setText("");
    }

}
