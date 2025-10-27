package com.example.appecig.adminRV;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appecig.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<AdminData> userList = new ArrayList<>();
    private boolean[] userApprovedStatus;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public UserAdapter(Context context) {
        this.context = context;
    }

    public void setUserList(List<AdminData> userList) {
        this.userList = userList;
        userApprovedStatus = new boolean[userList.size()];
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_item_admin, parent, false);
        return new UserViewHolder(view);

    }
    public static class UserViewHolder extends RecyclerView.ViewHolder {

        public MaterialCheckBox approval_checkbox;
        private TextView name, email, role;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
            email = itemView.findViewById(R.id.userEmail);
            role = itemView.findViewById(R.id.userRole);
            approval_checkbox = itemView.findViewById(R.id.checkboxId);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        AdminData user = userList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
        holder.role.setText(user.getRole());

        holder.approval_checkbox.setChecked(user.getApprovement());

        holder.approval_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            user.setApprovement(isChecked);
            String userId = user.getId();
            db.collection("users").document(userId)
                    .update("approved", isChecked)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String toast = isChecked ? "User approved successfully!" : "User unapproved successfully!";
                            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error updating approval status!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }


}