package com.custom.co2.utils;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;


import com.custom.co2.R;
import com.tmall.ultraviewpager.UltraViewPager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;

public class UltraPagerAdapter extends PagerAdapter {
    private boolean isMultiScr;
    UltraViewPager mRecyclerView;
    RecyclerView recyclerView;
    OnselectVehicleType onselectVehicleType;

    private ArrayList<TaxiTypeModel> taxiTypeModelsList = new ArrayList<>();

    public UltraPagerAdapter(boolean isMultiScr, UltraViewPager mRecyclerView, OnselectVehicleType onselectVehicleType) {
        this.isMultiScr = isMultiScr;
        this.mRecyclerView = mRecyclerView;
        this.onselectVehicleType = onselectVehicleType;
    }



    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        @SuppressLint("InflateParams")
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.layout_child, null);
        recyclerView = linearLayout.findViewById(R.id.recyclerView);
        //TextView textView = linearLayout.findViewById(R.id.pager_textview);

        JSONObject ob = new JSONObject();
        try {
            ob.put("name", "Sedan");
            ob.put("img", R.mipmap.new_car_small);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (position) {
            case 0:
                // textView.setText("eeee");
                /*CarAdapter adapter = new CarAdapter(container.getContext(), new CarAdapter.interaction() {
                    @Override
                    public void onCarSelect(JSONObject ob, int position, ImageView img) {
                        CustomCarSelect.getInstance().changeState(ob, position, img);
                    }
                });
                adapter.setParams(recyclerView);
                for (int i = 0; i < 2; i++) {
                    if (i == 0) {
                        try {
                            ob.put("select", 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.setData(ob.toString());
                }
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext(),
                        LinearLayoutManager.HORIZONTAL, false));
                recyclerView.setAdapter(adapter);*/
                getTaxi(container);
                break;
            /*case 1:
                textView.setText("Personal");
                CarAdapter adapters = new CarAdapter(container.getContext(), new CarAdapter.interaction() {
                    @Override
                    public void onCarSelect(JSONObject ob, int position, ImageView img) {
                        CustomCarSelect.getInstance().changeState(ob, position, img);
                    }
                });
                adapters.setParams(recyclerView);
                for (int i = 0; i < 2; i++) {
                    adapters.setData(ob.toString());
                }
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext(),
                        HORIZONTAL, false));
                recyclerView.setAdapter(adapters);
                break;
            case 2:
                textView.setText("Consumer");
                CarAdapter adapter3 = new CarAdapter(container.getContext(), new CarAdapter.interaction() {
                    @Override
                    public void onCarSelect(JSONObject ob, int position, ImageView img) {
                        CustomCarSelect.getInstance().changeState(ob, position, img);
                    }
                });
                adapter3.setParams(recyclerView);
                for (int i = 0; i < 3; i++) {
                    adapter3.setData(ob.toString());
                }
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext(),
                        HORIZONTAL, false));
                recyclerView.setAdapter(adapter3);
                break;
            case 3:
                textView.setText("Daily");
                CarAdapter adapter4 = new CarAdapter(container.getContext(), new CarAdapter.interaction() {
                    @Override
                    public void onCarSelect(JSONObject ob, int position, ImageView img) {
                        CustomCarSelect.getInstance().changeState(ob, position, img);
                    }
                });
                adapter4.setParams(recyclerView);
                for (int i = 0; i < 2; i++) {
                    adapter4.setData(ob.toString());
                }
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext(),
                        HORIZONTAL, false));
                recyclerView.setAdapter(adapter4);
                break;*/
        }
        container.addView(linearLayout);
        return linearLayout;
    }

    private void getTaxi(ViewGroup container) {
        taxiTypeModelsList.clear();
        TaxiTypeModel taxiTypeModel = new TaxiTypeModel();
       // taxiTypeModel.setImg_taxi(R.drawable.img_auto);
        taxiTypeModel.setSetSecelt(false);
        taxiTypeModel.setTaxi_name("AUTO");
        taxiTypeModelsList.add(taxiTypeModel);

        taxiTypeModel = new TaxiTypeModel();
      //  taxiTypeModel.setImg_taxi(R.drawable.img_bike);
        taxiTypeModel.setSetSecelt(false);
        taxiTypeModel.setTaxi_name("BIKE");
        taxiTypeModelsList.add(taxiTypeModel);

        taxiTypeModel = new TaxiTypeModel();
      //  taxiTypeModel.setImg_taxi(R.drawable.img_micro);
        taxiTypeModel.setSetSecelt(false);
        taxiTypeModel.setTaxi_name("ECO");
        taxiTypeModelsList.add(taxiTypeModel);

        taxiTypeModel = new TaxiTypeModel();
       // taxiTypeModel.setImg_taxi(R.drawable.img_taxi);
        taxiTypeModel.setSetSecelt(false);
        taxiTypeModel.setTaxi_name("LUX");
        taxiTypeModelsList.add(taxiTypeModel);
        setAdapter(container);
    }

    @SuppressLint("WrongConstant")
    private void setAdapter(ViewGroup container) {
//        TaxiTypeAdapter bookMemberAdapter = new TaxiTypeAdapter(container.getContext(), taxiTypeModelsList,onselectVehicleType);
//        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 4, VERTICAL,  false));
//        recyclerView.setAdapter(bookMemberAdapter);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        LinearLayout view = (LinearLayout) object;
        container.removeView(view);
    }
}
