package com.app.managebr.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import com.app.managebr.utils.MyFirebaseInstanceService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

//Show list of accepted events screen..
public class ManageEmpActivity extends BaseActivity {

    RecyclerView recyclerView;
    TextView textView;
    DatabaseReference databaseReference;
    List<EmployeeModelCLass> list;
    String userId="";
    EventsListAdapter adapter;
    public static EmployeeModelCLass model;
    EditText edtSearch;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_emp);

        showProgressDialog("Loading data..");
        //Firebase and screen views initialization..
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("EmployeesData");
        list = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); ;
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(gridLayoutManager);
        textView = findViewById(R.id.textView);
        edtSearch = findViewById(R.id.edtSearch);

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
                    EmployeeModelCLass model = snapshot.getValue(EmployeeModelCLass.class);
                    if(userId.equals(model.getManagerId())){
                        list.add(model);
                    }
                }
                if(list.size()>0){
                    adapter = new EventsListAdapter(ManageEmpActivity.this, list);
                    recyclerView.setAdapter(adapter);
                }else {
                    textView.setText("No Employees Selected!");
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
        List<EmployeeModelCLass> filterdNames = new ArrayList<>();

        //looping through existing elements
        for (EmployeeModelCLass s : list) {
            //if the existing elements contains the search input
            if (s.getFullName().toLowerCase().contains(text.toLowerCase())) {
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

    //Adapter class to set adpater to recyclerview of employees list..
    public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<EmployeeModelCLass> muploadList;

        public EventsListAdapter(Context context , List<EmployeeModelCLass> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.employee_list_layout, parent , false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final EmployeeModelCLass employee = muploadList.get(position);
            holder.tvEmpName.setText(employee.getFullName());
            holder.tvSkill.setText(employee.getAddress());
            holder.tvContact.setText(employee.getPhone());

            if(!employee.getPic().equals("")){
                Picasso.with(mcontext).load(employee.getPic()).placeholder(R.mipmap.ic_launcher_round).into(holder.imgEmp);
            }
            holder.cbAddEmp.setChecked(true);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dailogBuilder = new AlertDialog.Builder(ManageEmpActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.dialog_layout, null);
                    dailogBuilder.setView(dialogView);

                    final TextView tv = dialogView.findViewById(R.id.tv);
                    final EditText edtSalary = dialogView.findViewById(R.id.edtSalary);
                    final Button btnSub = dialogView.findViewById(R.id.btnSub);

                    tv.setText("Edit Hour Salary");
                    edtSalary.setText(employee.getHourSalary()+"");

                    btnSub.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String hourSalary = edtSalary.getText().toString().trim();
                            if(hourSalary.isEmpty()){
                                edtSalary.setError("Required!");
                                edtSalary.requestFocus();
                                return;
                            }
                            float salary = Float.parseFloat(hourSalary);
                            databaseReference.child(employee.getUserId()).child("hourSalary").setValue(salary);
                            Toast.makeText(mcontext, "Hour's Salary Updated!", Toast.LENGTH_SHORT).show();
                            muploadList.remove(position);
                            notifyDataSetChanged();
                            new MyFirebaseInstanceService().sendMessageSingle(ManageEmpActivity.this, employee.getToken(), ManagerActivity.fullName, "Manager updated your hour salary.", null);
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog = dailogBuilder.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.show();
                }
            });
            holder.cbAddEmp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(!isChecked){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ManageEmpActivity.this);
                        builder.setTitle("Confirmation?");
                        builder.setMessage("Are you sure to remove this employee?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference.child(employee.getUserId()).child("managerId").setValue("");
                                databaseReference.child(employee.getUserId()).child("hourSalary").setValue(0f);
                                Toast.makeText(mcontext, "Employee Removed!", Toast.LENGTH_SHORT).show();
                                muploadList.remove(position);
                                notifyDataSetChanged();
                                new MyFirebaseInstanceService().sendMessageSingle(ManageEmpActivity.this, employee.getToken(), ManagerActivity.fullName, "Manager removed you from business.", null);
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
            public ImageView imgEmp;
            public TextView tvEmpName;
            public TextView tvSkill;
            public TextView tvContact;
            public Switch cbAddEmp;

            public ImageViewHolder(View itemView) {
                super(itemView);

                imgEmp = itemView.findViewById(R.id.imgEmp);
                tvEmpName = itemView.findViewById(R.id.tvEmpName);
                tvSkill = itemView.findViewById(R.id.tvSkill);
                tvContact = itemView.findViewById(R.id.tvContact);
                cbAddEmp = itemView.findViewById(R.id.cbAddEmp);
            }
        }

        public void filterList(List<EmployeeModelCLass> searchList) {
            this.muploadList = searchList;
            notifyDataSetChanged();
        }
    }
}