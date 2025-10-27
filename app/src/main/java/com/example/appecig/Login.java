package com.example.appecig;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.appecig.util.Toasty;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class Login extends AppCompatActivity {
    private NavController navController;
    private FirebaseAuth mAuth;
        private FirebaseFirestore mFirestore;
    String email;
    String password;
    String uid;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        AppCompatButton loginBtn = findViewById(R.id.loginBtn);
        EditText editEmail = findViewById(R.id.email);
        EditText editPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(this, R.id.loginNavHost);
        mFirestore = FirebaseFirestore.getInstance();

        loginBtn.setOnClickListener(v -> {

            email = editEmail.getText().toString().trim();
            password = editPassword.getText().toString().trim();

            if (validateInput()) {
                progressBar.setVisibility(View.VISIBLE);
                auth();
            }
        });

    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toasty.message(this,"Please enter a valid email address");
            return false;
        }
        if (password.length() < 6) {
            Toasty.message(this,"Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void auth() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                   if (task.isSuccessful()) {
                       uid = task.getResult().getUser().getUid();
                       signIn();
                   } else {
                       Toasty.message(this, "Something went wrong!");
                   }
                });
    }

    private void signIn() {
        DocumentReference mRef = mFirestore.collection("users").document(uid);
        mRef.get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               Toasty.message(this, "Task was successful");
               DocumentSnapshot ds = task.getResult();
               if (ds.exists()) {
                   Toasty.message(this, "It exists");
                   Map<String, Object> map = ds.getData();
                   if (map != null) {
                       Toasty.message(this, "Data is found");
                       System.out.println(map.toString());
                        boolean isApproved = (boolean) map.get("approved");
                        if (isApproved) {
                            Toasty.message(this, "Approved");
                            String role = (String) map.get("role");
                            if (role.equals("Etudiant")){
                                navController.navigate(R.id.etudiantActivity);
                                finish();
                            }else if(role.equals("Enseignant")){
                                navController.navigate(R.id.teacherActivity);
                            } else if (role.equals("Admin")) {
                                navController.navigate(R.id.adminActivity);
                            }else {
                                Toasty.message(this,"Role not found");
                            }
                        } else {
                            Toasty.message(this, "Not Approved");
                            pending();
                        }

                   } else {
                       Toasty.message(this, "Data is null");
                   }
               } else {
                   Toasty.message(this, "It doesn't exists");
               }
           } else {
               Toasty.message(this, "Task was not successful");
           }
        });
    }
    private void pending() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, new PendingFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }





}