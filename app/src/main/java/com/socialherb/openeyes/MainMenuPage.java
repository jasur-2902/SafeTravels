package com.socialherb.openeyes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainMenuPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_page);

        ImageView mUserIcon = findViewById(R.id.icon);

        mUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenuPage.this, ProfileActivity.class);
                startActivity(intent);




            }
        });

        TextView mDriveSafe = findViewById(R.id.drive_safe);

        mDriveSafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenuPage.this, MainActivity.class);
                startActivity(intent);
            }
        });


        TextView mCalculatorBtn = findViewById(R.id.calculatorBtn);

        mCalculatorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenuPage.this, AlcoholCalculator.class);
                startActivity(intent);
            }
        });

    }
}
