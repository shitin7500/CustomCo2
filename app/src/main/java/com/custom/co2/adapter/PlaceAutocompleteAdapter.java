package com.custom.co2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.custom.co2.R;
import com.custom.co2.utils.CustomProgress;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;

/**
 * Adapter that handles Autocomplete requests from the Places Geo Data API.
 * {@link AutocompletePrediction} results from the API are frozen and stored directly in this
 * adapter. (See {@link AutocompletePrediction#freeze()}.)
 * <p>
 * Note that this adapter requires a valid {@link GoogleApiClient}.
 * The API client must be maintained in the encapsulating Activity, including all lifecycle and
 * connection states. The API client must be connected with the {@link Places#GEO_DATA_API} API.
 */
public class PlaceAutocompleteAdapter extends RecyclerView.Adapter<PlaceAutocompleteAdapter.PlaceViewHolder> implements Filterable {
    private ImageView iv_line;

    public void setProgress(ImageView iv_line) {
        this.iv_line = iv_line;
    }


    public interface PlaceAutoCompleteInterface {
        public void onPlaceClick(ArrayList<PlaceAutocomplete> mResultList, int position);
    }

    Context mContext;
    PlaceAutoCompleteInterface mListener;
    private static final String TAG = "PlaceAutocompleteAdapter";
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private ArrayList<PlaceAutocomplete> mResultList = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds mBounds;
    private AutocompleteFilter mPlaceFilter;
    ArrayList<PlaceAutocomplete> resultList = new ArrayList<>();

    public PlaceAutocompleteAdapter(Context context, GoogleApiClient googleApiClient, LatLngBounds bounds, AutocompleteFilter filter) {
        this.mContext = context;
        mGoogleApiClient = googleApiClient;
        mBounds = bounds;
        mPlaceFilter = filter;
        this.mListener = (PlaceAutoCompleteInterface) mContext;
    }

    /*
    Clear List items
     */
    public void clearList() {
        if (mResultList != null && mResultList.size() > 0) {
            mResultList.clear();
            mResultList = null;
        }
    }

    /**
     * Sets the bounds for all subsequent queries.
     */
    public void setBounds(LatLngBounds bounds) {
        mBounds = bounds;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    CustomProgress.getInstance().changeState(true);
                    // Query the autocomplete API for the (constraint) search string.

                    mResultList = getAutocomplete(constraint);
                    if (mResultList != null) {
                        // The API successfully returned results.
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.  mResultList = (ArrayList<AutocompletePrediction>) results.values;
                    mResultList = (ArrayList<PlaceAutocomplete>) results.values;
                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    if (mResultList != null) {
                        mResultList.clear();
                        mResultList = null;
                    }
                    notifyItemRangeChanged(0, 0);
                }
                CustomProgress.getInstance().changeState(false);
            }
        };
    }

    private ArrayList<PlaceAutocomplete> getAutocomplete(CharSequence constraint) {

        if (mGoogleApiClient.isConnected()) {
            Log.i("", "Starting autocomplete query for: " + constraint);

            /*PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                    mBounds, mPlaceFilter);

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                //oast.makeText(mContext, "Error contacting API: " + status.toString(),
                //       Toast.LENGTH_SHORT).show();
                Log.e("", "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }

            Log.i("", "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList<PlaceAutocomplete> resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getFullText(STYLE_BOLD), prediction.getSecondaryText(STYLE_BOLD)));
            }
            autocompletePredictions.release();
            return resultList;*/

            // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
            // and once again when the user makes a selection (for example when calling fetchPlace()).
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

            // Use the builder to create a FindAutocompletePredictionsRequest.
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    // Call either setLocationBias() OR setLocationRestriction().
//                    .setLocationBias(bounds)
                    //.setLocationRestriction(bounds)
//                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token)
                    .setQuery(String.valueOf(constraint))
                    .build();


            // Initialize the SDK
            com.google.android.libraries.places.api.Places.initialize(mContext, mContext.getResources().getString(R.string.Map_Api_Key));

            // Create a new Places client instance
            PlacesClient placesClient = com.google.android.libraries.places.api.Places.createClient(mContext);

            placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                Log.d("ResultArray", String.valueOf(response.getAutocompletePredictions()));

                mResultList = new ArrayList<>();

                for (com.google.android.libraries.places.api.model.AutocompletePrediction prediction : response.getAutocompletePredictions()) {
//                    AutocompletePrediction prediction = (AutocompletePrediction) predictionlist;
                    // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
                    mResultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                            prediction.getFullText(STYLE_BOLD), prediction.getSecondaryText(STYLE_BOLD)));
                    Log.i("+++++", prediction.getPlaceId());
                    Log.i("+++++", prediction.getPrimaryText(null).toString());
                }
                Log.i("+++++SizeInner", String.valueOf(resultList.size()));

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e("+++++", "Place not found: " + apiException.getStatusCode());
                }
            });
            Log.i("+++++Size", String.valueOf(resultList.size()));

        }
        Log.e("", "Google API client is not connected for autocomplete query.");

        return mResultList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //Toast.makeText(mContext, "" + viewType, Toast.LENGTH_SHORT).show();
        View v0;
        /*  if (viewType == 0) {
            v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_home_place, viewGroup, false);
        } else {*/
        v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.expandlistitem, viewGroup, false);
        //}
        return new PlaceViewHolder(v0);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder mPredictionHolder, @SuppressLint("RecyclerView") final int i) {
        switch (getItemViewType(i)) {
            case 0:
                mPredictionHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // mListener.onPlaceClick(null, i);

                    }
                });
                break;
            case 1:
                if (mResultList != null && mResultList.size() > 0) {
                    if (mPredictionHolder.mParentLayout != null) {
                        mPredictionHolder.mParentLayout.setVisibility(View.VISIBLE);
                        mPredictionHolder.mParentLayout.setText(mResultList.get(i).description);
                        mPredictionHolder.mAddress.setText(mResultList.get(i).second);
                    } else {
                        mPredictionHolder.mParentLayout.setVisibility(View.GONE);
                    }
                } else {
                    mPredictionHolder.mParentLayout.setVisibility(View.GONE);
                }
                mPredictionHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onPlaceClick(mResultList, i);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mResultList != null && position < mResultList.size()) {
            if (mResultList != null && mResultList.get(position).placeId != null)
                return 1;
            else
                return 0;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        if (mResultList != null)
            return mResultList.size();
        else
            return 1;
    }

    public PlaceAutocomplete getItem(int position) {
        return mResultList.get(position);
    }

    /*
    View Holder For Trip History
     */
    class PlaceViewHolder extends RecyclerView.ViewHolder {
        //        CardView mCardView;
        TextView mParentLayout;
        TextView mAddress;
        LinearLayout layout_home;

        PlaceViewHolder(View itemView) {
            super(itemView);


            mParentLayout = itemView.findViewById(R.id.text1);
            mAddress = itemView.findViewById(R.id.text2);
            // layout_home = itemView.findViewById(android.R.id.layout_home);
        }

    }

    /**
     * Holder for Places Geo Data Autocomplete API results.
     */
    public class PlaceAutocomplete {

        public CharSequence placeId;
        public CharSequence description = "";
        public CharSequence second = "";

        PlaceAutocomplete(CharSequence placeId, CharSequence description, CharSequence secondaryText) {
            this.placeId = placeId;
            this.description = description;
            this.second = secondaryText;
        }

        @Override
        public String toString() {
            return description.toString();
        }
    }
}