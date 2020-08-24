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

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import static com.google.android.gms.location.places.Places.GEO_DATA_API;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    // Constants
    public static final String TAG = MainActivity.class.getSimpleName();

    // Member variables
    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private GoogleApiClient mGoogleApiClient;


    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.places_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PlaceListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // TODO [✓] (4) Create a GoogleApiClient with the LocationServices API and GEO_DATA_API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

    }

    // TODO (5) Override onConnected, onConnectionSuspended and onConnectionFailed for GoogleApiClient

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { Log.i(TAG, "API Client connecting failed"); }

    @Override
    public void onConnected(@Nullable Bundle bundle) { Log.i(TAG, "API Client connecting successfully"); }

    @Override
    public void onConnectionSuspended(int i) { Log.i(TAG, "API Client connecting was suspended"); }



    // TODO (7) Override onResume and inside it initialize the location permissions ())
    @Override
    protected void onResume() {
        super.onResume();
        

    }


    // TODO (8) Implement onLocationPermissionClicked to handle the CheckBox click event



    // TODO (9) Implement the Add Place Button click event to show  a toast message with the permission status
    public void onAddPlaceButtonClicked(View view) {

    }
}
