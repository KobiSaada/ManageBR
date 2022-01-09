package com.app.managebr.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.app.managebr.R;
import com.app.managebr.models.ManagerModelCLass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//Manager Home Page Screen Activity File.
public class ManagerActivity extends BaseActivity {

    Button btnAddNewEmp, btnManageEmp, btnSalaryOverview, btnMessaging, btnCreateShift, btnViewShiftReq, btnProfile, btnSignOut;
    TextView tvName;
    String userId="";
    DatabaseReference databaseReference;
    public static String fullName,pic, phone, address, token, userType, email, password;
    public static boolean emailVerified;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    LottieAnimationView addEmp,manageEmp,messaging,createShift,viewReq,overview,profile,signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        addEmp = (LottieAnimationView) findViewById(R.id.addEmp);
        addEmp.playAnimation();
        manageEmp = (LottieAnimationView) findViewById(R.id.manageEmp);
        manageEmp.playAnimation();
        messaging = (LottieAnimationView) findViewById(R.id.messaging);
        messaging.playAnimation();
        createShift = (LottieAnimationView) findViewById(R.id.createShift);
        createShift.playAnimation();
        viewReq = (LottieAnimationView) findViewById(R.id.viewReq);
        viewReq.playAnimation();
        overview = (LottieAnimationView) findViewById(R.id.overview);
        overview.playAnimation();
        profile = (LottieAnimationView) findViewById(R.id.profile);
        profile.playAnimation();
        signOut = (LottieAnimationView) findViewById(R.id.signOut);
        signOut.playAnimation();

        //Firebase realtime database initialization for driver.
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("ManagersData").child(userId);

        //Storing manager login state in SharedPreferences.
        sharedPref = getSharedPreferences("managerInfo", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putBoolean("statusManager",true);
        editor.apply();

        //Views initialization of driver screen.
        tvName = findViewById(R.id.tvName);
        btnAddNewEmp = findViewById(R.id.btnAddNewEmp);
        btnManageEmp = findViewById(R.id.btnManageEmp);
        btnSalaryOverview = findViewById(R.id.btnSalaryOverview);
        btnMessaging = findViewById(R.id.btnMessaging);
        btnCreateShift = findViewById(R.id.btnCreateShift);
        btnViewShiftReq = findViewById(R.id.btnViewShiftReq);
        btnProfile = findViewById(R.id.btnProfile);
        btnSignOut = findViewById(R.id.btnSignOut);

        btnAddNewEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddNewEmpActivity.class);
                startActivity(intent);
            }
        });
        btnManageEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ManageEmpActivity.class);
                startActivity(intent);
            }
        });
        btnSalaryOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewEmpActivity.class);
                startActivity(intent);
            }
        });
        btnMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ManagerMessagingActivity.class);
                startActivity(intent);
            }
        });
        btnCreateShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShiftsActivity.class);
                startActivity(intent);
            }
        });
        btnViewShiftReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewShiftRequestsActivity.class);
                startActivity(intent);
            }
        });
        //View Profile button code
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileScreenActivity.class);
                intent.putExtra("key","Manager");
                startActivity(intent);
            }
        });
        //Sign Out button code
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManagerActivity.this);
                builder.setTitle("Confirmation?");
                builder.setMessage("Are you sure to sign out?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.clear();
                        editor.apply();
                        databaseReference.child("token").setValue("null");
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), SelectionActivity.class));
                        finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        //Getting location permissions code from user
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(ManagerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ManagerActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(ManagerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {

            }
        }
    }

    //Getting current logged in manager data from firebase realtime database.
    private void loadUserData() {

        showProgressDialog("Preparing app functions..");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ManagerModelCLass model = snapshot.getValue(ManagerModelCLass.class);
                fullName = model.getFullName();
                pic = model.getPic();
                address = model.getAddress();
                phone = model.getPhone();
                password = model.getPassword();
                email = model.getEmail();
                userType = model.getUserType();
                token = model.getToken();
                emailVerified = model.isEmailVerified();

                for(DataSnapshot snapshot1 : snapshot.getChildren()){ }

                //Check if current logged in user complete his/her account by providing all the required data in profile screen or not.
                if(fullName.equals("")){
                    tvName.setText("Account not completed");
                    AlertDialog.Builder builder = new AlertDialog.Builder(ManagerActivity.this);
                    builder.setTitle("Alert!");
                    builder.setMessage("Please complete your account from profile screen").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else {
                    tvName.setText(fullName);
                }
                hideProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { hideProgressDialog();}});
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadUserData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

}