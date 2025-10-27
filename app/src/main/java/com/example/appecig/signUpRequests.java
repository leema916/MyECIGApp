package com.example.appecig;

import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.appecig.adminRV.AdminData;
import com.example.appecig.adminRV.UserAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signUpRequests#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signUpRequests extends Fragment {

    UserAdapter userAdapter;
    FirebaseFirestore db;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public signUpRequests() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signUpRequests.
     */
    // TODO: Rename and change types and number of parameters
    public static signUpRequests newInstance(String param1, String param2) {
        signUpRequests fragment = new signUpRequests();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_sign_up_requests, container, false);

        db = FirebaseFirestore.getInstance();
        RecyclerView userRecyclerView = v.findViewById(R.id.rvAdmin);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(getContext());
        userRecyclerView.setAdapter(userAdapter);

        approveUser();
        return v;
    }
    private void approveUser() {
        CollectionReference usersRef = db.collection("users");

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<AdminData> users = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    String userId = doc.getId();
                    AdminData user = doc.toObject(AdminData.class);

                    if (user != null) {
                        user.setId(userId);
                        boolean approved = doc.getBoolean("approved");
                        user.setApprovement(approved);
                    } else {
                        Log.w("TAG", "Unexpected: User object is null!");
                    }
                    users.add(user);
                    Log.d("TAG", "Fetched User: " + user);
                }
                userAdapter.setUserList(users);
            } else {
                Log.w("TAG", "Error fetching users:", task.getException());
            }
        });
    }

}