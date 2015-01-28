package com.moczul.notepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.moczul.notepad.models.Item;
import com.moczul.notepad.utils.Broadcast;
import com.moczul.notepad.utils.DBHelper;
import com.moczul.notepad.utils.Styling;

import java.util.ArrayList;

public class NotepadFragment extends Fragment implements
        AdapterView.OnItemClickListener {

    private static final String TAG = "NotepadFragment";

    private ListView mList;
    private ArrayAdapter<String> mListAdapter;
    private ArrayList<String> mListTitles;
    private ArrayList<Item> mListAdapterItems;
    private int mPosition = 0;
    private int mPadding;

    public NotepadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View localRoot = inflater.inflate(R.layout.fragment_notepad, container, false);

        mList = (ListView) localRoot.findViewById(R.id.noteList);

        Styling.updateWithFont(
                getActivity(),
                (TextView) localRoot.findViewById(R.id.addNote),
                Styling.Fonts.LATO_BOLD
        ).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .replace(
                                R.id.fragmentContainer,
                                NoteAddEditFragment.newInstance()
                        )
                        .addToBackStack(null)
                        .commit();
            }
        });

        setupNotesList();

        registerForContextMenu(mList);

        localRoot.findViewById(R.id.go_to_prev_page)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(getFragmentManager().getBackStackEntryCount()>0) {
                            getFragmentManager().popBackStack();
                        }else{
                            getActivity().finish();
                        }
                    }
                });

        return localRoot;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        getFragmentManager().beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        NoteViewFragment.newInstance(mPadding, mListAdapterItems.get(arg2).getId())
                )
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        setupNotesList();

        reqisterRecieverToGetPadding();
        Broadcast.requestForGetPaddindForSosButton(getActivity());
    }

    @Override
    public void onPause() {
        Broadcast.requestToHideButton(getActivity());

        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        mPosition = info.position;

        menu.setHeaderTitle(getResources().getString(R.string.CtxMenuHeader));

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        TextView tv = (TextView) mList.getChildAt(mPosition);
        String title = tv.getText().toString();

        // performing one of actions, depending on user choice
        switch (item.getItemId()) {

            case R.id.showNote:
                getFragmentManager().beginTransaction()
                        .replace(
                                R.id.fragmentContainer,
                                NoteViewFragment.newInstance(mPadding, mListAdapterItems.get(mPosition).getId()))
                        .addToBackStack(null)
                        .commit();
                break;

            case R.id.editNote:
                getFragmentManager().beginTransaction()
                        .replace(
                                R.id.fragmentContainer,
                                NoteAddEditFragment.newInstance(
                                        mListAdapterItems.get(mPosition).getId(),
                                        title
                                )
                        )
                        .addToBackStack(null)
                        .commit();
                Log.d(TAG, title);
                break;

            case R.id.removeNote:
                // removing this notes
                DBHelper.getInstance(getActivity()).removeNote(mListAdapterItems.get(mPosition).getId());
                // refreshing the listView
                setupNotesList();
                break;

        }

        return false;
    }

    private void reqisterRecieverToGetPadding() {
        final PaddingReceiver paddingReciever = new PaddingReceiver();
        IntentFilter intentFilter = new IntentFilter("launcher.send.padding.for.sos.button");
        getActivity().getApplication().registerReceiver(paddingReciever, intentFilter);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                getActivity().getApplication().unregisterReceiver(paddingReciever);
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    // Send back broadcast with padding for SOS button state.
    public class PaddingReceiver extends BroadcastReceiver {
        private final String PADDING = "padding";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(PADDING)) {
                int padding = intent.getIntExtra(PADDING, 0);
                setPadding(padding);
            }
        }

        private void setPadding(int padding) {
            mPadding = padding;
            getActivity().getWindow().getDecorView().findViewById(R.id.rootView)
                    .setPadding(0, 0, 0, padding);
        }
    }

    private void setupNotesList() {
        mListTitles = new ArrayList<>();
        mListAdapterItems = new ArrayList<>();

        // getting readable database
        SQLiteDatabase db = DBHelper.getInstance(getActivity()).getReadableDatabase();
        // getting notes from db
        // see mDbHelper for more details
        Cursor notes = DBHelper.getInstance(getActivity()).getNotes2(db);

        // populating ArrayList mListAdapterItems with notes mListTitles
        if (notes.moveToFirst()) {
            do {
                mListAdapterItems.add(new Item(notes.getShort(0), notes.getString(1)));
            } while (notes.moveToNext());
        }
        notes.close();
        db.close();

        for (Item i : mListAdapterItems) {
            mListTitles.add(i.getTitle());
        }

        // creating new mListAdapter
        mListAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item, mListTitles);
        mList.setAdapter(mListAdapter);
        // setting listener to the listView
        mList.setOnItemClickListener(this);
    }
}