package com.custom.co2.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.custom.co2.utils.Constant.getShaedPref;
import static com.custom.co2.utils.Constant.showProgressDialog;
import static com.custom.co2.utils.Constant.spEmail;

public class changepasswordactivity extends AppCompatActivity {
    final Calendar myCalendar = Calendar.getInstance();
    FirebaseFirestore db;
    String TAG = "RegisterActivity";
    TextView btnLogin;
    EditText edt_old_password, edt_new_password, edt_confirm_password;
    Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_change_password);

        inits();
        clicks();

        db = FirebaseFirestore.getInstance();
        progressDialog = showProgressDialog(changepasswordactivity.this);
    }

    private void clicks() {

        btnLogin.setOnClickListener(view -> {

            if (Valid()) {
                checkPassword(edt_old_password.getText().toString());
            }
        });

    }

    private boolean Valid() {
        if (edt_old_password.getText().toString().isEmpty()) {
            edt_old_password.setError("Enter Old Password");
            edt_old_password.requestFocus();
            return false;

        } else if (edt_new_password.getText().toString().isEmpty()) {
            edt_new_password.setError("Enter New Password");
            edt_new_password.requestFocus();
            return false;

        } else if (!edt_confirm_password.getText().toString().equals(edt_new_password.getText().toString())) {
            edt_confirm_password.setError("Password don't match");
            edt_confirm_password.requestFocus();
            return false;

        }
        return true;
    }

    private void inits() {
        btnLogin = findViewById(R.id.tv_btn_login);
        edt_old_password = findViewById(R.id.edt_old_password);
        edt_new_password = findViewById(R.id.edt_new_password);
        edt_confirm_password = findViewById(R.id.edt_confirm_password);
    }

    private void registerDataToCloud(String password) {
        progressDialog.show();
        Map<String, Object> user = new HashMap<>();
        user.put("Password", password);

        db.collection("users").document(getShaedPref(this, Constant.spUserId))
                .update(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(changepasswordactivity.this, "Change Password Successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
        });
    }


    private void checkPassword(String password) {
        progressDialog.show();

        db.collection("users")
                .whereEqualTo("Email", getShaedPref(changepasswordactivity.this, spEmail))
                .whereEqualTo("Password", password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d(TAG, "checkingIfusernameExist: checking if username exists");

                        if (task.isSuccessful()) {
                            for (DocumentSnapshot ds : task.getResult()) {
                                if (ds.getString("Password").equals(password)) {


                                }
                            }
                        }
                        //checking if task contains any payload. if no, then update
                        if (task.getResult().size() > 0) {
                            try {
                                registerDataToCloud(edt_new_password.getText().toString());
                            } catch (NullPointerException e) {
                                Log.e(TAG, "NullPointerException: " + e.getMessage());
                            }
                        } else {
                            Constant.showAlertDailogBox(changepasswordactivity.this, "Old Password Don't Matched");
                        }
                        progressDialog.dismiss();
                    }
                });


    }

}