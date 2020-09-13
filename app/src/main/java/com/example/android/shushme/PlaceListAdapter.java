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

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import java.util.List;


public class PlaceListAdapter extends RecyclerView.Adapter< PlaceListAdapter.PlaceViewHolder> {

    private Context mContext;
    private List<Place> mPlaces;

    /**
     * Constructor using the context and the db cursor
     *
     * @param context the calling context/activity
     */
    public PlaceListAdapter(Context context, List<Place> placeBuffer) {
        // TODO [✓] (4) Take a PlaceBuffer as an input and store it as a local private member mPlaces
        this.mContext = context;
        this.mPlaces = placeBuffer;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item
     *
     * @param parent   The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new PlaceViewHolder that holds a View with the item_place_card layout
     */
    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate( R.layout.item_place_card, parent, false);
        return new PlaceViewHolder( view);
    }

    /**
     * Binds the data from a particular position in the cursor to the corresponding view holder
     *
     * @param holder   The PlaceViewHolder instance corresponding to the required position
     * @param position The current position that needs to be loaded with data
     */
    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {
        // TODO [✓] (6) Implement onBindViewHolder to set the view holder's Name and Address text fields from the Place object at the specified position in mPlaces
        holder.nameTextView.setText( mPlaces.get(position).getName());
        holder.addressTextView.setText( mPlaces.get(position).getAddress());
    }

    //TODO [✓] (7) Implement a public method swapPlaces that replaces the current mPlaces PlaceBuffer with a new one
    public void swapPlaces(List<Place> placeBuffer) {
        mPlaces = placeBuffer;
        if (mPlaces != null) { notifyDataSetChanged(); }
    }

    /**
     * Returns the number of items in the cursor
     *
     * @return Number of items in the cursor, or 0 if null
     */
    @Override
    public int getItemCount() {
        // TODO [✓] (5) Update getItemCount to return mPlaces's item count
        if (mPlaces == null) return 0;
        return mPlaces.size();
    }

    /**
     * PlaceViewHolder class for the recycler view item
     */
    class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView addressTextView;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById( R.id.name_text_view);
            addressTextView = (TextView) itemView.findViewById( R.id.address_text_view);
        }

    }
}
