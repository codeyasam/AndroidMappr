package org.mappr.org.mappr.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.codeyasam.mappr.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mappr.EstablishmentDetails;
import org.mappr.MapActivity;

import java.util.List;

/**
 * Created by codeyasam on 7/26/16.
 */
public class SearchesAdapter extends ArrayAdapter<MapprJSONSearch> {

    private Context context;
    private List<MapprJSONSearch> mapprJSONSearchList;

    public SearchesAdapter(Context context, List<MapprJSONSearch> mapprJSONSearchList) {
        super(context, android.R.layout.simple_list_item_1, mapprJSONSearchList);
        this.context = context;
        this.mapprJSONSearchList = mapprJSONSearchList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.search_list_item, null);

        try {
            MapprJSONSearch mapprJSONSearch = mapprJSONSearchList.get(position);
            if (mapprJSONSearch.hasBranch) {
                CYM_Utility.displayText(view, R.id.searcbEstabName, "Searched: " + mapprJSONSearch.getMapprBranch().getMapprEstablishment().getName());
                CYM_Utility.displayText(view, R.id.branchAddress, "Address: " + mapprJSONSearch.getMapprBranch().getAddress());
            } else {
                Log.i("poop", "dumaaan ng searches adpater");
                CYM_Utility.displayText(view, R.id.searcbEstabName, "Searched: " + mapprJSONSearch.getDisplayValue());
                view.findViewById(R.id.branchAddress).setVisibility(View.GONE);
            }

            String searchBy = mapprJSONSearch.getMappr_opt();
            if (searchBy.equals(CYM_Utility.OPT_BY_CATEGORY)) {
                searchBy = "by Category";
            } else if (searchBy.equals(CYM_Utility.OPT_BY_STRING)) {
                searchBy = "by Establishment";
            } else if (searchBy.equals(CYM_Utility.OPT_BY_QRCODE)) {
                searchBy = "by QR Code Scanner";
            } else {
                searchBy = "SELECTS";
            }
            CYM_Utility.displayText(view, R.id.searchBy, searchBy);
            view.setOnClickListener(getAdapterOnClickListener(mapprJSONSearch));
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("poop", "nag error sa view ng searches adpater");
        }

        return view;
    }

    private View.OnClickListener getAdapterOnClickListener(final MapprJSONSearch mapprJSONSearch) {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String mappr_opt = mapprJSONSearch.getMappr_opt();
                Intent intent = new Intent(context.getApplicationContext(), MapActivity.class);
                intent.putExtra(CYM_Utility.MAPPR_OPT, mappr_opt);
                if (mappr_opt.equals(CYM_Utility.OPT_BY_CATEGORY)) {
                    intent.putExtra("categoryID", mapprJSONSearch.getSearch_value());
                } else if (mappr_opt.equals(CYM_Utility.OPT_BY_STRING)) {
                    intent.putExtra("searchString", mapprJSONSearch.getSearch_value());
                } else if (mappr_opt.equals(CYM_Utility.OPT_BY_QRCODE)) {
                    MapActivity.implementedQrSearch = true; //to prevent from implementing search history
                    intent.putExtra("branchID", mapprJSONSearch.getSearch_value());
                } else {
                    Intent estabDetailsIntent = new Intent(context.getApplicationContext(), EstablishmentDetails.class);
                    intent.putExtra("branch_id", mapprJSONSearch.getMapprBranch().getId());
                    context.startActivity(estabDetailsIntent);
                    return;
                }
                context.startActivity(intent);
            }
        };
    }
}
