package com.example.custos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.libraries.places.api.Places;
//import com.google.android.libraries.places.api.model.TypeFilter;
//import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private SearchView searchView;
    private FragmentTransaction transaction;
    private FusedLocationProviderClient fusedLocationClient;
    private final int ok = 0;
    //Testcode below

    int dangerZoneRequestCode = 0;


    public void openFragment(Fragment fragment) {
/*
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(android.R.id.content, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
*/

        transaction = getSupportFragmentManager().beginTransaction();

        transaction.addToBackStack(null);
       transaction.replace(R.id.container, fragment);
        transaction.commit();


    }

    public void closeFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {

            getSupportFragmentManager().popBackStackImmediate();
        }

    }
    private DatabaseReference db;
    //tillhere

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        db = FirebaseDatabase.getInstance().getReference("Users").child("rlpham18").child("event").child("e1234");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Hoang testing for search bar
        searchView = findViewById(R.id.search_view_mapActivity);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if(location != null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location,1);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
        //end
        // bottomNavigation = findViewById(R.id.bottom_navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //Rahul TestCode below

        final Button dangerzonebutton= findViewById(R.id.mapsDsngerZoneButton);
        dangerzonebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this,DangerZoneActivity.class);
                int requestCode = intent.getIntExtra("dangervalue",2);
                //startActivityForResult(intent,requestCode);
                startActivityForResult(intent,requestCode);
                //startActivity(intent);
            }
        });



        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_maps);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_events:
                        dangerzonebutton.setVisibility(View.GONE);
                        searchView.setVisibility(View.GONE);
                        openFragment(MainEventListActivity.newInstance());
                        return true;
                    case R.id.navigation_notifications:
                        dangerzonebutton.setVisibility(View.GONE);
                        searchView.setVisibility(View.GONE);
                        openFragment(NotificationActivity.newInstance());
                        return true;
                    case R.id.navigation_friends:
                        dangerzonebutton.setVisibility(View.GONE);
                    searchView.setVisibility(View.GONE);
                      openFragment(ContactsActivity.newInstance());
                        return true;
                    case R.id.navigation_settings:
                        searchView.setVisibility(View.GONE);
                        dangerzonebutton.setVisibility(View.GONE);
                        openFragment(SettingsActivity.newInstance());
//                        Intent intent = new Intent(MapsActivity.this,SecondSplashActivity.class);
//                        startActivityForResult(intent,2);
                        return true;
                    case R.id.navigation_maps:
                        dangerzonebutton.setVisibility(View.VISIBLE);
                        searchView.setVisibility(View.VISIBLE);
                        FragmentManager fm = MapsActivity.this.getSupportFragmentManager();

                        while(fm.getBackStackEntryCount() >= 1) {
                            fm.popBackStackImmediate();
                        }

                        return true;


                }
                return true;
            }
        });
        //rahul end


        //rahul new


        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String eventaddress=dataSnapshot.child("address").getValue().toString();
                System.out.println(eventaddress);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //rahul new end



    }

private LatLng eventlocation;
    public void setEventsLocation(LatLng ll,String mess){
        eventlocation=ll;
        if(eventlocation!=null) {
            mMap.addMarker(new MarkerOptions().position(eventlocation).title(mess));
            moveToCurrentLocation(eventlocation);
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               double  eventlonitude=Double.parseDouble(dataSnapshot.child("address").child("longitude").getValue().toString());
                double eventlatitude=Double.parseDouble(dataSnapshot.child("address").child("latitude").getValue().toString());
                String mess=dataSnapshot.child("message").getValue().toString()+", on "+dataSnapshot.child("date").getValue().toString();
                LatLng eventloc=new LatLng(eventlatitude,eventlonitude);
                setEventsLocation(eventloc,mess);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (checkPermissions()) {
            googleMap.setMyLocationEnabled(true);

            mMap = googleMap;
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                            //  mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                            moveToCurrentLocation(sydney);
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("MapDemoActivity", "Error trying to get last GPS location");
                    e.printStackTrace();
                }
            });
        }

        // Add a marker in Sydney and move the camera

    }

    private void moveToCurrentLocation(LatLng currentLocation) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // Here, thisActivity is the current activity
            requestPermissions();
            return false;
        }


    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0
                );

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ok: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Intent openApp = getPackageManager().getLaunchIntentForPackage("com.example.custos");

                    startActivity(openApp);
                    System.out.println("GRANTED");
                } else {
                    System.out.println("Not GRANTED");
                    System.exit(0);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * THIS IS HOW WE INTERACT WITH THE MAP
     * LINK: https://www.javatpoint.com/android-startactivityforresult-example?fbclid=IwAR0jMGAHYTVHMJVogObT-lkYwkyRWjMbTlVOLjJYL1B1i-TSfgFRxuyy7S4
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2

        if(requestCode==2)
        {
            //TODO: Add marker at current location
            Location dangerzonelocation = new Location("High Danger");
            dangerzonelocation.setLatitude(40.7934);
            dangerzonelocation.setLongitude(77.86);

            LatLng stateCollege = new LatLng(40.7934,-77.86);
            MarkerOptions dangerMarker = new MarkerOptions().position(stateCollege).title("Danger Zone Marker").icon(BitmapDescriptorFactory.fromResource(R.drawable.redtriangle));
            mMap.addMarker(dangerMarker);
            moveToCurrentLocation(stateCollege);

            //TODO: Modify the marker imagery and implement current location
            // https://stackoverflow.com/questions/17549372/how-to-get-click-event-of-the-marker-text
            System.out.println("This MAP ish workin");
        }

        if (requestCode == 3){
            Location dangerzonelocation = new Location("Medium Danger");
            dangerzonelocation.setLatitude(40.7934);
            dangerzonelocation.setLongitude(77.86);

            LatLng desmoines = new LatLng(41.619,-93.598);
            MarkerOptions dangerMarker = new MarkerOptions().position(desmoines).title("Danger Zone Marker").icon(BitmapDescriptorFactory.fromResource(R.drawable.orangetriangle));
            mMap.addMarker(dangerMarker);
            moveToCurrentLocation(desmoines);

        }

        if (requestCode == 4){
            Location dangerzonelocation = new Location("Low Danger");
            dangerzonelocation.setLatitude(40.7934);
            dangerzonelocation.setLongitude(77.86);

            LatLng hershey = new LatLng(40.2859,-76.658);
            MarkerOptions dangerMarker = new MarkerOptions().position(hershey).title("Danger Zone Marker").icon(BitmapDescriptorFactory.fromResource(R.drawable.orangetriangle));
            mMap.addMarker(dangerMarker);
            moveToCurrentLocation(hershey);

        }


    }


}
