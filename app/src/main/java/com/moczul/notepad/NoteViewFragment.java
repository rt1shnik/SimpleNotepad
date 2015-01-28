package com.moczul.notepad;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moczul.notepad.utils.Broadcast;
import com.moczul.notepad.utils.DBHelper;

public class NoteViewFragment extends Fragment {
    private static final String KEY_PADDING = "padding";
    private static final String KEY_ID = "id";

    private int mId = 0;
    private int mPadding;

    public NoteViewFragment() {
        // Required empty public constructor
    }

    public static NoteViewFragment newInstance(int padding, int id) {
        NoteViewFragment f = new NoteViewFragment();
        f.setArguments(new Bundle());
        f.getArguments().putInt(KEY_PADDING, padding);
        f.getArguments().putInt(KEY_ID, id);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mPadding = getArguments().getInt(KEY_PADDING);
        mId = getArguments().getInt(KEY_ID);

        View localRoot = inflater.inflate(R.layout.fragment_note_view, container, false);

        SQLiteDatabase db = DBHelper.getInstance(getActivity()).getReadableDatabase();

        //getting the note from database
        Cursor c = DBHelper.getInstance(getActivity()).getNote(db, mId);

        //setting notes to our views
        ((TextView) localRoot.findViewById(R.id.noteTitle)).setText(c.getString(0));
        ((TextView) localRoot.findViewById(R.id.noteContent)).setText(c.getString(1));
        ((TextView) localRoot.findViewById(R.id.createdAt)).setText(c.getString(2));

        c.close();
        db.close();

        localRoot.findViewById(R.id.go_to_prev_page)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getFragmentManager().popBackStack();
                    }
                });

        localRoot.findViewById(R.id.rootView).setPadding(0, 0, 0, mPadding);

        return localRoot;
    }

    @Override
    public void onResume() {
        super.onResume();

        Broadcast.requestForGetPaddindForSosButton(getActivity());
    }

    @Override
    public void onPause() {
        Broadcast.requestToHideButton(getActivity());

        super.onPause();
    }
}