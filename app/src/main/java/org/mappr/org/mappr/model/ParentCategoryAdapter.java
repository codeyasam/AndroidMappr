package org.mappr.org.mappr.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.codeyasam.mappr.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by codeyasam on 12/5/16.
 */
public class ParentCategoryAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<MapprCategory> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<MapprCategory>> _listDataChild;
    private FragmentActivity _activity;
    private AdapterView.OnItemClickListener customOnClickMethodCateg;
    private GridView categoryGrid;


    public ParentCategoryAdapter(Context context, List<MapprCategory> listDataHeader,
                           HashMap<String, List<MapprCategory>> listChildData, FragmentActivity _activity) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._activity = _activity;
        this.customOnClickMethodCateg = customOnClickMethodCateg;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition).getId())
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        try {
            LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_parent_categ_child, null);
            categoryGrid = (GridView) convertView.findViewById(R.id.categoryGrid);
            CategoryAdapter categoryAdapter = new CategoryAdapter(_context, _listDataChild.get(_listDataHeader.get(groupPosition).getId()));
            Log.i("poop", "id: " + _listDataHeader.get(groupPosition).getId());
            Log.i("poop", "size: " + _listDataChild.get(_listDataHeader.get(groupPosition).getId()).size());
            categoryGrid.setAdapter(categoryAdapter);
            //categoryGrid.setOnItemClickListener(customOnClickMethodCateg);
            categoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    List<MapprCategory> categoryList = _listDataChild.get(_listDataHeader.get(groupPosition).getId());
                    MapprCategory category = categoryList.get(position);
                    CategoryFragment.searchByCategory(_activity, category);
                }
            });

            int listChildSize =  _listDataChild.get(_listDataHeader.get(groupPosition).getId()).size();
            Log.i("poop", "divided: " + Math.ceil(listChildSize / 3.0));
            int multiplier = (int)Math.ceil(listChildSize / 3.0);
            ViewGroup.LayoutParams layoutParams = categoryGrid.getLayoutParams();
            layoutParams.height = (int)CYM_Utility.dipToPixels(_context, 120 * multiplier); //this is in pixels
            categoryGrid.setLayoutParams(layoutParams);
            Log.i("poop", "grid height: " + categoryGrid.getMeasuredHeight() + " multiplier: " + multiplier);

            return convertView;
        } catch (Exception e) {
            e.printStackTrace();
            return convertView;
        }

    }

    @Override
    public int getChildrenCount(int groupPosition) {
//        return this._listDataChild.get(this._listDataHeader.get(groupPosition).getId())
//                .size();
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final MapprCategory mapprCategory = (MapprCategory) getGroup(groupPosition);
        LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.expandable_parent_categ, null);
        CYM_Utility.setImageOnView(convertView, R.id.parentCategIcon, mapprCategory.getDisplay_picture());
        CYM_Utility.displayText(convertView, R.id.lblListHeaderParentCateg, mapprCategory.getName());
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
