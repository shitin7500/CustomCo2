package com.custom.co2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.custom.co2.R;
import com.custom.co2.utils.Constant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.events.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextView btnGotoSignUp;
    FirebaseFirestore db;
    String TAG = "RegisterActivity";
    TextView btnLogin;
    EditText edUsername, edEmail, edContact, edDob, edPassword;
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        inits();
        clicks();

        db = FirebaseFirestore.getInstance();
    }

    private void clicks() {
        btnGotoSignUp.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });
        btnLogin.setOnClickListener(view -> {

            if (Valid()) {
                checkEmail(edEmail.getText().toString());
            }
        });
        edDob.setOnClickListener(view -> {

            myCalendar.add(Calendar.YEAR, -10);
            int year = myCalendar.get(Calendar.YEAR);
            int month = myCalendar.get(Calendar.MONTH);
            int day = myCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(RegisterActivity.this, date, year, month, day);

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

        } else if (edPassword.getText().toString().isEmpty()) {
            edPassword.setError("Enter password");
            edPassword.requestFocus();
            return false;

        }


        return true;
    }

    private void inits() {
        btnGotoSignUp = findViewById(R.id.tv_goto_login);
        btnLogin = findViewById(R.id.tv_btn_login);
        edUsername = findViewById(R.id.ed_username);
        edEmail = findViewById(R.id.ed_email);
        edPassword = findViewById(R.id.ed_password);
        edDob = findViewById(R.id.ed_dob);
        edContact = findViewById(R.id.ed_contact);

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

    private void registerDataToCloud(String username, String email, String password, String dob, String contact) {
        Map<String, Object> user = new HashMap<>();
        user.put("Username", username);
        user.put("Email", email);
        user.put("Password", password);
        user.put("DOB", dob);
        user.put("Contact", contact);

        db.collection("users")
                .document("user" + System.currentTimeMillis() / 1000).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Registration successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                    // Toast.makeText(RegisterActivity.this, "Registration successfully", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> {
            Log.e(TAG, "registerDataToCloud: " + e.getMessage());

            Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void checkEmail(String email) {

        db.collection("users")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d(TAG, "checkingIfusernameExist: checking if username exists");

                        if (task.isSuccessful()) {
                            for (DocumentSnapshot ds : task.getResult()) {
                                if (ds.getString("Email").equals(email)) {

                                    Constant.showAlertDailogBox(RegisterActivity.this, "This email is already exist");

                                }
                            }
                        }
                        //checking if task contains any payload. if no, then update
                        if (task.getResult().size() == 0) {
                            try {
                                registerDataToCloud(edUsername.getText().toString(), edEmail.getText().toString(),
                                        edPassword.getText().toString(), edDob.getText().toString(), edContact.getText().toString());
                            } catch (NullPointerException e) {
                                Log.e(TAG, "NullPointerException: " + e.getMessage());
                            }
                        }
                    }
                });


    }

}