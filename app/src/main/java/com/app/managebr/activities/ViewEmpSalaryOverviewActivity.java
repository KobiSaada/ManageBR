package com.app.managebr.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewEmpSalaryOverviewActivity extends BaseActivity {

    RecyclerView recyclerDate, recyclerCheckedIn, recyclerCheckedOut, recyclerHours, recyclerPay;
    TextView textView, tvAssign, tvStdName, tvTotal;
    DatabaseReference databaseReference;
    ImageView imgDate;
    List<String> date, checkedIn, checkedOut, hours, pay;
    List<AppliedShiftModelClass> list, list2;
    String empId, userId;
    float hourSalary;
    float totalSalary = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_emp_salary_overview);

        tvStdName =  findViewById(R.id.tvStdName);
        empId = ViewEmpActivity.model.getUserId();
        hourSalary = ViewEmpActivity.model.getHourSalary();
        tvStdName.setText(ViewEmpActivity.model.getFullName());

        //Firebase and screen views initialization..
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("AppliedShifts");
        date = new ArrayList<>();
        checkedIn = new ArrayList<>();
        checkedOut = new ArrayList<>();
        hours = new ArrayList<>();
        pay = new ArrayList<>();
        list = new ArrayList<>();
        list2 = new ArrayList<>();

        recyclerDate =  findViewById(R.id.recyclerDate);
        recyclerCheckedIn = findViewById(R.id.recyclerCheckedIn);
        recyclerCheckedOut = findViewById(R.id.recyclerCheckedOut);
        recyclerHours = findViewById(R.id.recyclerHours);
        recyclerPay = findViewById(R.id.recyclerPay);
        tvTotal = findViewById(R.id.tvTotal);

        recyclerDate.setHasFixedSize(true); ;
        recyclerCheckedIn.setHasFixedSize(true); ;
        recyclerCheckedOut.setHasFixedSize(true); ;
        recyclerHours.setHasFixedSize(true); ;
        recyclerPay.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,1);
        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(this,1);
        GridLayoutManager gridLayoutManager3 = new GridLayoutManager(this,1);
        GridLayoutManager gridLayoutManager4 = new GridLayoutManager(this,1);
        GridLayoutManager gridLayoutManager5 = new GridLayoutManager(this,1);
        recyclerDate.setLayoutManager(gridLayoutManager);
        recyclerCheckedIn.setLayoutManager(gridLayoutManager2);
        recyclerCheckedOut.setLayoutManager(gridLayoutManager3);
        recyclerHours.setLayoutManager(gridLayoutManager4);
        recyclerPay.setLayoutManager(gridLayoutManager5);

        textView = findViewById(R.id.textView);
        tvAssign = findViewById(R.id.tvAssign);
        imgDate = findViewById(R.id.imgDate);

        imgDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }});
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
                    String shiftDate = sdf.format(calendar.getTime());

                    getRecord(shiftDate);
                }
            }, year, month, day);
            datePicker.setTitle("Select Date");
            datePicker.show();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void getRecord(String shiftDate) {
        String str = "CheckedOut";
        date.clear();
        checkedIn.clear();
        checkedOut.clear();
        hours.clear();
        pay.clear();
        totalSalary = 0;
        for(AppliedShiftModelClass model : list){
            Date d = model.getDate();
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
            String sDate = sdf2.format(d);
            String[] arr = shiftDate.split("-");
            String[] arr2 = sDate.split("-");

            if(arr[1].equals(arr2[1]) && str.equals(model.getStatus())){
                date.add(sDate);
                checkedIn.add(model.getCheckInDate());
                checkedOut.add(model.getCheckOutDate());

                SimpleDateFormat sdf =  new SimpleDateFormat("HH:mm:ss");
                try {
                    Date date1 = sdf.parse(model.getCheckInDate());
                    Date date2 = sdf.parse(model.getCheckOutDate());

                    long millis = date2.getTime()-date1.getTime();
                    int hour = (int) (millis/(1000*60*60));
                    int mins = (int) ((millis/(1000*60))%60);

                    hours.add(hour+"h:"+mins+"m");

                    if(mins>0){
                        float t = hourSalary*hour;
                        float minsSalary = hourSalary/60;
                        float t2 = minsSalary*mins;

                        float total = t+t2;
                        totalSalary = totalSalary+total;

                        pay.add("$"+total);
                    }else {
                        float total = hourSalary*hour;
                        totalSalary = totalSalary+total;
                        pay.add(total+"");
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        if(date.size()>0){
            textView.setText("");
            recyclerDate.setAdapter(null);
            recyclerCheckedIn.setAdapter(null);
            recyclerCheckedOut.setAdapter(null);
            recyclerHours.setAdapter(null);
            recyclerPay.setAdapter(null);

            AssignmentListAdapter adapter = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, date);
            recyclerDate.setAdapter(adapter);
            AssignmentListAdapter adapter2 = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, checkedIn);
            recyclerCheckedIn.setAdapter(adapter2);
            AssignmentListAdapter adapter3 = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, checkedOut);
            recyclerCheckedOut.setAdapter(adapter3);
            AssignmentListAdapter adapter4 = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, hours);
            recyclerHours.setAdapter(adapter4);
            AssignmentListAdapter adapter5 = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, pay);
            recyclerPay.setAdapter(adapter5);

            tvTotal.setText("(Total Payable Amount : $"+totalSalary+")");
        }else {
            textView.setText("No Data on Selected Month and Year!");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        String str = "CheckedOut";
        String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String[] arr = cDate.split("-");

        showProgressDialog("Loading data..");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                date.clear();
                checkedIn.clear();
                checkedOut.clear();
                hours.clear();
                pay.clear();
                textView.setText("");
                recyclerDate.setAdapter(null);
                recyclerCheckedIn.setAdapter(null);
                recyclerCheckedOut.setAdapter(null);
                recyclerHours.setAdapter(null);
                recyclerPay.setAdapter(null);
                totalSalary = 0f;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    AppliedShiftModelClass model = snapshot.getValue(AppliedShiftModelClass.class);
                    list.add(model);
                    if(empId.equals(model.getEmpId()) && userId.equals(model.getManagerId()) && str.equals(model.getStatus())){
                        Date d = model.getDate();
                        SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
                        String sDate = sdf2.format(d);
                        String[] arr2 = sDate.split("-");
                        if(arr[1].equals(arr2[1])){
                            date.add(sDate);
                            checkedIn.add(model.getCheckInDate());
                            checkedOut.add(model.getCheckOutDate());

                            SimpleDateFormat sdf =  new SimpleDateFormat("HH:mm:ss");
                            try {
                                Date date1 = sdf.parse(model.getCheckInDate());
                                Date date2 = sdf.parse(model.getCheckOutDate());

                                long millis = date2.getTime()-date1.getTime();
                                int hour = (int) (millis/(1000*60*60));
                                int mins = (int) ((millis/(1000*60))%60);

                                hours.add(hour+"h:"+mins+"m");

                                if(mins>0){
                                    float t = hourSalary*hour;
                                    float minsSalary = hourSalary/60;
                                    float t2 = minsSalary*mins;

                                    float total = t+t2;
                                    totalSalary = totalSalary+total;

                                    pay.add("$"+total);
                                }else {
                                    float total = hourSalary*hour;
                                    totalSalary = totalSalary+total;
                                    pay.add(total+"");
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if(date.size()>0){
                    AssignmentListAdapter adapter = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, date);
                    recyclerDate.setAdapter(adapter);
                    AssignmentListAdapter adapter2 = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, checkedIn);
                    recyclerCheckedIn.setAdapter(adapter2);
                    AssignmentListAdapter adapter3 = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, checkedOut);
                    recyclerCheckedOut.setAdapter(adapter3);
                    AssignmentListAdapter adapter4 = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, hours);
                    recyclerHours.setAdapter(adapter4);
                    AssignmentListAdapter adapter5 = new AssignmentListAdapter(ViewEmpSalaryOverviewActivity.this, pay);
                    recyclerPay.setAdapter(adapter5);

                    tvTotal.setText("(Total Payable Amount : $"+totalSalary+")");
                }else {
                    textView.setText("No Data!");
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

    public class AssignmentListAdapter extends RecyclerView.Adapter<AssignmentListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<String> muploadList;

        public AssignmentListAdapter(Context context , List<String> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.record_layout , parent , false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, final int position) {

            final String assignment = muploadList.get(position);

            holder.tvName.setText(assignment);

        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public TextView tvName;

            public ImageViewHolder(View itemView) {
                super(itemView);

                tvName = itemView.findViewById(R.id.tvName);

            }
        }
    }
}