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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.custom.co2.R;
import com.custom.co2.utils.CustomProgress;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;


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


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    CustomProgress.getInstance().changeState(true);
                    mResultList = getAutocomplete(constraint);
                    if (mResultList != null) {
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mResultList = (ArrayList<PlaceAutocomplete>) results.values;
                    notifyDataSetChanged();
                } else {
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
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(String.valueOf(constraint))
                    .build();


            com.google.android.libraries.places.api.Places.initialize(mContext, mContext.getResources().getString(R.string.Map_Api_Key));

            PlacesClient placesClient = com.google.android.libraries.places.api.Places.createClient(mContext);

            placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                Log.d("ResultArray", String.valueOf(response.getAutocompletePredictions()));

                mResultList = new ArrayList<>();

                for (com.google.android.libraries.places.api.model.AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                    mResultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                            prediction.getFullText(STYLE_BOLD), prediction.getSecondaryText(STYLE_BOLD)));
                }
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                }
            });
        }
        return mResultList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v0;
        v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.expandlistitem, viewGroup, false);
        return new PlaceViewHolder(v0);
    }


    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder mPredictionHolder, @SuppressLint("RecyclerView") final int i) {
        switch (getItemViewType(i)) {
            case 0:
                mPredictionHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

    class PlaceViewHolder extends RecyclerView.ViewHolder {
        //        CardView mCardView;
        TextView mParentLayout;
        TextView mAddress;

        PlaceViewHolder(View itemView) {
            super(itemView);
            mParentLayout = itemView.findViewById(R.id.text1);
            mAddress = itemView.findViewById(R.id.text2);
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