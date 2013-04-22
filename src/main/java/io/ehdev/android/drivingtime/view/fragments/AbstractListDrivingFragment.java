package io.ehdev.android.drivingtime.view.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import dagger.ObjectGraph;
import io.ehdev.android.drivingtime.R;
import io.ehdev.android.drivingtime.adapter.EntryAdapter;
import io.ehdev.android.drivingtime.database.dao.DatabaseHelper;
import io.ehdev.android.drivingtime.module.ModuleGetters;
import io.ehdev.android.drivingtime.view.dialog.ShowDialog;

import javax.inject.Inject;
import java.sql.SQLException;

public abstract class AbstractListDrivingFragment<T> extends Fragment {

    private static final String TAG = AbstractListDrivingFragment.class.getName();

    private ActionMode actionMode;
    private EntryAdapter<T> adapter;

    @Inject
    protected DatabaseHelper databaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ObjectGraph objectGraph = ObjectGraph.create(ModuleGetters.getInstance());
        objectGraph.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(getViewId(), null);
        if(adapter == null)
            throw new AdapterNotSetException();
        setupListView(view);

        return view;
    }

    public EntryAdapter<T> getAdapter() {
        return adapter;
    }

    public void setAdapter(EntryAdapter<T> adapter) {
        this.adapter = adapter;
    }

    protected int getViewId(){
        return R.layout.detailed_list_view;
    }

    private void setupListView(View view) {
        ListView listView = (ListView) view.findViewById(R.id.listOfAllRecords);
        listView.setAdapter(adapter);
        listView.setSelector(R.drawable.custom_selector);
        addOnItemClickListener(listView);
    }

    private void addOnItemClickListener(ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(adapter.isIndexSelected(EntryAdapter.NO_VALUE_SELECTED)){
                    try{
                        actionMode = getActivity().startActionMode(new EditDeleteActionMode<T>(adapter, getShowDialog(), databaseHelper.getDao(adapter.getClassName()), getReloadAdapter()));
                        adapter.setSelected(position);
                    } catch (SQLException e) {
                        Toast.makeText(getActivity(), "Unable to select item", Toast.LENGTH_LONG);
                    }
                } else if (!adapter.isIndexSelected(position)) {
                    adapter.setSelected(position);
                } else if (actionMode != null){
                    actionMode.finish();
                    actionMode = null;
                }

            }
        });
    }

    abstract protected ShowDialog<T> getShowDialog();

    abstract protected PostEditExecution getReloadAdapter();

    public interface PostEditExecution{
        public void execute();
    }

    public static class AdapterNotSetException extends RuntimeException {
    }
}