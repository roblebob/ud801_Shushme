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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import com.example.android.shushme.provider.PlaceContract;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Constants
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private static int REQUEST_CODE = 1;

    // Member variables
    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private GoogleApiClient mGoogleApiClient;
    private PlacesClient mPlacesClient;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private List<Place> mPlaceList;
    private List<String> mPlacesIdList;


    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState);
        setContentView( R.layout.activity_main);

        // Set up the recycler view
        mRecyclerView = (RecyclerView) findViewById( R.id.places_list_recycler_view);
        mRecyclerView.setLayoutManager( new LinearLayoutManager(this));

        // TODO[✓] (3) Modify the Adapter to take a PlaceBuffer in the constructor
        mAdapter = new PlaceListAdapter(this, null);
        mRecyclerView.setAdapter( mAdapter);

        mPlaceList = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        ((SupportMapFragment)   getSupportFragmentManager().findFragmentById( R.id.map_fragment))  .getMapAsync(this);;
        // Initialize the SDK
        Places.initialize( getApplicationContext(), getString( R.string.ApiKey));
        // Create a new PlacesClient instance
        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        refreshPlacesData();
    }


    // TODO[✓] (1) Implement a method called refreshPlacesData that:
    public void refreshPlacesData() {

        // - Queries all the locally stored Places IDs
        Uri uri = PlaceContract.PlaceEntry.CONTENT_URI;
        Cursor data = getContentResolver() .query( uri, null, null, null, null);
        if (data == null || data.getCount() == 0) return;
        mPlacesIdList = new ArrayList<>();
        while (data.moveToNext()) {  mPlacesIdList.add( data.getString( data.getColumnIndex( PlaceContract.PlaceEntry.COLUMN_PLACE_ID)));  }



        // - Calls Places.GeoDataApi.getPlaceById with that list of IDs
        // Note: When calling Places.GeoDataApi.getPlaceById use the same GoogleApiClient created in MainActivity's onCreate (you will have to declare it as a private member)

        // PendingResult<PlaceBuffer> placeResult =
        // Places.GeoDataApi.getPlaceById( mGoogleApiClient, guids.toArray( new String[guids.size()]));
        //TODO[✓] (8) Set the getPlaceById callBack so that onResult calls the Adapter's swapPlaces with the result
        //placeResult .setResultCallback( placeBuffer -> mAdapter.swapPlaces( placeBuffer));



        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);

        // Construct a request object, passing the place ID and fields array.
        //final FetchPlaceRequest request = FetchPlaceRequest.newInstance(guids, placeFields);


        for (String id : mPlacesIdList) {

            mPlacesClient
                    .fetchPlace(FetchPlaceRequest.newInstance(id, placeFields))
                    .addOnSuccessListener((response) -> {
                        Place place = response.getPlace();
                        Log.i(TAG, "Place found: " + place.getName() + "    " + place.getAddress() + "    " + place.getId());
                        int pos = mPlacesIdList.indexOf( place.getId());
                        mPlaceList.add( pos, place);
                    })
                    .addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            final ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + exception.getMessage());
                            final int statusCode = apiException.getStatusCode();
                            // TODO: Handle error with given status code.
                        }
                    });
        }
        mAdapter.swapPlaces(mPlaceList);
    }


    /***
     * Button Click event handler to handle clicking the "Add new location" Button
     *
     * @param view
     */
    public void onAddPlaceButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            return;
        }

        // Start a new Activity for the Place Autocomplete API, this will trigger {@code #onActivityResult} when a place is selected or with the user cancels.
        List<Place.Field> fields = Arrays.asList( Place.Field.ID, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder( AutocompleteActivityMode.OVERLAY, fields) .build(this);
        startActivityForResult( intent, REQUEST_CODE);
    }
    /***
     * Called when the Place Autocomplete Activity returns back with a selected place (or after canceling)
     *
     * @param requestCode The request code passed when calling startActivityForResult
     * @param resultCode  The result code specified by the second activity
     * @param data        The Intent that carries the result data.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK) {

                //Place place = PlacePicker.getPlace( this, data);
                Place place = Autocomplete.getPlaceFromIntent( data);
                Log.i(TAG,  "Place: " + place.getName() + ", " + place.getAddress() + ", " + place.getId());

                // Extract the place information from the API
                String placeName = place.getName();
                String placeAddress = place.getAddress();
                String placeID = place.getId();

                // Insert a new place into DB
                ContentValues contentValues = new ContentValues();
                contentValues.put( PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeID);
                getContentResolver().insert( PlaceContract.PlaceEntry.CONTENT_URI, contentValues);

                refreshPlacesData();

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) { Log.i(TAG, Objects.requireNonNull( Autocomplete.getStatusFromIntent(data) .getStatusMessage()));
            } else if (resultCode == RESULT_CANCELED) { /* The user canceled the operation. */}
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {

        // Initialize location permissions checkbox
        CheckBox locationPermissions = (CheckBox) findViewById( R.id.location_permission_checkbox);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissions.setChecked(false);
        } else {
            locationPermissions.setChecked(true);
            locationPermissions.setEnabled(false);
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
