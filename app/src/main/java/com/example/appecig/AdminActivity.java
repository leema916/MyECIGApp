package com.example.appecig;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminActivity extends AppCompatActivity {
    TextView adminNameId, adminEmailID;
    MaterialCardView signUpReuestsCard;
    FirebaseAuth fbAuth;
    private ProgressBar progressBar;
    NavController navController;
    ImageView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adminNameId = findViewById(R.id.adminNameID);
        adminEmailID = findViewById(R.id.adminEmailID);
        getAdminInfos(adminNameId);
        logout = findViewById(R.id.logout);
        progressBar = findViewById(R.id.progressBar);

        fbAuth = FirebaseAuth.getInstance();

        signUpReuestsCard = findViewById(R.id.singUpRequestsCard);
        signUpReuestsCard.setOnClickListener(v -> {
            Navigation.findNavController(this, R.id.adminNavHost).navigate(R.id.signUpRequests);
        });

        logout.setOnClickListener(v -> logout());

        // ======= CREATE ADMIN AUTOMATICALLY =======
        /* FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> adminUser = new HashMap<>();
        adminUser.put("name", "NewAdmin");
        adminUser.put("email", "newadmin@example.com");
        adminUser.put("role", "Admin");
        adminUser.put("approved", true);

        db.collection("users").document("admin1")
                .set(adminUser)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Admin created successfully!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error creating admin", e));

         */
        // ======= END OF CODE =======
    }

    private void getAdminInfos(TextView nom) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("role", "Admin")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userEmail = document.getString("email");
                        String userName = document.getString("name");
                        nom.setText("Bonjour " + userName);
                        adminEmailID.setText(userEmail);
                    }
                });
    }

    private void logout() {
        fbAuth.signOut();
        progressBar.setVisibility(ProgressBar.GONE);
        finish();
    }
}
