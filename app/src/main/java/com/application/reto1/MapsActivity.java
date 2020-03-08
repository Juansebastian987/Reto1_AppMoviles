package com.application.reto1;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker;
    double lat = 0.0;
    double lng = 0.0;
    SupportMapFragment mapFragment;
    private HashMap<String, LatLng> positions;
    int i=0;
    private TextView nearest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        positions = new HashMap<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nearest = findViewById(R.id.txt_Nearest);
    }

    public String nearestMarker(){
        float n = 0,mayor = 0,menor = 99999999;
        String msg="Usted esta en XXX";

        if(positions.size()>1){
            for (int i = 0; i < positions.size(); i++) {

                if(positions.get(i+"")!=null){
                    Location location1 = new Location("PO");
                    location1.setLongitude(positions.get("position").longitude);
                    location1.setLatitude(positions.get("position").latitude);

                    Location location2 = new Location("PE");
                    location2.setLongitude(positions.get(i+"").longitude);
                    location2.setLatitude(positions.get(i+"").latitude);

                    if(location1.distanceTo(location2) > mayor){
                           mayor = location1.distanceTo(location2);
                    }
                    if(location1.distanceTo(location2)  < menor){
                           menor = location1.distanceTo(location2);
                           msg = "Usted esta cerca de: "+address(location1.getLatitude(), location2.getLongitude());
                    }
                }
                else {
                    Log.i("error", "::::No existe el elemento::::");
                }

            }
        }
        return msg;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        myPosition();

        if(mMap!=null){
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View row = getLayoutInflater().inflate(R.layout.custom_address,null);
                    TextView txt_address = row.findViewById(R.id.txt_address);

                    txt_address.setText(marker.getTitle());

                    return row;
                }
            });
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                Location location1 = new Location("A");
                location1.setLatitude(latLng.latitude);
                location1.setLongitude(latLng.longitude);

                Location location2 = new Location("B");
                location2.setLongitude(positions.get("position").longitude);
                location2.setLatitude(positions.get("position").latitude);

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(address(location1.getLatitude(), location2.getLongitude())+", Y usted se encuentra a "+location1.distanceTo(location2)+"m del lugar")
                        .icon(generateBitmapDescriptorFromRes(mapFragment.getContext(), R.mipmap.market)));

                LatLng laa = new LatLng(latLng.latitude, latLng.longitude);
                positions.put(i+"", laa);
                i++;
                nearest.setText(nearestMarker());
            }
        });
    }

    public String address(double latitude,double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        String address=null;
        String[] allAddress = null;

        try {
            addresses = geocoder.getFromLocation(latitude,longitude, 1);
             address = addresses.get(0).getAddressLine(0);
             allAddress = address.split(",");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return allAddress[0];
    }

    public BitmapDescriptor generateBitmapDescriptorFromRes(
            Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void addMarker(double lat, double lng) {
        LatLng coordenadas = new LatLng(lat, lng);
        CameraUpdate myPosition = CameraUpdateFactory.newLatLngZoom(coordenadas, 16);
        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .position(coordenadas)
                .title("Usted se encuentra en "+address(lat,lng))
                .icon(generateBitmapDescriptorFromRes(mapFragment.getContext(), R.mipmap.position)));
        mMap.animateCamera(myPosition);

        positions.put("position", coordenadas);
    }

    private void updateLocation(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            addMarker(lat, lng);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void myPosition() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateLocation(location);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000,0,locationListener);
    }

}
