package com.app.managebr.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.app.managebr.models.ShiftModelClass;
import com.app.managebr.utils.MyFirebaseInstanceService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApplyForShiftActivity extends BaseActivity {

    ImageView imgShiftDate;
    String selectedDate="";
    RecyclerView recyclerView;
    TextView textView;
    DatabaseReference databaseReference;
    String userId;
    List<ShiftModelClass> list, selectDateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_for_shift);

        showProgressDialog("Loading shifts..");
        //Firebase and screen views initialization..
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Shifts");
        list = new ArrayList<>();
        selectDateList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); ;
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(gridLayoutManager);
        textView = findViewById(R.id.textView);
        imgShiftDate = findViewById(R.id.imgShiftDate);

        imgShiftDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }
        });
    }

    //Get all the accepted events from firebase in this function and set them up in a list..
    @Override
    protected void onStart() {
        super.onStart();

        String str = "InProgress";

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                recyclerView.setAdapter(null);
                textView.setText("Shifts");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ShiftModelClass model = snapshot.getValue(ShiftModelClass.class);
                    if(str.equals(model.getStatus())){
                        list.add(model);
                    }
                }
                if(list.size()>0){
                    ShiftssListAdapter adapter = new ShiftssListAdapter(ApplyForShiftActivity.this, list);
                    recyclerView.setAdapter(adapter);
                }else {
                    textView.setText("No Shifts Created Yet!");
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
    private void selectDate() {
        try {

            final Calendar calendar = Calendar.getInstance();
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            final int month = calendar.get(Calendar.MONTH);
            final int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    calendar.set(year, month, day);
                    selectedDate = sdf.format(calendar.getTime());
                    getShiftsOnSelectedDate();

                    //Toast.makeText(BiddingStartActivity.this, "date "+chooseDate, Toast.LENGTH_SHORT).show();

                    String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                }
            }, year, month, day);
            datePicker.setTitle("Select Date");
            datePicker.show();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void getShiftsOnSelectedDate() {
        String str = "InProgress";
        selectDateList.clear();
        for(ShiftModelClass model : list){
            Date d = model.getDate();
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
            String sDate = sdf2.format(d);
            if(selectedDate.equals(sDate) && str.equals(model.getStatus())){
                selectDateList.add(model);
            }
        }
        if(selectDateList.size()>0){
            recyclerView.setAdapter(null);
            textView.setText("");
            ShiftssListAdapter adapter = new ShiftssListAdapter(ApplyForShiftActivity.this, selectDateList);
            recyclerView.setAdapter(adapter);
            textView.setText("Shifts");
        }else {
            recyclerView.setAdapter(null);
            textView.setText("No Shifts on Selected Date!");
        }
    }

    //Adapter class to set adpater to recyclerview of shifts list..
    public class ShiftssListAdapter extends RecyclerView.Adapter<ShiftssListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<ShiftModelClass> muploadList;

        public ShiftssListAdapter(Context context , List<ShiftModelClass> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public ShiftssListAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.shift_list_layout, parent , false);
            return (new ShiftssListAdapter.ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ShiftssListAdapter.ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final ShiftModelClass shift = muploadList.get(position);
            Date d = shift.getDate();
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
            String sDate = sdf2.format(d);
            holder.tvShiftDate.setText("Shift Date : "+sDate);
            holder.tvStartTime.setText("Start Time : "+shift.getStartTime());
            holder.tvEndTime.setText("End Time : "+shift.getEndTime());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ApplyForShiftActivity.this);
                    builder.setTitle("Confirmation?");
                    builder.setMessage("Are you sure to apply for this shift?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("AppliedShifts");
                            AppliedShiftModelClass modelClass = new AppliedShiftModelClass(shift.getId(),userId,EmployeeActivity.token,shift.getManagerId(),shift.getDate(),
                                    shift.getStartTime(),shift.getEndTime(),"Pending","","");
                            dbRef.child(shift.getId()).setValue(modelClass);
                            Toast.makeText(getApplicationContext(), "Successfully Applied for Shift", Toast.LENGTH_SHORT).show();
                            new MyFirebaseInstanceService().sendMessageSingle(ApplyForShiftActivity.this, shift.getManagerToken(), "New Shift Request", EmployeeActivity.fullName+" apply for shift. ", null);

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

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public ImageView imgEmp;
            public TextView tvShiftDate;
            public TextView tvStartTime;
            public TextView tvEndTime;

            public ImageViewHolder(View itemView) {
                super(itemView);

                imgEmp = itemView.findViewById(R.id.imgEmp);
                tvShiftDate = itemView.findViewById(R.id.tvShiftDate);
                tvStartTime = itemView.findViewById(R.id.tvStartTime);
                tvEndTime = itemView.findViewById(R.id.tvEndTime);
            }
        }
    }
}