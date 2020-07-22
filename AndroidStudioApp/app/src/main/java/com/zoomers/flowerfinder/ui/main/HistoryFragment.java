package com.zoomers.flowerfinder.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zoomers.flowerfinder.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.zoomers.flowerfinder.ui.main.PictureContent.loadImage;

/**
 * The fragment/page to display the interactive detection history
 */
public class HistoryFragment extends Fragment implements RecyclerAdapter.OnPicListener {

    private RecyclerAdapter.OnPicListener mListener;
    private RecyclerView.Adapter recyclerViewAdapter;

    /**
     * Mandatory constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistoryFragment() {
    }

    @SuppressWarnings("unused")
    public static HistoryFragment newInstance(int columnCount) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_item_list, container, false);

        try {
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String historyPath = absolutePath + "/flowerfinder/history.csv";
            List<String[]> images = parseCSV(historyPath);
            for (String[] image : images) {
                // get all four components from each line of CSV
                // check if filepath, result exist
                String filepath, result;
                if (image.length > 0) {
                    filepath = absolutePath + "/flowerfinder/pictures/" + image[0];
                    result = image[1];
                } else {
                    continue;
                }
                // check if lat, lng exist
                String lat, lng;
                if (image.length > 2) {
                    lat = image[2];
                    lng = image[3];
                } else {
                    lat = "0";
                    lng = "0";
                }

                File imgFile = new File(filepath);
                if (imgFile.exists()) {
                    loadImage(imgFile, result, lat, lng);
                } else {
                    Log.d("FlowerFinder", "file does not exist");
                }
            }
        } catch (FileNotFoundException e) {
            Log.d("FlowerFinder", "History file not found");
        }

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerViewAdapter = new RecyclerAdapter(PictureContent.ITEMS, this);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * update the history list when a new photo is taken
     */
    public void refreshListView() {
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * parses a given CSV file
     *
     * @param filename is the file to parse
     * @return the information as a list
     * @throws FileNotFoundException if the file is not found
     */
    public List<String[]> parseCSV(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filename));
        List<String[]> list = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            list.add(line.split(", "));
        }
        scanner.close();
        return list;
    }

    @Override
    public void onPicClick(int position) {
        PictureContent.PictureItem item = PictureContent.ITEMS.get(position);
        String result = item.result;
        double lat = 0;
        double lng = 0;
        if (!(item.lat.equals("") && item.lng.equals(""))){
            lat = Double.parseDouble(item.lat);
            lng = Double.parseDouble(item.lng);
        }
        Intent intent = new Intent(this.getContext(), MapActivity.class);
        intent.putExtra("flowerLat", lat);
        intent.putExtra("flowerLong", lng);
        intent.putExtra("flowerClass", result);
        startActivity(intent);
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(PictureContent.PictureItem item);
    }
}