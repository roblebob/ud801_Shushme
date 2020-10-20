package com.example.android.shushme;

/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.constraintlayout.utils.widget.ImageFilterButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.example.android.shushme.provider.PlaceContract;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Constants
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private static final int REQUEST_CODE = 1;
    private static final List<Place.Field> PLACE_FIELDS = Arrays.asList(  Place.Field.ID,  Place.Field.NAME,  Place.Field.ADDRESS, Place.Field.LAT_LNG);
    private static final int DEFAULT_ZOOM = 12;

    // Member variables

    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private PlacesClient mPlacesClient;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    private ConstraintLayout mConstraintLayout;
    private ImageFilterButton mGetLocation;
    private ImageFilterButton mAddLocation;
    private ImageFilterButton mEnableGeofences;
    private MyBar mBar;
    private Layer mBarLayer;

    private Context mContext;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate( savedInstanceState);
        setContentView( R.layout.activity_main);
        mContext = this;

        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mSupportMapFragment.getMapAsync(this);

        Places.initialize( getApplicationContext(), getString( R.string.ApiKey));
        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices .getFusedLocationProviderClient(this);


        mAdapter = new PlaceListAdapter(this, new ArrayList<Place>()); // TODO[✓] (3) Modify the Adapter to take a PlaceBuffer in the constructor
        mRecyclerView = (RecyclerView) findViewById( R.id.recylerview);
        mRecyclerView.setLayoutManager( new LinearLayoutManager(this));
        mRecyclerView.setAdapter( mAdapter);




        mAddLocation = (ImageFilterButton)findViewById(R.id.add_location);
        mEnableGeofences = (ImageFilterButton) findViewById(R.id.enable_geofences);
        mBar = (MyBar)  findViewById(R.id.bar);
        mBarLayer = (Layer) findViewById(R.id.bar_layer);
        mConstraintLayout = (ConstraintLayout) findViewById(R.id.constraint_layout);


        mGetLocation =  (ImageFilterButton) findViewById( R.id.get_location);
        mGetLocation.setOnClickListener(view -> {

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (mGetLocation.isActivated()) {

                    Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                    locationResult.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                            } else {
                                Toast.makeText(mContext, "Current Location cannot been shown!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {mGetLocation.setActivated(true);}
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_FINE_LOCATION);
            }
        });



        mAddLocation.setOnClickListener(view -> {

//            if ( ! view.isActivated())   {
//                Toast.makeText(mContext, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
//                return;
//            }

            Log.e(TAG, "passed   onAddPlaceButtonClicked!");

            if ( !mAddLocation.isActivated()) {

                AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
                autocompleteFragment.setPlaceFields( PLACE_FIELDS);
                autocompleteFragment.setOnPlaceSelectedListener(   new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(@NotNull Place place) {

                        mGoogleMap .moveCamera( CameraUpdateFactory.newLatLngZoom( place.getLatLng(), DEFAULT_ZOOM));

                        Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                        // Insert a new place into DB
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, place.getId());
                        getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues);

                        refreshData();

                        mAddLocation.setSelected( false);
                        mAddLocation.invalidate();
                        mAddLocation.requestLayout();

                        ((CardView) findViewById(R.id.autocomplete)) .setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(@NotNull Status status) {
                        Log.i(TAG, "An error occurred: " + status);
                    }
                });

                ((CardView) findViewById( R.id.autocomplete)) .setVisibility( View.VISIBLE);
                mAddLocation.setActivated( true);
                mAddLocation.invalidate();
                mAddLocation.requestLayout();

            } else  {

                ((CardView) findViewById( R.id.autocomplete)) .setVisibility( View.GONE);
                mAddLocation.setActivated( false);
                mAddLocation.invalidate();
                mAddLocation.requestLayout();
            }
        });

        mEnableGeofences.setOnClickListener(   (view) -> {
            // TODO
        });




        mBar.setOnTouchListener(   (v, event) -> {

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet .clone(mConstraintLayout);

            if      (event.getActionMasked() == MotionEvent.ACTION_DOWN) { v.setBackgroundColor(  getColor( R.color.colorAccent)); }
            else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) { constraintSet .setVerticalBias(  mBarLayer.getId(),   event.getRawY() / mConstraintLayout.getHeight()); }
            else if (event.getActionMasked() == MotionEvent.ACTION_UP)   { v.setBackgroundColor(  getColor( R.color.colorBlackTransparent));    v.performClick(); }

            constraintSet.applyTo(mConstraintLayout);
            return true;
        });




        refreshData();
    }




    // TODO[✓] (1) Implement a method called refreshPlacesData that:
    public void refreshData() {

        // - Queries all the locally stored Places IDs
        Uri uri = PlaceContract.PlaceEntry.CONTENT_URI;
        Cursor data = getContentResolver() .query( uri, null, null, null, null);
        if (data == null || data.getCount() == 0) return;
        List<String> placesIdList = new ArrayList<>();
        while (data.moveToNext()) {  placesIdList .add(    data.getString( data.getColumnIndex(   PlaceContract.PlaceEntry.COLUMN_PLACE_ID)));  }


        // - Sends for each entry the request to gain the detailed Place information
        for (String id : placesIdList) {

            //TODO[✓] (8) Set the getPlaceById callBack so that onResult calls the Adapter's swapPlaces with the result
            mPlacesClient
                    .fetchPlace(   FetchPlaceRequest .builder(id, PLACE_FIELDS) .build())
                    .addOnSuccessListener(   (response) -> {
                        Place place = response .getPlace();
                        int pos = placesIdList .indexOf(  place.getId());
                        mAdapter .add(  pos, place);
                        Log.i(TAG, "[" + id + "]:  " + "Place found: " + place.getName() + "    " + place.getAddress() + "    " + place.getId()  +  "     " + place.getLatLng()  + "  ,  entered at position " + pos);
                    })
                    .addOnFailureListener(   (exception) -> {
                        if (exception instanceof ApiException) {
                            final ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + exception.getMessage());
                            final int statusCode = apiException .getStatusCode();
                            // TODO: Handle error with given status code.
                        }
                    });
        }
    }







    @Override
    public void onResume() {

        // Initialize location permissions checkbox
//        ImageView locationPermissions = (ImageView) findViewById( R.id.location_permission);
//        if (ActivityCompat.checkSelfPermission(MainActivity.this,  android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            locationPermissions.setSelected(false);
//        } else {
//            locationPermissions.setSelected(true);
//        }

        super.onResume();
    }








    @Override
    public void onRequestPermissionsResult(  int requestCode,   @NonNull String[] permissions,   @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGetLocation.setActivated( true);
                    Toast.makeText(mContext, "Location permission has been granted ", Toast.LENGTH_LONG).show();
                } else {
                    mGetLocation.setActivated( false);
                    Toast.makeText(mContext, "Location permission has been denied ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }






//TODO[✓] (2) call refreshPlacesData in GoogleApiClient's onConnected and in the Add New Place button click event

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

        mGoogleMap = googleMap;
        LatLng latLng = new LatLng( 0 ,  27);
        mGoogleMap .moveCamera(  CameraUpdateFactory .newLatLng(  latLng));
        mGoogleMap .addMarker(  new MarkerOptions() .position(latLng) .title("???"));
        mGoogleMap .getUiSettings() .setZoomControlsEnabled(true);
    }
}
