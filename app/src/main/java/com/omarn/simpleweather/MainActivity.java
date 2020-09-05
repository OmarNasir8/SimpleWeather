package com.omarn.simpleweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    int lat;
    int log;
    double latD;
    double logD;
    TextView temp;
    TextView feelsLike;
    TextView description;
    ImageView tempIMG;
    TextView place;
    List<Address> addressList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temp = findViewById(R.id.Temprature);
        feelsLike = findViewById(R.id.feelsLike);
        description = findViewById(R.id.Description);
        tempIMG = findViewById(R.id.currentTempIMG);
        place = findViewById(R.id.place);


        getLatLog();

    }

    public void getLatLog() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = (int) location.getLatitude();
                log = (int) location.getLongitude();
                latD = location.getLatitude();
                logD =  location.getLongitude();
                getWeather();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            }
        }
    }

    public void getWeather() {

        try {
            String result = "";

            DownloadTask task = new DownloadTask();

            System.out.println(lat);
            System.out.println(log);

            result = task.execute("https://samples.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=439d4b804bc8187953eb36d2a8c26a02").get();

            //"api.openweathermap.org/data/2.5/weather?lat=" + Integer.toString(lat) + "&lon=" + Integer.toString(log) + "&appid=fc32f6e9bcb24f59327436c05a2080fc"
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {

                String result = "";
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();

                while(data != -1) {
                    char current = (char)data;
                    result = result + current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                super.onPostExecute(s);


                JSONObject jsonObject = new JSONObject(s);

                System.out.println(s);

                String weatherInfo = jsonObject.getString("weather");

                String tempP = jsonObject.getString("main");

                String tempCut = tempP.substring(8,13);

                String feelCut = tempP.substring(25,30);

                String comTemp = "";

                String feelTemp = "";

                for(int i = 0; i < tempCut.length(); i++) {
                    if(tempCut.charAt(i) != '.' && tempCut.charAt(i) != ',') {
                        comTemp = comTemp + tempCut.charAt(i);
                    } else {
                        break;
                    }
                }

                temp.setText(comTemp + "C");

                int tempnum = Integer.parseInt(comTemp);

                if(tempnum < 10) {
                    tempIMG.setImageResource(R.drawable.cold);
                } else if(tempnum >= 10 && tempnum <= 29) {
                    tempIMG.setImageResource(R.drawable.thermometer);
                } else if (tempnum >= 30 && tempnum < 40) {
                    tempIMG.setImageResource(R.drawable.hot);
                } else if (tempnum >= 40) {
                    tempIMG.setImageResource(R.drawable.hotasf);
                }

                for(int i = 0; i < feelCut.length(); i++) {
                    if(feelCut.charAt(i) != '.' && feelCut.charAt(i) !='"' && feelCut.charAt(i) != ':' && feelCut.charAt(i) != 'e') {
                        feelTemp = feelTemp + feelCut.charAt(i);
                    } else if (feelCut.charAt(i) == '.'){
                        break;
                    }
                }




                feelsLike.setText("Feels Like: " + feelTemp + "C" );

                JSONArray arr = new JSONArray(weatherInfo);


                for(int i = 0; i < arr.length(); i++) {
                    JSONObject jsonPArt = arr.getJSONObject(i);

                    description.setText(jsonPArt.getString("main"));
                    //text.setText(jsonPArt.getString("description"));
                }

                if(addressList != null && addressList.size() > 0) {
                    String address = "";

                    if (addressList.get(0).getLocality() != null) {
                        address = address + addressList.get(0).getLocality() + ", ";
                    }
                    if (addressList.get(0).getAdminArea() != null) {
                        address = address + addressList.get(0).getAdminArea();
                    }

                    place.setText(address);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
