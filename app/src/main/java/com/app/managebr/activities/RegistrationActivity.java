package com.app.managebr.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.managebr.R;
import com.app.managebr.models.EmployeeModelCLass;
import com.app.managebr.models.ManagerModelCLass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//Sign up form screen fro getting name, address etc from user
public class RegistrationActivity extends BaseActivity {

    EditText edtName, edtPhone, edtAddress;
    Button btnRegister;
    String fullName, phone, address, token;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //Firebase database and authentication initialization...

        mAuth = FirebaseAuth.getInstance();

        //Get device token for sending notifications using fcm..
        SharedPreferences e = getSharedPreferences("token",MODE_PRIVATE);
        token = e.getString("id","null");

        //All the Views of screen and firebase initialization..
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnRegister = findViewById(R.id.btnRegister);

        if(SelectionActivity.key.equals("Manager")){
            edtAddress.setHint("Business Title");
        }else if(SelectionActivity.key.equals("Employee")){
            edtAddress.setHint("Skill");
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullName = edtName.getText().toString().trim();
                phone = edtPhone.getText().toString().trim();
                address = edtAddress.getText().toString().trim();

                if(TextUtils.isEmpty(fullName)){
                   edtName.setError("Required!");
                   edtName.requestFocus();
                   return;
                }
                if(TextUtils.isEmpty(phone)){
                    edtPhone.setError("Required!");
                    edtPhone.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(address)){
                    edtAddress.setError("Required!");
                    edtAddress.requestFocus();
                    return;
                }
                createAccount();
            }
        });
    }

    //This method store all the data fields of user to firebase...
    private void createAccount(){
        if(SelectionActivity.key.equals("Manager")){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ManagersData");
            ManagerModelCLass model = new ManagerModelCLass(userId,fullName,"",AuthenticationActivity.email,phone,address,
                    AuthenticationActivity.password,token,SelectionActivity.key, VerificationActivity.emailVerified);
            databaseReference.child(userId).setValue(model);
            Toast.makeText(this, "Your details saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), ManagerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else if(SelectionActivity.key.equals("Employee")){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("EmployeesData");
            EmployeeModelCLass model = new EmployeeModelCLass(userId,fullName,"",AuthenticationActivity.email,phone,address,
                    AuthenticationActivity.password,token,SelectionActivity.key, VerificationActivity.emailVerified,"",0f);
            databaseReference.child(userId).setValue(model);
            Toast.makeText(this, "Your details saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), EmployeeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
