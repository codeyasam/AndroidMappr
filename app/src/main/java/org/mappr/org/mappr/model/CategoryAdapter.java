package org.mappr.org.mappr.model;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.codeyasam.mappr.R;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by codeyasam on 7/17/16.
 */
public class CategoryAdapter extends ArrayAdapter {

    private Context context;
    private List<MapprCategory> categoryList;

    public CategoryAdapter(Context context, List<MapprCategory> categoryList) {
        super(context, android.R.layout.simple_list_item_1, categoryList);
        this.context = context;
        this.categoryList = categoryList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.category_grid_item, null);

        try {
            MapprCategory category = categoryList.get(position);
            CYM_Utility.setImageOnView(view, R.id.categoryDpImg, category.getDisplay_picture());
            CYM_Utility.displayText(view, R.id.categoryNameTxt, category.getName());
            TextView tv = (TextView) view.findViewById(R.id.categoryNameTxt);
            tv.setTextColor(Color.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
}
