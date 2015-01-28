package com.moczul.notepad;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.moczul.notepad.utils.Broadcast;
import com.moczul.notepad.utils.DBHelper;
import com.moczul.notepad.utils.Styling;

public class NoteAddEditFragment extends Fragment {
    private static final String TAG = "NoteAddEditFragment";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";

    private Button addNoteToDB;
    private EditText titleEditText;
    private EditText contentEditText;

    private int mId;

    public NoteAddEditFragment() {
        // Required empty public constructor
    }

    /**
     * Use this for Edit
     * @param id note id inside database
     * @param title note title
     * @return instance of fragment (not cached)
     */
    public static NoteAddEditFragment newInstance(int id, String title) {
        NoteAddEditFragment f = new NoteAddEditFragment();
        f.setArguments(new Bundle());
        f.getArguments().putInt(KEY_ID, id);
        f.getArguments().putString(KEY_TITLE, title);
        return f;
    }

    /**
     * Use this for New
     * @return instance of fragment (not cached)
     */
    public static NoteAddEditFragment newInstance() {
        return newInstance(0, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View localRoot = inflater.inflate(R.layout.fragment_note_add_edit, container, false);

        mId = getArguments().getInt(KEY_ID);

        localRoot.findViewById(R.id.go_to_prev_page)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getFragmentManager().popBackStack();
                    }
                });

        addNoteToDB = (Button) Styling.updateWithFont(
                getActivity(), (TextView) localRoot.findViewById(R.id.addNoteToDB),
                Styling.Fonts.LATO_BOLD
        );

        titleEditText = (EditText) localRoot.findViewById(R.id.TitleEditText);
        contentEditText = (EditText) localRoot.findViewById(R.id.ContentEditText);

        //we're checking if user want to edit note
        if(mId > 0) {
            Log.d(TAG, "isEdit");
            //getting the readable database
            SQLiteDatabase db = DBHelper.getInstance(getActivity()).getReadableDatabase();
            Cursor c = DBHelper.getInstance(getActivity()).getNote(db, mId);
            //here we're set title and content of note to editText views
            titleEditText.setText(c.getString(0));
            contentEditText.setText(c.getString(1));
            //and we're changing the button text to something more appropriate
            //from add note to update note
            //you can change button text in /res/values/strings.xml file
            addNoteToDB.setText(getResources().getString(R.string.updateNoteButton));
            c.close();
            db.close();
        }


        //setting listener for button
        addNoteToDB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //when user clicks button
                //we're grabbing the title and content from editText
                String title = titleEditText.getText().toString();
                String content = contentEditText.getText().toString();

                //if user left title or content field empty
                //we show the toast, and tell to user to fill the fields

                if (title.equals("") || content.equals("")) {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.validation), Toast.LENGTH_LONG).show();
                    return;
                }

                //adding note to db
                if (mId == 0) {
                    //if it isn't edit mode we just add a new note to db
                    DBHelper.getInstance(getActivity()).addNote(title, content);
                } else {
                    DBHelper.getInstance(getActivity()).updateNote(title, content, getArguments().getString(KEY_TITLE));
                }

                getFragmentManager().popBackStack();
            }
        });

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
