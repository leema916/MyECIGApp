package com.example.appecig;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;

public class Cards extends AppCompatActivity implements View.OnClickListener {
    MaterialCardView adminCard, teacherCard, studentCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cards);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        studentCard = findViewById(R.id.studentCard);
        teacherCard = findViewById(R.id.teacherCard);
        adminCard = findViewById(R.id.adminCard);

        studentCard.setOnClickListener(this);
        adminCard.setOnClickListener(this);
        teacherCard.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {

        Bundle bundle = new Bundle();

        if(v.getId() == R.id.studentCard){

            bundle.putString("role", "Etudiant");
            Navigation.findNavController(this, R.id.cardsNavHost)
                    .navigate(R.id.signup, bundle);

        }
        else if (v.getId() == R.id.teacherCard) {

            bundle.putString("role", "Enseignant");
            Navigation.findNavController(this, R.id.cardsNavHost)
                    .navigate(R.id.signup, bundle);
        }

        else if (v.getId() == R.id.adminCard) {
            bundle.putString("role", "Admin");
            Navigation.findNavController(this, R.id.cardsNavHost)
                    .navigate(R.id.login, bundle);

        }
    }


}