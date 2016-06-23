package com.example.codeyasam.mappr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by codeyasam on 6/23/16.
 */
public class ReviewAdapter extends ArrayAdapter<ReviewHolder> {

    Context context;
    List<ReviewHolder> reviewHolderList;

    public ReviewAdapter(Context context, List<ReviewHolder> reviewHolderList) {
        super(context, android.R.layout.simple_list_item_1, reviewHolderList);
        this.context = context;
        this.reviewHolderList = reviewHolderList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.review_list_item, null);

        try {
            ReviewHolder rhObj = reviewHolderList.get(position);
            MapprEndUser userObj = rhObj.getEndUser();
            CYM_Utility.setImageOnView(view, R.id.userDisplayPic, userObj.getDisplay_picture());
            CYM_Utility.displayText(view, R.id.fullName, userObj.getFullName());
            CYM_Utility.displayText(view, R.id.submitDate, rhObj.getSubmitDate());
            CYM_Utility.setRatingBarRate(view, R.id.branchRating, rhObj.getRating());
            CYM_Utility.displayText(view, R.id.userComment, rhObj.getComment());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
}
