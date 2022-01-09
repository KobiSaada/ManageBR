package com.app.managebr.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.app.managebr.R;

//Select login choice screen as driver or mechanic.
public class SelectionActivity extends AppCompatActivity {

    Button btnManager, btnEmployee;
    public static String key="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        btnManager = findViewById(R.id.btnManager);
        btnEmployee = findViewById(R.id.btnEmployee);

        btnManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                key="Manager";
                startActivity(new Intent(getApplicationContext(), ManagerLoginActivity.class));
            }
        });
        btnEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                key="Employee";
                startActivity(new Intent(getApplicationContext(), EmployeeLoginActivity.class));
            }
        });
    }

    //This method checks user login status..
    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = getSharedPreferences("managerInfo", Context.MODE_PRIVATE);
        SharedPreferences sharedPref2 = getSharedPreferences("employeeInfo", Context.MODE_PRIVATE);
        boolean statusManager = sharedPref.getBoolean("statusManager", false);
        boolean statusEmp = sharedPref2.getBoolean("statusEmp", false);

        if(statusManager){
            startActivity(new Intent(getApplicationContext(), ManagerActivity.class));
            finish();
        }
        if(statusEmp){
            startActivity(new Intent(getApplicationContext(), EmployeeActivity.class));
            finish();
        }

    }

//    boolean isOnline(){
//        if(FirebaseAuth.getInstance().getCurrentUser() != null){
//            return true;
//        }
//        return false;
//    }
}