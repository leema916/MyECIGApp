package com.example.appecig;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.appecig.util.Toasty;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {
    EditText nameEt, emailEt, passwordEt, confirmPasswordEt;
    Spinner fieldSpinner,groupSpinner;
    private ArrayAdapter<String> fieldAdapter, groupAdapter;

    TextView roleTxt, loginLien, txtField,txtGrp;
    AppCompatButton SignUpBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    String name, email, password, uid, role,filiere, groupe;
    Map<String, Object> userMap = new HashMap<>();
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);

        nameEt = findViewById(R.id.nameId);
        emailEt = findViewById(R.id.emailId);
        passwordEt = findViewById(R.id.passwordId);
        fieldSpinner = findViewById(R.id.filiere);
        groupSpinner = findViewById(R.id.groupe);
         txtField= findViewById(R.id.txtfieliere);
        txtGrp = findViewById(R.id.txtgroup);
        groupSpinner = findViewById(R.id.groupe);
        confirmPasswordEt = findViewById(R.id.confirmPasswordId);
        SignUpBtn = findViewById(R.id.SignUpBtn);
        roleTxt = findViewById(R.id.roleTxt);
        loginLien = findViewById(R.id.loginLien);

        Bundle bundle = getIntent().getExtras();
        String getRole = bundle.getString("role");
        roleTxt.setText(getRole);
        fieldAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(fieldAdapter);

        groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupAdapter);

        if (roleTxt.getText().toString().equals("Etudiant"))  {
            fetchAndPopulateFields();
            fetchAndPopulateGroups();
            fieldSpinner.setVisibility(View.VISIBLE);
            groupSpinner.setVisibility(View.VISIBLE);
            txtField.setVisibility(View.VISIBLE);
            txtGrp.setVisibility(View.VISIBLE);

        } else {
            fieldSpinner.setVisibility(View.GONE);
            groupSpinner.setVisibility(View.GONE);
            txtField.setVisibility(View.GONE);
            txtGrp.setVisibility(View.GONE);
        }

        loginLien.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
        });


        SignUpBtn.setOnClickListener(v -> {
            name = nameEt.getText().toString();
            email = emailEt.getText().toString().trim();
            password = passwordEt.getText().toString().trim();

            if (validateInput(name, email, password)) {
                progressBar.setVisibility(View.VISIBLE);
                signUp();
            }
        });
    }
    private void fetchAndPopulateFields() {

        firestore.collection("fields")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            fieldAdapter.clear(); // Clear any existing items
                            for (DocumentSnapshot document : task.getResult()) {
                                fieldAdapter.add(document.getString("name")); //Assuming 'name' field in fields collection
                            }
                            fieldAdapter.notifyDataSetChanged();
                            // Fetch groups based on selected field

                        } else {
                            // Handle error
                            Toast.makeText(Signup.this, "Error fetching fields: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void fetchAndPopulateGroups() {

        firestore.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            groupAdapter.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                groupAdapter.add(document.getString("name"));
                            }
                            groupAdapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(Signup.this, "Error fetching groups: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    private boolean validateInput(String name, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            Toasty.message(this,"Please enter your name");
            return false;
        }

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

    private void pending() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, new PendingFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void signUp() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        uid = task.getResult().getUser().getUid();

                        filiere = fieldSpinner.getSelectedItem().toString();
                        groupe = groupSpinner.getSelectedItem().toString();
                    } else {
                        Toasty.message(this, "Something went wrong!: " +
                                task.getException().getMessage());
                        return;
                    }
                    role = roleTxt.getText().toString();
                    if (task.isSuccessful()) {

                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("password", password);
                        userMap.put("filiere", filiere);
                        userMap.put("group", groupe);
                        userMap.put("role", role);
                        userMap.put("approved", false);

                        sendToFirestore();

                    } else {
                        Toast.makeText(Signup.this, "Registration failed: ***  " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
    }


    private void sendToFirestore() {
        firestore.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(documentReference -> {
                    Toasty.message(this, "Registration successful!");

                    // Navigate based on role
                    switch (role) {
                        case "Etudiant":
                            pending();
                            break;
                        case "Enseignant":
                            startActivity(new Intent(Signup.this, TeacherActivity.class));
                            finish();
                            break;
                        case "Admin":
                            startActivity(new Intent(Signup.this, AdminActivity.class));
                            finish();
                            break;
                        default:
                            Toasty.message(this, "Role not recognized");
                    }

                })
                .addOnFailureListener(e -> {
                    Toasty.message(this, "Registration failed: " + e.getMessage());
                });
    }

}