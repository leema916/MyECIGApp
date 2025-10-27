package com.example.appecig;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appecig.teacherRV.Course;
import com.example.appecig.teacherRV.CourseAdapter;
import com.example.appecig.util.Toasty;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EtudiantActivity extends AppCompatActivity  {

    private static final String TAG = "EtudiantActivity";

    private TextView nameEt, fieldEt, groupEt;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private CourseAdapter courseAdapter;


    private NavController navController;
    ImageView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_etudiant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.blue));
        }

        nameEt = findViewById(R.id.nameId);
        fieldEt = findViewById(R.id.fieldId);
        groupEt = findViewById(R.id.groupId);
        logout = findViewById(R.id.logout);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        RecyclerView courseRecyclerView = findViewById(R.id.rvStudent);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        courseRecyclerView.setLayoutManager(layoutManager);

        logout.setOnClickListener(v -> {
            logout();
        });

        showInfo();
    }

    private void logout(){
        FirebaseAuth.getInstance().signOut();
        SharedPreferences sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("email");
        editor.remove("password");
        editor.apply();

        finish();
    }


    private void showInfo() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference dr = mFirestore.collection("users").document(uid);
            dr.get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name");
                            String field = document.getString("filiere");
                            String group = document.getString("group");
                            if (name != null) {
                                nameEt.setText(name);
                                fieldEt.setText(field);
                                groupEt.setText(group);
                                fetchRelevantCourses();
                            } else {
                                Toasty.message(this,"Name is null");
                            }
                        } else {
                            Toasty.message(this,"Document does not exist");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user info", e);
                        Toasty.message(this,"Failed to fetch user info");
                    });
        } else {
            Toasty.message(this,"Current user is null");
            Log.w(TAG, "Current user is null");

        }

    }
    private void fetchRelevantCourses() {
        String studentField = fieldEt.getText().toString();
        String studentGroup = groupEt.getText().toString();
        mFirestore.collection("courses")
                .whereEqualTo("field", studentField)
                .whereEqualTo("group", studentGroup)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Course> courses = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Course course = doc.toObject(Course.class);
                            courses.add(course);
                        }
                        courseAdapter = new CourseAdapter(this, courses);
                        RecyclerView courseRecyclerView = findViewById(R.id.rvStudent);
                        courseRecyclerView.setAdapter(courseAdapter);
                    } else {
                        // Handle Error
                        Toast.makeText(EtudiantActivity.this, "Error fetching courses: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}