//package com.example.tollcalculator;
//import android.location.Location;
//import android.os.Bundle;
//import android.util.Log;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.opencsv.CSVReader;
//import com.opencsv.exceptions.CsvException;
//
//import org.xmlpull.v1.XmlPullParser;
//import org.xmlpull.v1.XmlPullParserException;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MainActivity extends AppCompatActivity {
//
//    private static final double TOLL_RADIUS_METERS = 15000; // 15 km radius
//    private static final double TOLL_RATE_PER_METER = 3.2857; // Example rate
//    private List<TollZone> tollZones = new ArrayList<>();
//    private List<GPSPoint> gpsCoordinates = new ArrayList<>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        loadTollZones();
//        loadGPXData();
//        double tollDistance = calculateTollDistance();
//        double tollFee = (tollDistance * TOLL_RATE_PER_METER) / 1000;
//
//        Log.d("TollInfo", "Total Toll Distance: " + tollDistance + " meters");
//        Log.d("TollInfo", "Total Toll Fee: ₹" + tollFee);
//    }
//
//    private void loadTollZones() {
//        try {
//            InputStream inputStream = getResources().openRawResource(R.raw.toll_data);
//            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
//            List<String[]> rows = reader.readAll();
//            for (String[] row : rows) {
//                String name = row[0];
//                double lat = Double.parseDouble(row[1]);
//                double lon = Double.parseDouble(row[2]);
//                tollZones.add(new TollZone(name, lat, lon));
//            }
//        } catch (IOException | CsvException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void loadGPXData() {
//        try {
//            InputStream inputStream = getResources().openRawResource(R.raw.gps_route);
//            XmlPullParser parser = getResources().getXml(R.xml.gps_route);
//            int eventType = parser.getEventType();
//            double lat = 0, lon = 0;
//
//            while (eventType != XmlPullParser.END_DOCUMENT) {
//                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("trkpt")) {
//                    lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
//                    lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
//                    gpsCoordinates.add(new GPSPoint(lat, lon));
//                }
//                eventType = parser.next();
//            }
//        } catch (XmlPullParserException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private double calculateTollDistance() {
//        double totalDistance = 0.0;
//        GPSPoint previousPoint = null;
//        TollZone insideZone = null;
//
//        for (GPSPoint point : gpsCoordinates) {
//            TollZone currentZone = isWithinTollZone(point);
//            if (currentZone != null) {
//                if (insideZone != currentZone) {
//                    Log.d("TollInfo", "Entering: " + currentZone.name);
//                }
//                if (previousPoint != null) {
//                    totalDistance += distanceBetween(previousPoint, point);
//                }
//            } else if (insideZone != null) {
//                Log.d("TollInfo", "Exiting: " + insideZone.name);
//            }
//            insideZone = currentZone;
//            previousPoint = point;
//        }
//        return totalDistance;
//    }
//
//    private TollZone isWithinTollZone(GPSPoint point) {
//        for (TollZone zone : tollZones) {
//            if (distanceBetween(point, new GPSPoint(zone.lat, zone.lon)) <= TOLL_RADIUS_METERS) {
//                return zone;
//            }
//        }
//        return null;
//    }
//
//    private double distanceBetween(GPSPoint p1, GPSPoint p2) {
//        float[] results = new float[1];
//        Location.distanceBetween(p1.lat, p1.lon, p2.lat, p2.lon, results);
//        return results[0];
//    }
//
//    class TollZone {
//        String name;
//        double lat, lon;
//        TollZone(String name, double lat, double lon) {
//            this.name = name;
//            this.lat = lat;
//            this.lon = lon;
//        }
//    }
//
//    class GPSPoint {
//        double lat, lon;
//        GPSPoint(double lat, double lon) {
//            this.lat = lat;
//            this.lon = lon;
//        }
//    }
//}


//
//package com.example.tollcalculator;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.location.Location;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.opencsv.CSVReader;
//import com.opencsv.exceptions.CsvException;
//
//import org.xmlpull.v1.XmlPullParser;
//import org.xmlpull.v1.XmlPullParserException;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class MainActivity extends AppCompatActivity {
//
//    private List<TollZone> tollZones = new ArrayList<>();
//    private static final int TOLL_RADIUS = 15000; // 15 km
//    private static final int LOCATION_UPDATE_INTERVAL = 100; // 2 seconds per coordinate
//
//    private TextView tollStatus, coordinatesText;
//    private Button demoButton, summaryButton;
//    private List<Location> gpxLocations = new ArrayList<>();
//    private Handler handler = new Handler();
//    private int currentLocationIndex = 0;
//    private double totalCost = 0;
//    private Set<TollZone> tollsPassed = new HashSet<>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        tollStatus = findViewById(R.id.tollStatus);
//        coordinatesText = findViewById(R.id.coordinatesText);
//        demoButton = findViewById(R.id.demoButton);
//        summaryButton = findViewById(R.id.summaryButton);
//
//        summaryButton.setEnabled(false); // Disabled initially
//
//        loadTollZonesFromCSV();
//
//        demoButton.setOnClickListener(v -> openFilePicker());
//        summaryButton.setOnClickListener(v -> showTripSummary());
//    }
//
//    private void loadTollZonesFromCSV() {
//        try (CSVReader reader = new CSVReader(new FileReader(getExternalFilesDir(null) + "/coordinates_india.csv"))) {
//            List<String[]> rows = reader.readAll();
//            for (String[] row : rows) {
//                double lat = Double.parseDouble(row[1]);
//                double lon = Double.parseDouble(row[2]);
//                double cost = Double.parseDouble(row[3]); // Ensure CSV has cost
//                tollZones.add(new TollZone(row[0], lat, lon, cost, TOLL_RADIUS));
//            }
//        } catch (IOException | CsvException e) {
//            Log.e("TollDetection", "Error reading CSV", e);
//        }
//    }
//
//    private void openFilePicker() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*");
//        filePickerLauncher.launch(intent);
//    }
//
//    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
//                    Uri uri = result.getData().getData();
//                    if (uri != null) {
//                        loadGPXFile(uri);
//                    }
//                }
//            });
//
//    private void loadGPXFile(Uri uri) {
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(uri);
//            if (inputStream != null) {
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                gpxLocations = parseGPX(reader);
//                inputStream.close();
//
//                if (!gpxLocations.isEmpty()) {
//                    Toast.makeText(this, "GPX Loaded! Simulating movement...", Toast.LENGTH_SHORT).show();
//                    totalCost = 0;
//                    tollsPassed.clear();
//                    summaryButton.setEnabled(false);
//                    simulateMovement();
//                } else {
//                    Toast.makeText(this, "No valid GPS data found in GPX file!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        } catch (IOException e) {
//            Log.e("GPX Processing", "Error reading GPX file", e);
//        }
//    }
//
//    private List<Location> parseGPX(BufferedReader reader) {
//        List<Location> locations = new ArrayList<>();
//        try {
//            XmlPullParser parser = android.util.Xml.newPullParser();
//            parser.setInput(reader);
//            int eventType = parser.getEventType();
//            Location currentLocation = null;
//
//            while (eventType != XmlPullParser.END_DOCUMENT) {
//                if (eventType == XmlPullParser.START_TAG && "trkpt".equals(parser.getName())) {
//                    double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
//                    double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
//                    currentLocation = new Location("GPX");
//                    currentLocation.setLatitude(lat);
//                    currentLocation.setLongitude(lon);
//                } else if (eventType == XmlPullParser.END_TAG && "trkpt".equals(parser.getName()) && currentLocation != null) {
//                    locations.add(currentLocation);
//                    currentLocation = null;
//                }
//                eventType = parser.next();
//            }
//        } catch (XmlPullParserException | IOException e) {
//            Log.e("GPX Parsing", "Error parsing GPX file", e);
//        }
//        return locations;
//    }
//
//    private void simulateMovement() {
//        if (gpxLocations.isEmpty()) return;
//        currentLocationIndex = 0;
//        handler.post(locationUpdateRunnable);
//    }
//
//    private final Runnable locationUpdateRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (currentLocationIndex < gpxLocations.size()) {
//                Location location = gpxLocations.get(currentLocationIndex);
//                updateUIWithLocation(location);
//                checkTollZone(location.getLatitude(), location.getLongitude());
//                currentLocationIndex++;
//                handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
//            } else {
//                summaryButton.setEnabled(true);
//                Toast.makeText(MainActivity.this, "GPX Route Simulation Complete!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    };
//
//    private void updateUIWithLocation(Location location) {
//        coordinatesText.setText("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
//    }
//
//    private void checkTollZone(double lat, double lon) {
//        for (TollZone zone : tollZones) {
//            float[] results = new float[1];
//            Location.distanceBetween(lat, lon, zone.getLatitude(), zone.getLongitude(), results);
//            if (results[0] <= TOLL_RADIUS) {
//                tollsPassed.add(zone);
//                totalCost += zone.getCost();
//                tollStatus.setText("Inside Toll: " + zone.getName());
//                return;
//            }
//        }
//        tollStatus.setText("No Toll Zone Nearby");
//    }
//
//    private void showTripSummary() {
//        StringBuilder summary = new StringBuilder();
//        summary.append("Trip Summary:\n");
//        summary.append("Total Tolls Passed: ").append(tollsPassed.size()).append("\n");
//        summary.append("Total Cost: ₹").append(totalCost).append("\n\n");
//        summary.append("Tolls Passed:\n");
//
//        for (TollZone toll : tollsPassed) {
//            summary.append(toll.getName()).append(" - ₹").append(toll.getCost()).append("\n");
//        }
//
//        Toast.makeText(this, summary.toString(), Toast.LENGTH_LONG).show();
//    }
//}





package com.example.tollcalculator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<TollZone> tollZones = new ArrayList<>();
    private static final int TOLL_RADIUS = 15000; // 15 km
    private static final int LOCATION_UPDATE_INTERVAL = 100; // 2 seconds per coordinate

    private TextView tollStatus, coordinatesText;
    private Button demoButton, tripSummaryButton;
    private List<Location> gpxLocations = new ArrayList<>();
    private Handler handler = new Handler();
    private int currentLocationIndex = 0;
    private double totalDistance = 0;
    private List<TollZone> detectedTolls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tollStatus = findViewById(R.id.tollStatus);
        coordinatesText = findViewById(R.id.coordinatesText);
        demoButton = findViewById(R.id.demoButton);
        tripSummaryButton = findViewById(R.id.tripSummaryButton);

        // Load Toll Data from CSV
        loadTollZonesFromCSV();

        // Set click listeners
        demoButton.setOnClickListener(v -> openFilePicker());
        tripSummaryButton.setOnClickListener(v -> showTripSummary());
        tripSummaryButton.setEnabled(false); // Initially disabled
    }

    private void loadTollZonesFromCSV() {
        try (CSVReader reader = new CSVReader(new FileReader(getExternalFilesDir(null) + "/coordinates_india.csv"))) {
            List<String[]> rows = reader.readAll();
            for (String[] row : rows) {
                double lat = Double.parseDouble(row[1]);
                double lon = Double.parseDouble(row[2]);
                tollZones.add(new TollZone(row[0], lat, lon, TOLL_RADIUS));
            }
        } catch (IOException | CsvException e) {
            Log.e("TollDetection", "Error reading CSV", e);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/gpx+xml", "text/xml", "application/octet-stream"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        loadGPXFile(uri);
                    }
                }
            });

    private void loadGPXFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                gpxLocations = parseGPX(reader);
                inputStream.close();

                if (!gpxLocations.isEmpty()) {
                    Toast.makeText(this, "GPX File Loaded! Simulating movement...", Toast.LENGTH_SHORT).show();
                    simulateMovement();
                } else {
                    Toast.makeText(this, "No valid GPS data found in GPX file!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            Log.e("GPX Processing", "Error reading GPX file", e);
        }
    }

    private List<Location> parseGPX(BufferedReader reader) {
        List<Location> locations = new ArrayList<>();
        try {
            XmlPullParser parser = android.util.Xml.newPullParser();
            parser.setInput(reader);
            int eventType = parser.getEventType();
            Location currentLocation = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "trkpt".equals(parser.getName())) {
                    double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                    double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                    currentLocation = new Location("GPX");
                    currentLocation.setLatitude(lat);
                    currentLocation.setLongitude(lon);
                } else if (eventType == XmlPullParser.END_TAG && "trkpt".equals(parser.getName()) && currentLocation != null) {
                    locations.add(currentLocation);
                    currentLocation = null;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e("GPX Parsing", "Error parsing GPX file", e);
        }
        return locations;
    }

    private void simulateMovement() {
        if (gpxLocations.isEmpty()) return;
        currentLocationIndex = 0;
        handler.post(locationUpdateRunnable);
    }

    private final Runnable locationUpdateRunnable = new Runnable() {
        Location lastLocation = null;

        @Override
        public void run() {
            if (currentLocationIndex < gpxLocations.size()) {
                Location location = gpxLocations.get(currentLocationIndex);
                updateUIWithLocation(location);
                if (lastLocation != null) totalDistance += lastLocation.distanceTo(location);
                lastLocation = location;

                checkTollZone(location.getLatitude(), location.getLongitude());
                currentLocationIndex++;
                handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            } else {
                runOnUiThread(() -> tripSummaryButton.setEnabled(true));
            }
        }
    };

    private void showTripSummary() {
        new AlertDialog.Builder(this)
                .setTitle("Trip Summary")
                .setMessage("Total Distance: " + (totalDistance / 1000) + " km\nTolls Passed: " + detectedTolls.size())
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateUIWithLocation(Location location) {
        runOnUiThread(() -> {
            String latLonText = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
            coordinatesText.setText(latLonText);
        });
    }

    private void checkTollZone(double lat, double lon) {
        runOnUiThread(() -> {
            boolean inTollZone = false;

            for (TollZone zone : tollZones) {
                float[] results = new float[1];
                Location.distanceBetween(lat, lon, zone.getLatitude(), zone.getLongitude(), results);

                if (results[0] <= TOLL_RADIUS) { // If within the radius of a toll zone
                    tollStatus.setText("Inside Toll Zone: " + zone.getName());
                    inTollZone = true;
                    break;
                }
            }

            if (!inTollZone) {
                tollStatus.setText("No Toll Zone Nearby");
            }
        });
    }

}



//
//package com.example.tollcalculator;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.location.Location;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//
//import org.xmlpull.v1.XmlPullParser;
//import org.xmlpull.v1.XmlPullParserException;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class MainActivity extends AppCompatActivity {
//
//    private List<TollZone> tollZones = new ArrayList<>();
//    private static final double DEG_TO_KM = 1 / 111.0; // Approximate conversion
//    private static final int LOCATION_UPDATE_INTERVAL = 100; // Simulate 2s per coordinate
//
//    private TextView tollStatus, coordinatesText;
//    private Button demoButton, summaryButton;
//    private List<Location> gpxLocations = new ArrayList<>();
//    private Handler handler = new Handler();
//    private int currentLocationIndex = 0;
//    private double totalCost = 0, tollDistance = 0;
//    private Set<TollZone> tollsPassed = new HashSet<>();
//    private Location prevLocation = null;
//    private TollZone insideZone = null;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        tollStatus = findViewById(R.id.tollStatus);
//        coordinatesText = findViewById(R.id.coordinatesText);
//        demoButton = findViewById(R.id.demoButton);
//        summaryButton = findViewById(R.id.summaryButton);
//
//        summaryButton.setEnabled(false); // Disabled initially
//        loadTollZonesFromCSV();
//
//        demoButton.setOnClickListener(v -> openFilePicker());
//        summaryButton.setOnClickListener(v -> showTripSummary());
//    }
//
//    private void loadTollZonesFromCSV() {
//        try (BufferedReader reader = new BufferedReader(new FileReader(getExternalFilesDir(null) + "/coordinates_india.csv"))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] row = line.split(",");
//                double lat = Double.parseDouble(row[1]);
//                double lon = Double.parseDouble(row[2]);
//                double cost = Double.parseDouble(row[3]);
//                tollZones.add(new TollZone(row[0], lat, lon, cost, 30 * 1000)); // 30 km radius
//            }
//        } catch (IOException e) {
//            Log.e("TollDetection", "Error reading CSV", e);
//        }
//    }
//
//    private void openFilePicker() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*");
//        filePickerLauncher.launch(intent);
//    }
//
//    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
//                    Uri uri = result.getData().getData();
//                    if (uri != null) {
//                        loadGPXFile(uri);
//                    }
//                }
//            });
//
//    private void loadGPXFile(Uri uri) {
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(uri);
//            if (inputStream != null) {
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                gpxLocations = parseGPX(reader);
//                inputStream.close();
//
//                if (!gpxLocations.isEmpty()) {
//                    Toast.makeText(this, "GPX Loaded! Simulating movement...", Toast.LENGTH_SHORT).show();
//                    totalCost = 0;
//                    tollDistance = 0;
//                    tollsPassed.clear();
//                    summaryButton.setEnabled(false);
//                    simulateMovement();
//                } else {
//                    Toast.makeText(this, "No valid GPS data found in GPX file!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        } catch (IOException e) {
//            Log.e("GPX Processing", "Error reading GPX file", e);
//        }
//    }
//
//    private List<Location> parseGPX(BufferedReader reader) {
//        List<Location> locations = new ArrayList<>();
//        try {
//            XmlPullParser parser = android.util.Xml.newPullParser();
//            parser.setInput(reader);
//            int eventType = parser.getEventType();
//            Location currentLocation = null;
//
//            while (eventType != XmlPullParser.END_DOCUMENT) {
//                if (eventType == XmlPullParser.START_TAG && "trkpt".equals(parser.getName())) {
//                    double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
//                    double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
//                    currentLocation = new Location("GPX");
//                    currentLocation.setLatitude(lat);
//                    currentLocation.setLongitude(lon);
//                } else if (eventType == XmlPullParser.END_TAG && "trkpt".equals(parser.getName()) && currentLocation != null) {
//                    locations.add(currentLocation);
//                    currentLocation = null;
//                }
//                eventType = parser.next();
//            }
//        } catch (XmlPullParserException | IOException e) {
//            Log.e("GPX Parsing", "Error parsing GPX file", e);
//        }
//        return locations;
//    }
//
//    private void simulateMovement() {
//        if (gpxLocations.isEmpty()) return;
//        currentLocationIndex = 0;
//        handler.post(locationUpdateRunnable);
//    }
//
//    private final Runnable locationUpdateRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (currentLocationIndex < gpxLocations.size()) {
//                Location location = gpxLocations.get(currentLocationIndex);
//                updateUIWithLocation(location);
//                checkTollZone(location);
//                currentLocationIndex++;
//                handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
//            } else {
//                summaryButton.setEnabled(true);
//                Toast.makeText(MainActivity.this, "GPX Route Simulation Complete!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    };
//
//    private void updateUIWithLocation(Location location) {
//        coordinatesText.setText("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
//    }
//
//    private void checkTollZone(Location location) {
//        TollZone currentZone = getNearestTollZone(location);
//
//        if (currentZone != null) {
//            if (insideZone != currentZone) {
//                Log.d("TollDetection", "Entering " + currentZone.getName());
//            }
//
//            if (prevLocation != null) {
//                tollDistance += haversineDistance(prevLocation,
//                        location.getLatitude(), location.getLongitude());
//
//            }
//
//            totalCost += currentZone.getCost();
//            tollsPassed.add(currentZone);
//            tollStatus.setText("Inside Toll: " + currentZone.getName());
//        } else if (insideZone != null) {
//            Log.d("TollDetection", "Exiting " + insideZone.getName());
//        }
//
//        insideZone = currentZone;
//        prevLocation = location;
//    }
//
//    private TollZone getNearestTollZone(Location location) {
//        for (TollZone zone : tollZones) {
//            double distance = haversineDistance(location, zone.getLatitude(), zone.getLongitude());
//            if (distance <= zone.getRadius()) {
//                return zone;
//            }
//        }
//        return null;
//    }
//
//    private double haversineDistance(Location loc, double lat, double lon) {
//        double R = 6371000; // Earth radius in meters
//        double dLat = Math.toRadians(lat - loc.getLatitude());
//        double dLon = Math.toRadians(lon - loc.getLongitude());
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(Math.toRadians(loc.getLatitude())) * Math.cos(Math.toRadians(lat)) *
//                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//    }
//
//    private void showTripSummary() {
//        Toast.makeText(this, "Total Distance in Toll Zones: " + tollDistance + "m\nTotal Cost: ₹" + totalCost, Toast.LENGTH_LONG).show();
//    }
//}
