package com.app.managebr.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.managebr.R;
import com.app.managebr.models.EmployeeModelCLass;
import com.app.managebr.models.MessageModel;
import com.app.managebr.utils.MyFirebaseInstanceService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagerMessagingActivity extends BaseActivity {

    EditText edtChat;
    RelativeLayout sendMessage;
    RecyclerView recyclerView;
    TextView textView;
    DatabaseReference databaseReference, dbRef;
    List<MessageModel> list;
    List<EmployeeModelCLass> empList;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_messaging);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        list = new ArrayList<>();
        empList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("ManagerMessages");
        dbRef = FirebaseDatabase.getInstance().getReference("EmployeesData");
        edtChat = findViewById(R.id.edtChat);
        sendMessage = findViewById(R.id.rr);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); ;
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(gridLayoutManager);
        textView = findViewById(R.id.textView);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = edtChat.getText().toString().trim();
                if(msg.isEmpty()){
                    edtChat.setError("Required!");
                    edtChat.requestFocus();
                    return;
                }
                String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                String id = databaseReference.push().getKey();
                MessageModel model = new MessageModel(id,msg,userId,cDate);
                databaseReference.child(id).setValue(model);
                Toast.makeText(getApplicationContext(), "Message Published!", Toast.LENGTH_SHORT).show();
                edtChat.setText("");

                for(EmployeeModelCLass modelCLass : empList){
                    new MyFirebaseInstanceService().sendMessageSingle(ManagerMessagingActivity.this, modelCLass.getToken(), ManagerActivity.fullName, "New Message : "+msg, null);
                }
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
                    MessageModel model = snapshot.getValue(MessageModel.class);
                    if(userId.equals(model.getManagerId())){
                        list.add(model);
                    }
                }
                if(list.size()>0){
                    MessageListAdapter adapter = new MessageListAdapter(ManagerMessagingActivity.this, list);
                    recyclerView.setAdapter(adapter);
                }else {
                    textView.setText("No Messages!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "Error : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                empList.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    EmployeeModelCLass model = snapshot1.getValue(EmployeeModelCLass.class);
                    if(userId.equals(model.getManagerId())){
                        empList.add(model);
                    }
                }

                hideProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { hideProgressDialog();}});
    }

    //Adapter class to set adpater to recyclerview of employees list..
    public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<MessageModel> muploadList;

        public MessageListAdapter(Context context , List<MessageModel> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.message_layout, parent , false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final MessageModel message = muploadList.get(position);
            holder.tvMessage.setText(message.getMessage());
            holder.tvDate.setText(message.getDate());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ManagerMessagingActivity.this);
                    builder.setTitle("Confirmation?");
                    builder.setMessage("Are you sure to delete this message?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseReference.child(message.getId()).removeValue();
                            list.remove(position);
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
            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public TextView tvMessage;
            public TextView tvDate;

            public ImageViewHolder(View itemView) {
                super(itemView);

                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }
}