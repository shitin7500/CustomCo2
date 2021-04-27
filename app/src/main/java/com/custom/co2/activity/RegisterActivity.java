package com.custom.co2.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.custom.co2.utils.Constant.showProgressDialog;

public class RegisterActivity extends AppCompatActivity {

    TextView btnGotoSignUp;
    FirebaseFirestore db;
    String TAG = "RegisterActivity";
    TextView btnLogin;
    EditText edUsername, edEmail, edContact, edDob, edPassword, edConfirmPassword;
    final Calendar myCalendar = Calendar.getInstance();
    Dialog progressDialog;

    /**
     * Method for initailize layout
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        inits();
        clicks();

        db = FirebaseFirestore.getInstance();
        progressDialog = showProgressDialog(RegisterActivity.this);
    }

    /**
     * Method for manage clicks
     */
    private void clicks() {
        btnGotoSignUp.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });
        btnLogin.setOnClickListener(view -> {
            if(!isNetworkConnected()){
                Toast.makeText(this, "Internet is not connected", Toast.LENGTH_SHORT).show();
                return;
            }
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

    /**
     * Method for validations
     */
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
        else if (edPassword.getText().toString().length() < 6) {
            edPassword.setError("Password length should be more then 5 letter");
            edPassword.requestFocus();
            return false;

        }
        else if (!edConfirmPassword.getText().toString().equals(edPassword.getText().toString())) {
            edConfirmPassword.setError("Password don't match");
            edConfirmPassword.requestFocus();
            return false;

        }


        return true;
    }

    /**
     * Method for initialize view and variables
     */
    private void inits() {
        btnGotoSignUp = findViewById(R.id.tv_goto_login);
        btnLogin = findViewById(R.id.tv_btn_login);
        edUsername = findViewById(R.id.ed_username);
        edConfirmPassword = findViewById(R.id.ed_confirm_password);
        edEmail = findViewById(R.id.ed_email);
        edPassword = findViewById(R.id.ed_password);
        edDob = findViewById(R.id.ed_dob);
        edContact = findViewById(R.id.ed_contact);

    }

    /**
     * Method for get date picker callback when date selected
     */
    DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateLabel();
    };

    /**
     * Method for update date lable
     */
    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edDob.setText(sdf.format(myCalendar.getTime()));
        edDob.setError(null);
    }

    /**
     * Method for register data to firebase
     */
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
                    progressDialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();

                }).addOnFailureListener(e -> {
            Log.e(TAG, "registerDataToCloud: " + e.getMessage());
            progressDialog.dismiss();
           });
    }

    /**
     * Method for check email existing
     */
    private void checkEmail(String email) {
        progressDialog.show();

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
                                    progressDialog.dismiss();
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}