package com.example.tollcalculator;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private List<TollZone> tollZones = new ArrayList<>();
    private static final int TOLL_RADIUS = 10000; // 10 km (adjust as needed)
    private static final int LOCATION_UPDATE_INTERVAL = 100; // 0.1 seconds per coordinate
    private static final double TOLL_RATE_PER_KM = 1.75; // Rs. 1.75 per km

    private TextView tollStatus, coordinatesText;
    private Button demoButton, tripSummaryButton;
    private List<Location> gpxLocations = new ArrayList<>();
    private Handler handler = new Handler();
    private int currentLocationIndex = 0;
    private double totalDistance = 0;
    private Set<TollZone> detectedTolls = new HashSet<>();

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
        // Add sample toll zones for testing (replace with your CSV logic if needed)
        tollZones.add(new TollZone("WarangalToll", 17.9689, 79.5941, TOLL_RADIUS));
        tollZones.add(new TollZone("HyderabadToll", 17.3850, 78.4867, TOLL_RADIUS));

        Log.d("TollData", "Loaded " + tollZones.size() + " toll zones.");
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
                    Log.d("GPXParsing", "Parsed coordinate: Lat=" + lat + ", Lon=" + lon);
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
        totalDistance = 0;
        detectedTolls.clear();
        handler.post(locationUpdateRunnable);
    }

    private final Runnable locationUpdateRunnable = new Runnable() {
        Location lastLocation = null;

        @Override
        public void run() {
            if (currentLocationIndex < gpxLocations.size()) {
                Location location = gpxLocations.get(currentLocationIndex);
                updateUIWithLocation(location);

                if (lastLocation != null) {
                    float[] results = new float[1];
                    Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                            location.getLatitude(), location.getLongitude(), results);
                    totalDistance += results[0];
                    Log.d("DistanceCalculation", "Distance between points: " + results[0] + " meters");
                }
                lastLocation = location;

                checkTollZone(location.getLatitude(), location.getLongitude());
                currentLocationIndex++;
                handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            } else {
                runOnUiThread(() -> {
                    tripSummaryButton.setEnabled(true);
                    Log.d("TripSummary", "Total Distance: " + (totalDistance / 1000) + " km");
                });
            }
        }
    };

    private void showTripSummary() {
        // Calculate total cost
        double totalCost = totalDistance / 1000 * TOLL_RATE_PER_KM;

        // Display trip summary
        new AlertDialog.Builder(this)
                .setTitle("Trip Summary")
                .setMessage("Total Distance: " + (totalDistance / 1000) + " km\n" +
                        "Tolls Passed: " + detectedTolls.size() + "\n" +
                        "Total Cost: Rs. " + String.format("%.2f", totalCost))
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
        boolean inTollZone = false;

        for (TollZone zone : tollZones) {
            float[] results = new float[1];
            Location.distanceBetween(lat, lon, zone.getLatitude(), zone.getLongitude(), results);

            Log.d("TollDetection", "Checking toll: " + zone.getName() +
                    ", Distance: " + results[0] + " meters");

            if (results[0] <= TOLL_RADIUS) {
                runOnUiThread(() -> tollStatus.setText("Inside Toll Zone: " + zone.getName()));
                detectedTolls.add(zone);
                inTollZone = true;
                Log.d("TollDetection", "Toll detected: " + zone.getName());
            }
        }

        if (!inTollZone) {
            runOnUiThread(() -> tollStatus.setText("No Toll Zone Nearby"));
        }
    }
}