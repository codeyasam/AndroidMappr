package org.mappr.org.mappr.model;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.codeyasam.mappr.R;

import java.util.List;

/**
 * Created by codeyasam on 7/26/16.
 */
public class SearchesFragment extends Fragment {

    public View view;
    public ListView listViewSearches;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main_searches, container, false);
        listViewSearches = (ListView) view.findViewById(R.id.listViewSearches);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        List<MapprJSONSearch> mapprJSONSearchList = MapprJSONSearch.getSearchesList(getActivity().getApplicationContext());
        ArrayAdapter<MapprJSONSearch> adapter = new SearchesAdapter(getActivity(), mapprJSONSearchList);
        listViewSearches.setAdapter(adapter);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String searchRequest = settings.getString(MapprJSONSearch.SEARCH_REQUEST, "");

        if (searchRequest.isEmpty()) {
            view.findViewById(R.id.emptySearchesTxt).setVisibility(View.VISIBLE);
            view.findViewById(R.id.listViewSearches).setVisibility(View.INVISIBLE);
        } else {
            view.findViewById(R.id.emptySearchesTxt).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.listViewSearches).setVisibility(View.VISIBLE);
        }
    }
}
