package com.socialherb.openeyes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AlcoholCalculator extends AppCompatActivity {

    ListView ls;
    String text;
    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alcohol_calculator);

        TextView addDrinks = findViewById(R.id.add_drink);

        final ArrayList<String> drinksArray = new ArrayList<String>();

        ls = findViewById(R.id.drinks_list);
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,drinksArray);

        final EditText amount = findViewById(R.id.amount);

        final Spinner drinks = findViewById(R.id.drinks);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numberOfDrinks, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drinks.setAdapter(adapter);

        ImageView iconBtn = findViewById(R.id.icon);

        iconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlcoholCalculator.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        addDrinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                text = drinks.getSelectedItem().toString();

                String acloholAmount = amount.getText().toString();

                drinksArray.add(text + " - " + acloholAmount);
                listAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,drinksArray);

                amount.setText("");


                ls.setAdapter(listAdapter);




            }
        });


    }
}
