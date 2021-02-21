package com.custom.co2.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.custom.co2.R;
import com.custom.co2.utils.Constant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.custom.co2.utils.Constant.getShaedPref;
import static com.custom.co2.utils.Constant.setShaedPref;
import static com.custom.co2.utils.Constant.showProgressDialog;
import static com.custom.co2.utils.Constant.spConatact;
import static com.custom.co2.utils.Constant.spDob;
import static com.custom.co2.utils.Constant.spEmail;
import static com.custom.co2.utils.Constant.spPassword;
import static com.custom.co2.utils.Constant.spUserId;
import static com.custom.co2.utils.Constant.spUsername;

public class UpdateProfileActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String TAG = "RegisterActivity";
    TextView btnLogin;
    EditText edUsername, edEmail, edContact, edDob;
    final Calendar myCalendar = Calendar.getInstance();
    Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_update_profile);

        inits();
        clicks();

        db = FirebaseFirestore.getInstance();
        progressDialog = showProgressDialog(UpdateProfileActivity.this);
    }

    private void clicks() {

        btnLogin.setOnClickListener(view -> {

            if (Valid()) {
                registerDataToCloud(edUsername.getText().toString(), edEmail.getText().toString(),
                        edDob.getText().toString(), edContact.getText().toString());
            }
        });
        edDob.setOnClickListener(view -> {

            myCalendar.add(Calendar.YEAR, -10);
            int year = myCalendar.get(Calendar.YEAR);
            int month = myCalendar.get(Calendar.MONTH);
            int day = myCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(UpdateProfileActivity.this, date, year, month, day);

            datePicker.getDatePicker().setMaxDate(myCalendar.getTimeInMillis());
            datePicker.show();


        });
    }

    private boolean Valid() {
        if (edUsername.getText().toString().isEmpty()) {
            edUsername.setError("Enter username");
            edUsername.requestFocus();
            return false;

        } else if (edContact.getText().toString().isEmpty()) {
            edContact.setError("Enter contact");
            edContact.requestFocus();
            return false;

        } else if (edContact.getText().toString().length() != 10) {
            edContact.setError("Enter valid contact");
            edContact.requestFocus();
            return false;

        } else if (edDob.getText().toString().isEmpty()) {
            edDob.setError("Enter date of birth");
            edDob.requestFocus();
            return false;

        } else if (edEmail.getText().toString().isEmpty()) {
            edEmail.setError("Enter email");
            edEmail.requestFocus();
            return false;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(edEmail.getText().toString()).matches()) {
            edEmail.setError("Enter valid email");
            edEmail.requestFocus();
            return false;

        }
        return true;
    }

    private void inits() {
        btnLogin = findViewById(R.id.tv_btn_login);
        edUsername = findViewById(R.id.ed_username);
        edEmail = findViewById(R.id.ed_email);
        edDob = findViewById(R.id.ed_dob);
        edContact = findViewById(R.id.ed_contact);

        edUsername.setText(getShaedPref(this, spUsername));
        edEmail.setText(getShaedPref(this, spEmail));
        edDob.setText(getShaedPref(this, spDob));
        edContact.setText(getShaedPref(this, spConatact));


    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }


    };

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edDob.setText(sdf.format(myCalendar.getTime()));
        edDob.setError(null);
    }

    private void registerDataToCloud(String username, String email, String dob, String contact) {
        progressDialog.show();
        Map<String, Object> user = new HashMap<>();
        user.put("Username", username);
        user.put("DOB", dob);
        user.put("Contact", contact);

        db.collection("users").document(getShaedPref(this, Constant.spUserId))
                .update(user)
                .addOnSuccessListener(aVoid -> {

                    setShaedPref(UpdateProfileActivity.this, spUsername,username);
                    setShaedPref(UpdateProfileActivity.this, spConatact, contact);
                    setShaedPref(UpdateProfileActivity.this, spDob, dob);
                    Toast.makeText(UpdateProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }).addOnFailureListener(e -> {
            Log.e(TAG, "registerDataToCloud: " + e.getMessage());
            progressDialog.dismiss();
        });
    }
}