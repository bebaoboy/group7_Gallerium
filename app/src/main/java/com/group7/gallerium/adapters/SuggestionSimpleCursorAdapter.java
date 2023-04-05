package com.group7.gallerium.adapters;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.group7.gallerium.R;
import com.group7.gallerium.activities.SearchResultActivity;
import com.group7.gallerium.utilities.SuggestionsDatabase;

public class SuggestionSimpleCursorAdapter
        extends CursorAdapter
{
    LayoutInflater cursorInflater;

    Context context;

    public SuggestionSimpleCursorAdapter(Context context, int layout, Cursor c,
                                         String[] from, int[] to, int flags) {
        super(context, c, flags);
        cursorInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.search_suggestion_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtTitle = view.findViewById(R.id.search_title);
        int indexColumnSuggestion = cursor.getColumnIndex(SuggestionsDatabase.FIELD_SUGGESTION);
        String suggestion = cursor.getString(indexColumnSuggestion);
        txtTitle.setText(suggestion);
//
//        view.setOnClickListener((v)->{
//            getSearchResults(suggestion);
//        });
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {

        int indexColumnSuggestion = cursor.getColumnIndex(SuggestionsDatabase.FIELD_SUGGESTION);

        return cursor.getString(indexColumnSuggestion);
    }

    private void getSearchResults(String toString) {
        Intent intent = new Intent(context, SearchResultActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SearchManager.QUERY, toString);
        context.startActivity(intent);
    }

}