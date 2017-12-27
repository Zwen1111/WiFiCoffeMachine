package com.example.zwen1.wifi_coffee_machine;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private boolean turnedOn;
    private String coffeePart;
    private ImageView coffeeView;

    private Bitmap d0;
    private Bitmap d1;
    private Bitmap d2;
    private Bitmap d3;

    private boolean on;

    private Timer timer;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button makeCoffee = findViewById(R.id.activity_main_make_coffee_btn_id);
        coffeeView = findViewById(R.id.activity_main_coffee_view_id);

        d0 = BitmapFactory.decodeResource(getResources(), R.drawable.cup_of_coffee_part0);
        d1 = BitmapFactory.decodeResource(getResources(), R.drawable.cup_of_coffee_part1);
        d2 = BitmapFactory.decodeResource(getResources(), R.drawable.cup_of_coffee_part2);
        d3 = BitmapFactory.decodeResource(getResources(), R.drawable.cup_of_coffee_part3);

        on = false;

        requestQueue = Volley.newRequestQueue(this);

        makeCoffee.setOnClickListener(v -> {
            on = !on;

            coffeePart = "part0";

            String json = "{" +
                    "turnOn:" + on +
                    "}";

            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!turnedOn) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.102:8080", jsonObject, onSuccess, onError);
                requestQueue.add(jsonObjectRequest);
                requestQueue.start();
            }
        });
    }

    private TimerTask coffeeAnimation(){
        return new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    switch (coffeePart){
                        case "part0":
                            coffeePart = "part1";
                            coffeeView.setImageBitmap(d1);
                            break;
                        case "part1":
                            coffeePart = "part2";
                            coffeeView.setImageBitmap(d2);
                            break;
                        case "part2":
                            coffeePart = "part3";
                            coffeeView.setImageBitmap(d3);
                            break;
                        case "part3":
                            coffeePart = "part0";
                            coffeeView.setImageBitmap(d0);
                            break;
                    }

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "http://192.168.0.102:8080", null, response -> {
                        try {
                            int done = response.getInt("done");
                            if (done == 1){
                                timer.cancel();
                                timer = new Timer();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, onError);

                    requestQueue.add(jsonObjectRequest);
                    requestQueue.start();
                });
            }
        };
    }

    private Response.Listener<JSONObject> onSuccess = (JSONObject response) -> {
        try {
            int result = response.getInt("makingCoffee");
            turnedOn = false;
            if (result == 1){
                turnedOn = true;
                Toast.makeText(this, "Making coffee", Toast.LENGTH_LONG);
            }

            timer = new Timer();
            timer.schedule(coffeeAnimation(), 1000, 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    private Response.ErrorListener onError = (VolleyError error) -> {
        Toast.makeText(this, "Kan JSON niet ontvangen", Toast.LENGTH_SHORT);
    };
}
