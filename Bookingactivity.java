package com.example.flightapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flightapp.model.Flight;
import com.example.flightapp.network.ApiClient;
import com.example.flightapp.network.ApiService;
import com.example.flightapp.util.PrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {
    private static final String EXTRA_FLIGHT = "extra_flight";
    ApiService api;
    PrefManager pref;

    public static void start(Context ctx, Flight f) {
        Intent i = new Intent(ctx, BookingActivity.class);
        i.putExtra(EXTRA_FLIGHT, new GsonWrapper().toJson(f)); // small utility below or use Parcelables
        ctx.startActivity(i);
    }

    Flight flight;
    TextView tvInfo;
    EditText etSeats;
    Button btnBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking); // you need to create layout
        api = ApiClient.getApiService();
        pref = new PrefManager(this);

        String flightJson = getIntent().getStringExtra(EXTRA_FLIGHT);
        flight = new GsonWrapper().fromJson(flightJson, Flight.class);

        tvInfo = findViewById(R.id.tvInfo);
        etSeats = findViewById(R.id.etSeats);
        btnBook = findViewById(R.id.btnBook);

        tvInfo.setText(flight.airline + " " + flight.flight_number + "\n" + flight.src + " → " + flight.dest + "\nPrice: ₹" + flight.price);

        btnBook.setOnClickListener(v -> {
            int seats = Integer.parseInt(etSeats.getText().toString());
            Map<String, Object> body = new HashMap<>();
            body.put("flight_id", flight.id);
            body.put("seats", seats);
            String auth = "Bearer " + pref.getToken();
            api.bookFlight(auth, body).enqueue(new Callback<Map<String,Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    // handle response - show confirmation
                }
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) { t.printStackTrace(); }
            });
        });
    }
}
