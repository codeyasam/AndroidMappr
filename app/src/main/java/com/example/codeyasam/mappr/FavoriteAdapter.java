package com.example.codeyasam.mappr;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by codeyasam on 6/17/16.
 */
public class FavoriteAdapter extends ArrayAdapter<MapprBranch> {

    Context context;
    List<MapprBranch> mapprBranches;

    public FavoriteAdapter(Context context, List<MapprBranch> mapprBranches) {
        super(context, android.R.layout.simple_list_item_1, mapprBranches);
        this.context = context;
        this.mapprBranches = mapprBranches;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.branch_list_item, null);

        try {
            MapprBranch branch = mapprBranches.get(position);
            MapprEstablishment estab = branch.getMapprEstablishment();
            CYM_Utility.setImageOnView(view, R.id.estabLogo, estab.getDisplay_picture());
            CYM_Utility.displayText(view, R.id.estabName, estab.getName());
            CYM_Utility.displayText(view, R.id.branchAddress, branch.getAddress());
            view.setOnClickListener(getAdapterOnClickListener(branch.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }


        //CYM_Utility.setText(, R.id.estabName, "yeah");
        return view;
    }

    private View.OnClickListener getAdapterOnClickListener(final String branch_id) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapprDetails.class);
                intent.putExtra("branch_id", branch_id);
                intent.putExtra(CYM_Utility.MAPPR_FORM, CYM_Utility.FROM_FAVORITES);
                context.startActivity(intent);
            }
        };
    }

}
