package com.app.managebr.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.app.managebr.R;
import com.app.managebr.models.EmployeeModelCLass;
import com.app.managebr.models.ManagerModelCLass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

//Driver Profile Screen..
public class ProfileScreenActivity extends BaseActivity {

    private static final int PICK_FILE_REQUEST = 101;
    private static final int STORAGE_PERMISSION_CODE = 100;
    ImageView imgEdit, imgUserPic, selectpic;
    EditText edtName, edtPhone,edtAddress,  edtEmail, editPassword;
    Button btnUpdate, btnUpdatePass;
    TextView tvEmailStatus;
    String userId, userName,address, userPhone, url, password;
    DatabaseReference databaseReference;
    StorageReference mStorageRef;
    StorageTask mUploadTask;
    boolean mGranted;
    Uri fileUri = Uri.EMPTY;
    int count = 0;
    String key="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        Intent intent = getIntent();
        key = intent.getStringExtra("key");

        //Screen views initialization..
        imgEdit = findViewById(R.id.imgEdit);
        imgUserPic = findViewById(R.id.imgUserPic);
        selectpic = findViewById(R.id.selectpic);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtEmail = findViewById(R.id.edtEmail);
        editPassword = findViewById(R.id.editPassword);
        tvEmailStatus = findViewById(R.id.tvEmailStatus);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdatePass = findViewById(R.id.btnUpdatePass);

        if(key.equals("Manager")){
            //Firebase realtime and storage initialization..
            databaseReference = FirebaseDatabase.getInstance().getReference("ManagersData");
            mStorageRef = FirebaseStorage.getInstance().getReference("ManagersData/");

            //Get current logged in manager data from manager home page screen..
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userName = ManagerActivity.fullName;
            userPhone = ManagerActivity.phone;
            url = ManagerActivity.pic;
            password = ManagerActivity.password;
            address = ManagerActivity.address;
            edtAddress.setHint("Business Title");
        }else if(key.equals("Emp")){
            //Firebase realtime and storage initialization..
            databaseReference = FirebaseDatabase.getInstance().getReference("EmployeesData");
            mStorageRef = FirebaseStorage.getInstance().getReference("EmployeesData/");

            //Get current logged in manager data from employee home page screen..
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userName = EmployeeActivity.fullName;
            userPhone = EmployeeActivity.phone;
            url = EmployeeActivity.pic;
            password = EmployeeActivity.password;
            address = EmployeeActivity.address;
            edtAddress.setHint("Skill");
        }

        //Setting up current logged in driver data into appropriate fields.
        edtName.setText(userName);
        edtPhone.setText(userPhone);
        edtAddress.setText(address);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        edtEmail.setText(firebaseUser.getEmail());
        editPassword.setText(password);

        if(!url.isEmpty()){
            Picasso.with(this).load(url).placeholder(R.drawable.profile).into(imgUserPic);
        }

        edtName.setEnabled(false);
        edtPhone.setEnabled(false);
        edtAddress.setEnabled(false);
        edtEmail.setEnabled(false);
        editPassword.setEnabled(false);

        //Check email verification status and resend verification email to user on his/her email
        tvEmailStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkEmailIsVerified()){
                    sendVerificationEmail();
                }
            }
        });

        //Edit sign clicks code
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                if(count%2 != 0) {
                    edtName.setEnabled(true);
                    edtName.requestFocus();
                    edtPhone.setEnabled(true);
                    edtAddress.setEnabled(true);
                }else {
                    edtName.setEnabled(false);
                    edtPhone.setEnabled(false);
                    edtAddress.setEnabled(false);
                }
            }
        });
        //Select user picture code
        selectpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!mGranted) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            return;
                        }
                    }
                }

                //Move user to gallery of phone
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_FILE_REQUEST);
            }
        });

        //Update button click code
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = edtName.getText().toString().trim();
                userPhone = edtPhone.getText().toString().trim();
                address = edtAddress.getText().toString().trim();

                //Validations to all data fileds..
                if(TextUtils.isEmpty(userName)){
                    edtName.setError("Required!");
                    edtName.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(userPhone)){
                    edtPhone.setError("Required!");
                    edtPhone.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(address)){
                    edtAddress.setError("Required!");
                    edtAddress.requestFocus();
                    return;
                }

                updateAccount();
            }
        });

        //Send update password email code..
        btnUpdatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Password reset email sent successfully", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(), "Error : "+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //Update account details function..
    private void updateAccount() {
        if(key.equals("Manager")){
            ManagerModelCLass model = new ManagerModelCLass(userId,userName,url,ManagerActivity.email,userPhone,address,password,ManagerActivity.token,
                    ManagerActivity.userType,ManagerActivity.emailVerified);
            databaseReference.child(userId).setValue(model);
            edtName.setEnabled(false);
            edtPhone.setEnabled(false);
            edtAddress.setEnabled(false);
            editPassword.setEnabled(false);
        }else if(key.equals("Emp")) {
            EmployeeModelCLass model = new EmployeeModelCLass(userId,userName,url,EmployeeActivity.email,userPhone,address,password,EmployeeActivity.token,
                    EmployeeActivity.userType,EmployeeActivity.emailVerified,EmployeeActivity.managerId,EmployeeActivity.hourSalary);
            databaseReference.child(userId).setValue(model);
            edtName.setEnabled(false);
            edtPhone.setEnabled(false);
            edtAddress.setEnabled(false);
            editPassword.setEnabled(false);
        }

        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();

    }

    //Check permissions of read and write storage of mobile phone
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                mGranted = true;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_FILE_REQUEST);
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override

    //This function checks which file user select from his mobile gallery or file explorer..
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && data != null) {
            fileUri = data.getData();
            Picasso.with(this).load(fileUri).into(imgUserPic);
            updatePic();
        }
    }

    private  String getExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver() ;
        MimeTypeMap mime = MimeTypeMap.getSingleton() ;
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //This function update user profile pic and store it to firebase storage..
    private void updatePic() {
        if(fileUri !=null){
            showProgressDialog("Uploading Picture..");
            final StorageReference fileref = mStorageRef.child(System.currentTimeMillis() + "." + getExtension(fileUri));
            mUploadTask =   fileref.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            hideProgressDialog();
                            try {
                                databaseReference.child(userId).child("pic").setValue(uri.toString());
                                Toast.makeText(ProfileScreenActivity.this, "Picture updated", Toast.LENGTH_SHORT).show();
                                fileUri = Uri.EMPTY;
                            } catch (Exception ex ){
                                Toast.makeText(getApplicationContext()  , "Error : " + ex.toString() , Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                }
            });
        }
    }

    //Check current logged in email verification status on the start of profile screen.
    @Override
    protected void onStart() {
        super.onStart();

        if(!checkEmailIsVerified()){
            tvEmailStatus.setText("Email not Verified, CLick to verify");
        }else {
            tvEmailStatus.setText("Email Verified");
        }
    }

    //Check verification status method...
    private boolean checkEmailIsVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user.isEmailVerified()){
            return true;
        }else {
            return false;
        }
    }

    //Send verification email code.
    private void sendVerificationEmail() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Verification email sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
