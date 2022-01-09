package com.app.managebr.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.managebr.R;
import com.app.managebr.activities.SelectionActivity;
import com.app.managebr.models.ManagerModelCLass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ViewManagerActivity extends BaseActivity {

    TextView tvManagerName, tvBusinessTitle;
    ImageView imgUserPic;
    DatabaseReference databaseReference;
    String name="", business="", pic="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_manager);

        tvManagerName = findViewById(R.id.tvManagerName);
        tvBusinessTitle = findViewById(R.id.tvBusinessTitle);
        imgUserPic = findViewById(R.id.imgUserPic);

        databaseReference = FirebaseDatabase.getInstance().getReference("ManagersData").child(EmployeeActivity.managerId);

    }

    //Getting current logged in driver data from firebase realtime database.
    private void loadUserData() {

        showProgressDialog("Loading data....");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ManagerModelCLass model = snapshot.getValue(ManagerModelCLass.class);
                name = model.getFullName();
                pic = model.getPic();
                business = model.getAddress();

                for(DataSnapshot snapshot1 : snapshot.getChildren()){ }

                tvManagerName.setText("Name : "+name);
                tvBusinessTitle.setText("Business : "+business);
                if(!pic.equals("")){
                    Picasso.with(ViewManagerActivity.this).load(pic).placeholder(R.drawable.profile).into(imgUserPic);
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

}