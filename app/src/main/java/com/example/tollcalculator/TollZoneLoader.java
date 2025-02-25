package com.example.tollcalculator;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TollZoneLoader {
    public static List<TollZone> loadTollZones(Context context) {
        List<TollZone> tollZones = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open("toll_zones.json");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("name");
                double lat = obj.getDouble("latitude");
                double lon = obj.getDouble("longitude");
                int radius = obj.getInt("radius");
                double cost= obj.getDouble("cost");
                tollZones.add(new TollZone(name, lat, lon, radius));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tollZones;
    }
}
