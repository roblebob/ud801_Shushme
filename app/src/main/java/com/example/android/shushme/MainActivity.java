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

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.constraintlayout.utils.widget.ImageFilterButton;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.android.shushme.provider.PlaceContract;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
    private static int REQUEST_CODE = 1;

    // Member variables

    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private PlacesClient mPlacesClient;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SupportMapFragment mSupportMapFragment;

    private ImageFilterView mBar;
    private ImageFilterButton mLocationPermission;
    private ImageFilterButton mAddLocation;
    private ImageFilterButton mEnableGeofences;
    private Layer mBarLayer;




    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate( savedInstanceState);
        setContentView( R.layout.activity_main);


        mAdapter = new PlaceListAdapter(this, new ArrayList<Place>()); // TODO[✓] (3) Modify the Adapter to take a PlaceBuffer in the constructor
        mRecyclerView = (RecyclerView) findViewById( R.id.recylerview);
        mRecyclerView.setLayoutManager( new LinearLayoutManager(this));
        mRecyclerView.setAdapter( mAdapter);


        mSupportMapFragment = (SupportMapFragment)  getSupportFragmentManager() .findFragmentById( R.id.map_fragment);
        mSupportMapFragment .getMapAsync(this);


        
        Places.initialize( getApplicationContext(), getString( R.string.ApiKey));
        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);





        mBar = findViewById(R.id.bar);
        mLocationPermission = findViewById(R.id.location_permission);
        mAddLocation = findViewById(R.id.add_new_location);
        mEnableGeofences = findViewById(R.id.enable_geofences);
        mBarLayer = findViewById(R.id.bar_layer);






        refreshPlacesData();
    }




    // TODO[✓] (1) Implement a method called refreshPlacesData that:
    public void refreshPlacesData() {

        // - Queries all the locally stored Places IDs
        Uri uri = PlaceContract.PlaceEntry.CONTENT_URI;
        Cursor data = getContentResolver() .query( uri, null, null, null, null);
        if (data == null || data.getCount() == 0) return;
        List<String> placesIdList = new ArrayList<>();
        while (data.moveToNext()) {  placesIdList.add( data.getString( data.getColumnIndex( PlaceContract.PlaceEntry.COLUMN_PLACE_ID)));  }


        // - Sends for each entry the request to gain the detailed Place information
        for (String id : placesIdList) {

            List<Place.Field> placeFields = Arrays.asList(  Place.Field.ID,  Place.Field.NAME,  Place.Field.ADDRESS);
            FetchPlaceRequest request = FetchPlaceRequest .builder(id, placeFields) .build();

            //TODO[✓] (8) Set the getPlaceById callBack so that onResult calls the Adapter's swapPlaces with the result
            mPlacesClient .fetchPlace( request)
                    .addOnSuccessListener( (response) -> {
                        Place place = response .getPlace();
                        int pos = placesIdList .indexOf(  place.getId());
                        mAdapter .add(  pos, place);
                        Log.i(TAG, "[" + id + "]:  " + "Place found: " + place.getName() + "    " + place.getAddress() + "    " + place.getId()  +  "   ,  entered at position " + pos);
                    })
                    .addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            final ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + exception.getMessage());
                            final int statusCode = apiException .getStatusCode();
                            // TODO: Handle error with given status code.
                        }
                    });
        }
    }



    public void onAddPlaceButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            return;
        }

        Log.e(TAG, "passed   onAddPlaceButtonClicked!");


        CardView cardView = (CardView) findViewById(R.id.autocomplete);



        if ( !mAddLocation.isSelected()) {

            cardView.setVisibility( View.VISIBLE);
            mAddLocation .setSelected( true);
            mAddLocation .invalidate();
            mAddLocation .requestLayout();

            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NotNull Place place) {

                    Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                    // Insert a new place into DB
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, place.getId());
                    getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues);

                    refreshPlacesData();
                    mAddLocation .setSelected( false);
                    mAddLocation .invalidate();
                    mAddLocation .requestLayout();

                    cardView .setVisibility(View.GONE);

                }

                @Override
                public void onError(@NotNull Status status) {
                    Log.i(TAG, "An error occurred: " + status);
                }
            });

        } else if ( mAddLocation. isSelected()) {
            cardView.setVisibility(View.GONE);
            mAddLocation .setSelected(false);
            mAddLocation .invalidate();
            mAddLocation .requestLayout();
        }

    }



    @Override
    public void onResume() {

        // Initialize location permissions checkbox
        ImageView locationPermissions = (ImageView) findViewById( R.id.location_permission);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissions.setSelected(false);
        } else {
            locationPermissions.setSelected(true);
        }

        super.onResume();
    }

    public void onLocationPermissionClicked(View view) {
        ActivityCompat.requestPermissions(MainActivity.this,  new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},  PERMISSIONS_REQUEST_FINE_LOCATION);
    }






//TODO[✓] (2) call refreshPlacesData in GoogleApiClient's onConnected and in the Add New Place button click event


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
