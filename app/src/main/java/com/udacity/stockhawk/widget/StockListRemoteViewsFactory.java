package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

/**
 * Created by AK00481127 on 3/6/2017.
 */

public class StockListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String[] QUOTE_COLUMNS = {
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE
    };
    private Cursor data = null;
    private Context con = null;
    private int mAppWidgetId;

    public StockListRemoteViewsFactory(Context context, Intent intent) {
        con = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

        //initialising cursor with data
        data = con.getContentResolver().query(Contract.Quote.URI,
                QUOTE_COLUMNS,
                null,
                null,
                null);


    }

    @Override
    public RemoteViews getViewAt(int position) {

        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }

        RemoteViews views = new RemoteViews(con.getPackageName(),
                R.layout.widget_item);

        String symbol = data.getString(data.getColumnIndex("symbol"));
        float price = data.getFloat(data.getColumnIndex("price"));
        float change = data.getFloat(data.getColumnIndex("absolute_change"));
        float per_change = data.getFloat(data.getColumnIndex("percentage_change"));


        if (PrefUtils.getDisplayMode(con)
                .equals(con.getString(R.string.pref_display_mode_absolute_key))) {
            views.setTextViewText(R.id.stock_text_view, symbol + "  Price:  " + String.valueOf(price) + " Abs. Change: " + change);

        } else {
            views.setTextViewText(R.id.stock_text_view, symbol + "  Price:  " + String.valueOf(price) + " % Change: " + per_change);
        }


        return views;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        int item = 0;
        if (data.moveToPosition(position)) {
            item = data.getInt(data.getColumnIndex(Contract.Quote._ID));
        }
        return item;
    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
