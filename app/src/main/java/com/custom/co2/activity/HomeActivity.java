package com.custom.co2.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.custom.co2.R;

import static com.custom.co2.utils.Constant.clearShaedPref;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                showAlertLogout();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAlertLogout() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to logout ?");
        builder.setCancelable(false);

        builder.setPositiveButton("YES", (dialog, which) -> {
            clearShaedPref(HomeActivity.this);
            startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

        });

        builder.setNegativeButton("NO", (dialog, which) -> {

            // Do nothing
            dialog.dismiss();
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}