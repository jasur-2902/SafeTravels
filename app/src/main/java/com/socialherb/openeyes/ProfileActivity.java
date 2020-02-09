package com.socialherb.openeyes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    SharedPreferences.Editor myEdit;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        String s1 = sharedPreferences.getString("name", "");
        String weightInitial = sharedPreferences.getString("weight", "");
        String heightInitial = sharedPreferences.getString("height", "");
        int gender = sharedPreferences.getInt("gender", 0);

        myEdit = sharedPreferences.edit();

        TextView submit = findViewById(R.id.submit);

        final EditText nameField = findViewById(R.id.nameField);
        final EditText weighField = findViewById(R.id.amount);
        final EditText heightField = findViewById(R.id.height);

        final RadioGroup rg = findViewById(R.id.gender);

        if(s1 != null){
            nameField.setText(s1);
        }

        if(weightInitial != null ){
            weighField.setText(weightInitial);
        }

        if(heightField != null){
            heightField.setText(heightInitial);
        }

        if(gender != 0){
            rg.check(gender);
        }


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myEdit.putString("name", nameField.getText().toString());
                myEdit.putString("weight", weighField.getText().toString());
                myEdit.putString("height", heightField.getText().toString());

                // get selected radio button from radioGroup
                int selectedId = rg.getCheckedRadioButtonId();

                // find the radiobutton by returned id

                myEdit.putInt("gender", selectedId);


                myEdit.commit();

            }
        });


    }
}
