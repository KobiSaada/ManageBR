package com.app.managebr.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.managebr.R;
import com.app.managebr.models.AppliedShiftModelClass;
import com.app.managebr.models.EmployeeModelCLass;
import com.app.managebr.models.ShiftModelClass;
import com.app.managebr.utils.MyFirebaseInstanceService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewShiftRequestsActivity extends BaseActivity {

    String selectedDate="";
    RecyclerView recyclerView;
    TextView textView;
    DatabaseReference databaseReference;
    String userId;
    List<AppliedShiftModelClass> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_shift_requests);

        showProgressDialog("Loading shifts..");
        //Firebase and screen views initialization..
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("AppliedShifts");
        list = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); ;
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(gridLayoutManager);
        textView = findViewById(R.id.textView);
    }

    //Get all the accepted events from firebase in this function and set them up in a list..
    @Override
    protected void onStart() {
        super.onStart();

        String str = "Pending";

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                recyclerView.setAdapter(null);
                textView.setText("");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    AppliedShiftModelClass model = snapshot.getValue(AppliedShiftModelClass.class);
                    if(str.equals(model.getStatus()) && userId.equals(model.getManagerId())){
                        list.add(model);
                    }
                }
                if(list.size()>0){
                    ShiftssListAdapter adapter = new ShiftssListAdapter(ViewShiftRequestsActivity.this, list);
                    recyclerView.setAdapter(adapter);
                }else {
                    textView.setText("No Shift Requests!");
                }
                hideProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "Error : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Adapter class to set adapter to recyclerview of shifts list..
    public class ShiftssListAdapter extends RecyclerView.Adapter<ShiftssListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<AppliedShiftModelClass> muploadList;

        public ShiftssListAdapter(Context context , List<AppliedShiftModelClass> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.applied_shift_list_layout, parent , false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final AppliedShiftModelClass shift = muploadList.get(position);
            Date d = shift.getDate();
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
            String sDate = sdf2.format(d);
            holder.tvShiftDate.setText("Shift Date : "+sDate);
            holder.tvTime.setText("Time : "+shift.getStartTime()+" to "+shift.getEndTime());

            getEmpData(holder.imgEmp,holder.tvEmpName,shift.getEmpId());

            holder.btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateRecord(shift,position);
                }
            });
            holder.btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseReference.child(shift.getId()).child("status").setValue("Rejected");
                    Toast.makeText(getApplicationContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                    list.remove(position);
                    notifyDataSetChanged();
                    new MyFirebaseInstanceService().sendMessageSingle(ViewShiftRequestsActivity.this, shift.getEmpToken(), ManagerActivity.fullName+" Manager","Manager rejected  your shift request, Shift on Date : "+shift.getDate()+", "+shift.getStartTime()+" to "+shift.getEndTime(), null);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewShiftRequestsActivity.this);
                    builder.setTitle("Confirmation?");
                    builder.setMessage("Are you sure to delete this request?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseReference.child(shift.getId()).removeValue();
                            list.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Request Deleted", Toast.LENGTH_SHORT).show();
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
        }

        private void updateRecord(AppliedShiftModelClass shift, int position) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Shifts");

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        ShiftModelClass model = snapshot1.getValue(ShiftModelClass.class);
                        if(shift.getId().equals(model.getId())){
                            int numOfEmp = model.getNumberOfEmp();
                            numOfEmp = numOfEmp-1;
                            dbRef.child(shift.getId()).child("numberOfEmp").setValue(numOfEmp);
                            if(numOfEmp<1){
                                Toast.makeText(getApplicationContext(), "Employees are already completed for this shift!", Toast.LENGTH_SHORT).show();
                                dbRef.child(shift.getId()).child("status").setValue("EmployeeCompleted");
                            }else {
                                showProgressDialog("Accepting request..");
                                databaseReference.child(shift.getId()).child("status").setValue("Approved");
                                list.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), "Request approved", Toast.LENGTH_SHORT).show();
                                new MyFirebaseInstanceService().sendMessageSingle(ViewShiftRequestsActivity.this, shift.getEmpToken(), ManagerActivity.fullName+" Manager","Manager accepted  your shift request, Shift on Date : "+shift.getDate()+", "+shift.getStartTime()+" to "+shift.getEndTime(), null);

                            }
                            break;
                        }
                    }
                    hideProgressDialog();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { hideProgressDialog();}});
        }

        private void getEmpData(ImageView imgEmp, TextView tvEmpName, String empId) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("EmployeesData").child(empId);
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    EmployeeModelCLass model = snapshot.getValue(EmployeeModelCLass.class);

                    if(!model.getPic().equals("")){
                        Picasso.with(mcontext).load(model.getPic()).placeholder(R.mipmap.ic_launcher_round).into(imgEmp);
                    }
                    tvEmpName.setText("Emp : "+model.getFullName());

                    for(DataSnapshot snapshot1 : snapshot.getChildren()){ }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { hideProgressDialog();}});
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public ImageView imgEmp;
            public TextView tvShiftDate;
            public TextView tvEmpName;
            public TextView tvTime;
            public Button btnApprove;
            public Button btnReject;

            public ImageViewHolder(View itemView) {
                super(itemView);

                imgEmp = itemView.findViewById(R.id.imgEmp);
                tvShiftDate = itemView.findViewById(R.id.tvShiftDate);
                tvEmpName = itemView.findViewById(R.id.tvEmpName);
                tvTime = itemView.findViewById(R.id.tvTime);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }
}