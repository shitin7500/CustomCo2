package com.custom.co2.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.custom.co2.R;
import com.custom.co2.adapter.Statisticdapter;
import com.custom.co2.utils.Constant;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

import static com.custom.co2.utils.Constant.getShaedPref;
import static com.custom.co2.utils.Constant.showProgressDialog;
import static com.custom.co2.utils.Constant.spConatact;
import static com.custom.co2.utils.Constant.spDob;
import static com.custom.co2.utils.Constant.spEmail;
import static com.custom.co2.utils.Constant.spUsername;

public class StatisticsActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String TAG = "RegisterActivity";
    Statisticdapter statisticdapter;
    ImageView btnBack;
    Dialog progressDialog;
    ArrayList arrayList = new ArrayList<DocumentSnapshot>();
    RecyclerView recStatistic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_statistics);
        inits();

        db = FirebaseFirestore.getInstance();
        progressDialog = showProgressDialog(StatisticsActivity.this);
        getStatisticsList();
    }


    private void inits() {

        recStatistic = findViewById(R.id.recStatistic);
        btnBack = findViewById(R.id.iv_back);
        recStatistic.setLayoutManager(new LinearLayoutManager(this));
        statisticdapter = new Statisticdapter(this, arrayList);
        recStatistic.setAdapter(statisticdapter);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }


    private void getStatisticsList() {
        progressDialog.show();

        db.collection("Co2-Report")
                .whereEqualTo("UserID", getShaedPref(this, Constant.spUserId))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        QuerySnapshot document = task.getResult();
                        if (!document.isEmpty()) {

                            for (int i = 0; i < document.getDocuments().size(); i++) {
                                arrayList.addAll(document.getDocuments());
                            }
                            if (arrayList.size() > 0) {
                                statisticdapter.notifyDataSetChanged();
                                findViewById(R.id.txtNoData).setVisibility(View.GONE);
                            } else {
                                findViewById(R.id.txtNoData).setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                    progressDialog.dismiss();
                });
    }
}