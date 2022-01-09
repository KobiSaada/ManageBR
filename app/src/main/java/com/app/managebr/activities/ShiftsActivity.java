package com.app.managebr.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.managebr.R;
import com.app.managebr.models.EmployeeModelCLass;
import com.app.managebr.models.ShiftModelClass;
import com.app.managebr.utils.MyFirebaseInstanceService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//Show list of accepted events screen..
public class ShiftsActivity extends BaseActivity {

    RecyclerView recyclerView;
    TextView textView;
    DatabaseReference databaseReference;
    List<ShiftModelClass> list;
    String userId="";
    ShiftssListAdapter adapter;
    public static ShiftModelClass model;
    EditText edtSearch;
    FloatingActionButton btnCreateShift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts);

        showProgressDialog("Loading data..");
        //Firebase and screen views initialization..
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Shifts");
        list = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); ;
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(gridLayoutManager);
        textView = findViewById(R.id.textView);
        edtSearch = findViewById(R.id.edtSearch);
        btnCreateShift = findViewById(R.id.btnCreateShift);

        //Search bar code..
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }});

        btnCreateShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CreateShiftActivity.class));
            }
        });

    }

    //Get all the accepted events from firebase in this function and set them up in a list..
    @Override
    protected void onStart() {
        super.onStart();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                recyclerView.setAdapter(null);
                textView.setText("");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ShiftModelClass model = snapshot.getValue(ShiftModelClass.class);
                    if(userId.equals(model.getManagerId())){
                        list.add(model);
                    }
                }
                if(list.size()>0){
                    adapter = new ShiftssListAdapter(ShiftsActivity.this, list);
                    recyclerView.setAdapter(adapter);
                }else {
                    textView.setText("No Shifts Created!");
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

    //This method apply searching in list..
    private void filter(String text) {
        //new array list that will hold the filtered data
        List<ShiftModelClass> filterdNames = new ArrayList<>();

        //looping through existing elements
        for (ShiftModelClass s : list) {
            //if the existing elements contains the search input
            Date d = s.getDate();
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
            String sDate = sdf2.format(d);
            if (sDate.toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                filterdNames.add(s);
            }
        }

        try {
            adapter.filterList(filterdNames);
        }catch (Exception ex){
            ex.printStackTrace();
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
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.shift_list_layout, parent , false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

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
                    model = shift;
//                    Intent intent = new Intent(mcontext, EventDetailsActivity.class);
//                    intent.putExtra("check","accept");
//                    mcontext.startActivity(intent);
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

        public void filterList(List<ShiftModelClass> searchList) {
            this.muploadList = searchList;
            notifyDataSetChanged();
        }
    }
}