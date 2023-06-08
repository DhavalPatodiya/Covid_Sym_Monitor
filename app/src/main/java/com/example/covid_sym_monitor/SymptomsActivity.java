package com.example.covid_sym_monitor;

//Symptoms Activity Class contains all the button listeners and processes done in the symptoms logging page.


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class SymptomsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    DatabaseHandler dbHandler;

    RatingBar ratingBar;
    HashMap<String, Float> symptoms;

    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);
        Intent intent = getIntent();
        symptoms = (HashMap<String, Float>)intent.getSerializableExtra("symptom");
        dbHandler = new DatabaseHandler(SymptomsActivity.this);

        // Setting up the dropdown menu
        spinner = (Spinner) findViewById(R.id.symptomsDropDown);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.symptoms, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        // Listening to the rating bar
        ratingBar = (RatingBar) findViewById(R.id.rating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                String spin_text = spinner.getSelectedItem().toString();
                if(symptoms.containsKey(spin_text)) {
                    symptoms.put(spin_text, rating);
                }
            }
        });

        // Button to upload the symptoms rating into the database
        Button upload_btn = (Button)findViewById(R.id.upload);
        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean success = dbHandler.addOne(symptoms);
                if (success) {
                    Toast.makeText(SymptomsActivity.this, "Symptoms Uploaded", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    // Listener for the dropdown menu
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        String selected_item = (String) arg0.getItemAtPosition(position);
        Float rating = symptoms.get(selected_item);
        ratingBar.setRating(rating);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
