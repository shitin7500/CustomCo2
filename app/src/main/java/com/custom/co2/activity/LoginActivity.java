package com.custom.co2.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.custom.co2.R;
import com.custom.co2.utils.Constant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static com.custom.co2.utils.Constant.setShaedPref;
import static com.custom.co2.utils.Constant.showProgressDialog;
import static com.custom.co2.utils.Constant.spConatact;
import static com.custom.co2.utils.Constant.spDob;
import static com.custom.co2.utils.Constant.spEmail;
import static com.custom.co2.utils.Constant.spPassword;
import static com.custom.co2.utils.Constant.spUsername;

public class LoginActivity extends AppCompatActivity {
    TextView btnRegistration, btnLogin, btnForgotPassword;
    FirebaseFirestore db;
    String TAG = "LoginActivity";
    EditText edEmail, edPassword;
    Dialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        init();
        clicks();
        db = FirebaseFirestore.getInstance();

        progressDialog = showProgressDialog(LoginActivity.this);


    }

    private void clicks() {
        btnRegistration.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });

        btnLogin.setOnClickListener(view -> {
            if (Valid()) {
                checkUser(edEmail.getText().toString().trim(), edPassword.getText().toString().trim());
            }
        });
        btnForgotPassword.setOnClickListener(view -> {
            showForgotPasswordDialog();
        });
    }

    private void showForgotPasswordDialog() {

        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_forgot_password);
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);

        dialog.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);

        EditText edForgotEmail = dialog.findViewById(R.id.ed_email);
        TextView btnForgotPassword = dialog.findViewById(R.id.btn_forgot_password);


        btnForgotPassword.setOnClickListener(view -> {
            if (edForgotEmail.getText().toString().isEmpty()) {
                edForgotEmail.setError("Enter email");
                edForgotEmail.requestFocus();
            } else {
                checkEmail(edForgotEmail.getText().toString(),dialog);
            }
        });


        dialog.show();

    }

    private boolean Valid() {
        if (edEmail.getText().toString().isEmpty()) {
            edEmail.setError("Enter email");
            edEmail.requestFocus();
            return false;
        } else if (edPassword.getText().toString().isEmpty()) {
            edPassword.setError("Enter password");
            edPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void init() {
        btnRegistration = findViewById(R.id.tv_sign_up);
        edEmail = findViewById(R.id.ed_login_email);
        btnLogin = findViewById(R.id.btn_login);
        edPassword = findViewById(R.id.ed_login_password);
        btnForgotPassword = findViewById(R.id.btn_forgot_password);
    }


    private void checkUser(String email, String password) {
        progressDialog.show();
        db.collection("users")
                .whereEqualTo("Email", email)
                .whereEqualTo("Password", password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (DocumentSnapshot ds : task.getResult()) {
                                if (ds.getString("Email").equals(email)) {
//                                    Toast.makeText(LoginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
//                                    Constant.showAlertDailogBox(LoginActivity.this,"Login successfully");
                                    setShaedPref(LoginActivity.this, spUsername, ds.getString("Username"));
                                    setShaedPref(LoginActivity.this, spEmail, ds.getString("Email"));
                                    setShaedPref(LoginActivity.this, spPassword, ds.getString("Password"));
                                    setShaedPref(LoginActivity.this, spConatact, ds.getString("Contact"));
                                    setShaedPref(LoginActivity.this, spDob, ds.getString("DOB"));
                                    progressDialog.dismiss();
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    finish();
                                }
                            }
                        }

                        if (task.getResult().size() == 0) {
                            try {
                                //Toast.makeText(LoginActivity.this, "Invalid username and password", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                Constant.showAlertDailogBox(LoginActivity.this, "Invalid username and password");
                            } catch (NullPointerException e) {
                                Log.e(TAG, "NullPointerException: " + e.getMessage());
                            }
                        }

                    }
                });
    }

    private void checkEmail(String email, Dialog dialog) {
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
                                    Constant.showAlertDailogBox(LoginActivity.this, "Your Password is : "+ds.get("Password"));
                                    dialog.dismiss();

                                }
                            }
                        }
                        //checking if task contains any payload. if no, then update
                        if (task.getResult().size() == 0) {
                            try {
                                progressDialog.dismiss();
                                Constant.showAlertDailogBox(LoginActivity.this, "This email is not found. Plase enter your registered email");
                            } catch (NullPointerException e) {
                                Log.e(TAG, "NullPointerException: " + e.getMessage());
                            }
                        }
                    }
                });


    }

}