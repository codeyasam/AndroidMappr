package org.mappr.org.mappr.model;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.codeyasam.mappr.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.mappr.DirectionActivity;

import java.util.List;

/**
 * Created by codeyasam on 7/24/16.
 */
public class RouteAdapter extends ArrayAdapter<MapprRoute> {

    private Context mContext;
    private List<MapprRoute> mapprRoutes;
    private GoogleMap mMap;
    private Polyline line;


    public RouteAdapter(Context mContext, List<MapprRoute> mapprRoutes, GoogleMap mMap, Polyline line) {
        super(mContext, android.R.layout.simple_list_item_2, mapprRoutes);
        this.mContext = mContext;
        this.mapprRoutes = mapprRoutes;
        this.mMap = mMap;
        this.line = line;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.route_list_item, null);

        try {
            MapprRoute mapprRoute = mapprRoutes.get(position);
            CYM_Utility.displayText(view, R.id.etaTxt, "ETA: " + mapprRoute.getEstimatedTimeArrival());
            CYM_Utility.displayText(view, R.id.viaTxt, "Via: " + mapprRoute.getViaSummary());
            CYM_Utility.displayText(view, R.id.distanceTxt, "Distance: " + mapprRoute.getDistance());
            view.setOnClickListener(getAdapterOnClickListener(mapprRoute.getEncodedString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private View.OnClickListener getAdapterOnClickListener(final String encodedString) {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                line.remove();
                List<LatLng> list = DirectionActivity.decodePoly(encodedString);
                line = mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(12)
                                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                .geodesic(true)
                );
            }
        };
    }
}
