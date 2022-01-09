package com.app.managebr.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.app.managebr.R;
import com.app.managebr.models.ShiftModelClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateShiftActivity extends AppCompatActivity {

    ImageView imgShiftDate, imgStartTime, imgEndTime;
    EditText edtNumberOfEmp;
    TextView tvShiftDate, tvShiftStartTime, tvShiftEndTime;
    Button btnCreateShift;
    String shiftDate="", shiftStartTime="", shiftEndTime="";
    DatabaseReference databaseReference;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shift);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Shifts");

        imgShiftDate = findViewById(R.id.imgShiftDate);
        imgStartTime = findViewById(R.id.imgStartTime);
        imgEndTime = findViewById(R.id.imgEndTime);
        edtNumberOfEmp = findViewById(R.id.edtNumberOfEmp);
        tvShiftDate = findViewById(R.id.tvShiftDate);
        tvShiftStartTime = findViewById(R.id.tvShiftStartTime);
        tvShiftEndTime = findViewById(R.id.tvShiftEndTime);
        btnCreateShift = findViewById(R.id.btnCreateShift);

        imgShiftDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }
        });
        imgStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectShiftStartTime();
            }
        });
        imgEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectShiftEndTime();
            }
        });
        btnCreateShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numberOfEmp = edtNumberOfEmp.getText().toString().trim();
                if(shiftDate.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please select shift date!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(shiftStartTime.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please select shift start time!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(shiftEndTime.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please select shift end time!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(numberOfEmp.isEmpty()){
                    edtNumberOfEmp.setError("Required!");
                    edtNumberOfEmp.requestFocus();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    Date date = sdf.parse(shiftDate);
                    int emp = Integer.parseInt(numberOfEmp);
                    String id = databaseReference.push().getKey();
                    ShiftModelClass model = new ShiftModelClass(id,date,shiftStartTime,shiftEndTime,emp,"InProgress",userId,ManagerActivity.token);
                    databaseReference.child(id).setValue(model);
                    Toast.makeText(getApplicationContext(), "Shift Created Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
                    shiftDate = sdf.format(calendar.getTime());
                    tvShiftDate.setText(shiftDate);

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

    private void selectShiftStartTime() {
        try {

            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int mint = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minutes) {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minutes);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    shiftStartTime = sdf.format(calendar.getTime());
                    tvShiftStartTime.setText(shiftStartTime);

                }
            }, hour, mint, true);
            timePicker.setTitle("Select time");
            timePicker.show();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void selectShiftEndTime() {
        try {

            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int mint = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minutes) {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minutes);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    shiftEndTime = sdf.format(calendar.getTime());
                    tvShiftEndTime.setText(shiftEndTime);
                }
            }, hour, mint, true);
            timePicker.setTitle("Select time");
            timePicker.show();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}