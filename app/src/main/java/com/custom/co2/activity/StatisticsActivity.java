package com.custom.co2.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.custom.co2.utils.Constant.getShaedPref;
import static com.custom.co2.utils.Constant.showProgressDialog;

public class StatisticsActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String TAG = "RegisterActivity";
    Statisticdapter statisticdapter;
    ImageView btnBack, filter;
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
        getStatisticsList(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
    }


    private void inits() {

        recStatistic = findViewById(R.id.recStatistic);
        btnBack = findViewById(R.id.iv_back);
        filter = findViewById(R.id.iv_filter);
        recStatistic.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        statisticdapter = new Statisticdapter(this, arrayList);
        recStatistic.setAdapter(statisticdapter);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateFilter();
            }
        });


    }


    private void showDateFilter() {


        final Dialog dialog = new Dialog(StatisticsActivity.this);
        dialog.setContentView(R.layout.dialog_statistic_filter);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (displaymetrics.widthPixels * 0.90);
        dialog.getWindow().setAttributes(lp);
        TextView txtDate = dialog.findViewById(R.id.txtDate);

        txtDate.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));


        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        TextView btn_submit = dialog.findViewById(R.id.btn_submit);
        txtDate.setOnClickListener(v -> {
            final Calendar newCalendar = Calendar.getInstance();
            final DatePickerDialog StartTime = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                txtDate.setText(dateFormatter.format(newDate.getTime()));
            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
            StartTime.show();
        });
        btn_submit.setOnClickListener(v -> {
            getStatisticsList(txtDate.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void getStatisticsList(String Date) {
        progressDialog.show();
        arrayList.clear();
        db.collection("Co2-Report")
                .whereEqualTo("Date", Date)
                .whereEqualTo("UserID", getShaedPref(this, Constant.spUserId))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        QuerySnapshot document = task.getResult();
                        if (!document.isEmpty()) {

                            if (document.getDocuments().size() > 0) {
                                arrayList.addAll(document.getDocuments());
                                statisticdapter.notifyDataSetChanged();
                                findViewById(R.id.txtNoData).setVisibility(View.GONE);
                            } else {
                                findViewById(R.id.txtNoData).setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (arrayList.size() > 0) {
                                findViewById(R.id.txtNoData).setVisibility(View.GONE);
                            } else {
                                findViewById(R.id.txtNoData).setVisibility(View.VISIBLE);
                            }
                            statisticdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                    progressDialog.dismiss();
                });

    }
}