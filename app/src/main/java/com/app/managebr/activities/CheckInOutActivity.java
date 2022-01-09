package com.app.managebr.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.managebr.R;
import com.app.managebr.models.AppliedShiftModelClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckInOutActivity extends BaseActivity {

    RecyclerView recyclerView;
    TextView textView;
    DatabaseReference databaseReference;
    String userId;
    List<AppliedShiftModelClass> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checked_in_out);

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

        String str = "Approved";
        String str2 = "CheckedIn";

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                recyclerView.setAdapter(null);
                textView.setText("");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    AppliedShiftModelClass model = snapshot.getValue(AppliedShiftModelClass.class);
                    if(str.equals(model.getStatus()) || str2.equals(model.getStatus())){
                        if(userId.equals(model.getEmpId())){
                            difference(model);
                            list.add(model);
                        }
                    }
                }
                if(list.size()>0){
                    Collections.sort(list, new Comparator<AppliedShiftModelClass>() {
                        @Override
                        public int compare(AppliedShiftModelClass o1, AppliedShiftModelClass o2) {
                            Date d = o1.getDate();
                            Date d2 = o2.getDate();
                            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
                            String sDate = sdf2.format(d);
                            String sDate2 = sdf2.format(d2);
                            return sDate.compareTo(sDate2);
                        }
                    });
                    ShiftssListAdapter adapter = new ShiftssListAdapter(CheckInOutActivity.this, list);
                    recyclerView.setAdapter(adapter);
                }else {
                    textView.setText("No Upcoming Shifts!");
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
            View v = LayoutInflater.from(mcontext).inflate(R.layout.checked_shift_list_layout, parent , false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final AppliedShiftModelClass shift = muploadList.get(position);
            Date d = shift.getDate();
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
            String sDate = sdf2.format(d);
            holder.tvShiftDate.setText("Shift Date : "+sDate);
            holder.tvStartTime.setText("Start Time : "+shift.getStartTime());
            holder.tvEndTime.setText("End Time : "+shift.getEndTime());
            holder.tvStatus.setText("Shift Status : "+shift.getStatus());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(shift.getStatus().equals("Approved")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(CheckInOutActivity.this);
                        builder.setTitle("Confirmation?");
                        builder.setMessage("Do you want to check in for this shift?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
                                databaseReference.child(shift.getId()).child("status").setValue("CheckedIn");
                                databaseReference.child(shift.getId()).child("checkInDate").setValue(currentTime);
                                Toast.makeText(getApplicationContext(), "Checked In", Toast.LENGTH_SHORT).show();

                                notifyDataSetChanged();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }else if(shift.getStatus().equals("CheckedIn")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(CheckInOutActivity.this);
                        builder.setTitle("Confirmation?");
                        builder.setMessage("Do you want to check out for this shift?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
                                databaseReference.child(shift.getId()).child("status").setValue("CheckedOut");
                                databaseReference.child(shift.getId()).child("checkOutDate").setValue(currentTime);
                                Toast.makeText(getApplicationContext(), "Checked Out", Toast.LENGTH_SHORT).show();

                                notifyDataSetChanged();
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
                }
            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public TextView tvShiftDate;
            public TextView tvStartTime;
            public TextView tvEndTime;
            public TextView tvStatus;

            public ImageViewHolder(View itemView) {
                super(itemView);

                tvShiftDate = itemView.findViewById(R.id.tvShiftDate);
                tvStartTime = itemView.findViewById(R.id.tvStartTime);
                tvEndTime = itemView.findViewById(R.id.tvEndTime);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }

    private void difference(AppliedShiftModelClass model) {
        String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date date_current = sdf.parse(cDate);
            Date date_diff = model.getDate();

//            long diff = Math.abs(date_diff.getTime() - date_current.getTime());
            long diff = date_diff.getTime() - date_current.getTime();
            long diffDates = diff / (24 * 60 * 60 * 1000);
            int dayDiff = (int) diffDates;

            if(dayDiff<0){
                databaseReference.child(model.getId()).child("status").setValue("Not CheckedOut");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}