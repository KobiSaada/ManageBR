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
import com.app.managebr.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmpMessagingActivity extends BaseActivity {

    RecyclerView recyclerView;
    TextView textView;
    DatabaseReference databaseReference;
    List<MessageModel> list;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_messaging);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        list = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("ManagerMessages");;
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

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                recyclerView.setAdapter(null);
                textView.setText("");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    MessageModel model = snapshot.getValue(MessageModel.class);
                    if(EmployeeActivity.managerId.equals(model.getManagerId())){
                        list.add(model);
                    }
                }
                if(list.size()>0){
                    Collections.reverse(list);
                    MessageListAdapter adapter = new MessageListAdapter(EmpMessagingActivity.this, list);
                    recyclerView.setAdapter(adapter);
                }else {
                    textView.setText("No Messages From Manager!");
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

    //Adapter class to set adpater to recyclerview of messages list..
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