package com.example.flightapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.flightapp.model.Flight;
import com.example.flightapp.network.ApiClient;
import com.example.flightapp.network.ApiService;
import com.example.flightapp.util.PrefManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    EditText etSrc, etDest, etDate;
    Button btnSearch;
    RecyclerView rvResults;
    ApiService api;
    FlightsAdapter adapter;
    PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        api = ApiClient.getApiService();
        pref = new PrefManager(this);

        etSrc = findViewById(R.id.etSrc);
        etDest = findViewById(R.id.etDest);
        etDate = findViewById(R.id.etDate);
        btnSearch = findViewById(R.id.btnSearch);
        rvResults = findViewById(R.id.rvResults);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FlightsAdapter(this, flight -> {
            // click -> open booking screen
            BookingActivity.start(SearchActivity.this, flight);
        });
        rvResults.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String src = etSrc.getText().toString().trim();
            String dest = etDest.getText().toString().trim();
            String date = etDate.getText().toString().trim(); // format YYYY-MM-DD
            api.searchFlights(src, dest, date).enqueue(new Callback<Map<String, List<Flight>>>() {
                @Override
                public void onResponse(Call<Map<String, List<Flight>>> call, Response<Map<String, List<Flight>>> response) {
                    if (!response.isSuccessful() || response.body() == null) return;
                    List<Flight> flights = response.body().get("flights");
                    adapter.setFlights(flights);
                }
                @Override public void onFailure(Call<Map<String, List<Flight>>> call, Throwable t) { t.printStackTrace(); }
            });
        });
    }
}
