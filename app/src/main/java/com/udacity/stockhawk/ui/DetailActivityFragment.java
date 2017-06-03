package com.udacity.stockhawk.ui;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.CustomMarkerView;
import com.udacity.stockhawk.utils.XAxisDateFormatter;
import com.udacity.stockhawk.utils.YAxisPriceFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.R.color.white;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String[] QUOTE_COLUMNS = {
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_HISTORY,
            Contract.Quote.COLUMN_STOCK_NAME,
            Contract.Quote.COLUMN_STOCK_EXCHANGE,
            Contract.Quote.COLUMN_DAY_LOWEST,
            Contract.Quote.COLUMN_DAY_HIGHEST,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE
    };
    @BindView(R.id.stock_name)
    public TextView tvStockName;
    @BindView(R.id.stock_exchange)
    public TextView tvStockExchange;
    @BindView(R.id.stock_price)
    public TextView tvStockPrice;
    @BindView(R.id.day_highest)
    public TextView tvDayHighest;
    @BindView(R.id.day_lowest)
    public TextView tvDayLowest;
    @BindView(R.id.absolute_change)
    public TextView tvAbsoluteChange;
    private LineChart mChart;
    private Cursor data;


    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


        tvStockName = (TextView) rootView.findViewById(R.id.stock_name);
        tvStockExchange = (TextView) rootView.findViewById(R.id.stock_exchange);
        tvStockPrice = (TextView) rootView.findViewById(R.id.stock_price);
        tvDayHighest = (TextView) rootView.findViewById(R.id.day_highest);
        tvDayLowest = (TextView) rootView.findViewById(R.id.day_lowest);
        tvAbsoluteChange = (TextView) rootView.findViewById(R.id.absolute_change);

        mChart = (LineChart) rootView.findViewById(R.id.stock_chart);

        //Setting Data for Line Chart
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String symbol = intent.getStringExtra(Intent.EXTRA_TEXT);
            setData(symbol);
        }

        // no description text
        // mChart.getDescription().setEnabled(false);

        // enable touch gestures
        // mChart.setTouchEnabled(true);

        // mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        //  mChart.setDragEnabled(true);
        // mChart.setScaleEnabled(true);
        // mChart.setDrawGridBackground(false);
        // mChart.setHighlightPerDragEnabled(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.GRAY);
        //mChart.setViewPortOffsets(0f, 0f, 0f, 0f);


        //mChart.invalidate();


        // get the legend (only possible after setting data)
        //Legend l = mChart.getLegend();
        // l.setEnabled(false);

        return rootView;
    }


    private Entry getLastButOneData(List<Entry> dataPairs) {
        if (dataPairs.size() > 2) {
            return dataPairs.get(dataPairs.size() - 2);
        } else {
            return dataPairs.get(dataPairs.size() - 1);
        }
    }

    // We will extract data from History Column for given stock
    public void setData(String symbol) {
        List<String> historyData = new ArrayList<String>();

        Uri stockUri = Contract.Quote.makeUriForStock(symbol);

        String selectionClause = Contract.Quote.COLUMN_SYMBOL + " = ?";
        String[] selectionArgs = {symbol};

        data = getActivity().getContentResolver().query(stockUri
                , QUOTE_COLUMNS
                , selectionClause
                , selectionArgs
                , null
        );

        if (data != null && data.getCount() > 0) {
            data.moveToFirst();
            String stockHistroy = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            float stockPrice = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PRICE));
            float absoluteChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
            String stockName = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_STOCK_NAME));
            String stockExchange = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_STOCK_EXCHANGE));
            float dayLowest = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_DAY_LOWEST));
            float dayHighest = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_DAY_HIGHEST));


            tvStockName.setText(stockName);
            tvStockExchange.setText(stockExchange);
            tvStockPrice.setText(String.valueOf(stockPrice));
            tvDayHighest.setText(String.valueOf(dayHighest));
            tvDayLowest.setText(String.valueOf(dayLowest));
            tvAbsoluteChange.setText(String.valueOf(absoluteChange));

            if (stockHistroy != null) {
                String[] splitQuoteHistory = stockHistroy.split("\\n");
                historyData = Arrays.asList(splitQuoteHistory);
            } else {
                Timber.d("Error Ouccred ! No Stock History Found");
            }

            if (historyData.size() > 0) {
                Iterator<String> it = historyData.iterator();
                String stockDataWithDate;
                String[] splitDataAndTime;
                //float price;

                ArrayList<Entry> values = new ArrayList<Entry>();
                ArrayList<Float> timeData = new ArrayList<Float>();
                ArrayList<Float> priceData = new ArrayList<Float>();
                String dateFormat = "dd/M/yyyy";


                while (it.hasNext()) {
                    stockDataWithDate = it.next();
                    splitDataAndTime = stockDataWithDate.split(",");
                    timeData.add(Float.valueOf(splitDataAndTime[0]));
                    priceData.add(Float.valueOf(splitDataAndTime[1]));
                }
                Collections.reverse(timeData);
                Collections.reverse(priceData);

                Float referenceTime = timeData.get(0);
                for (int i = 0; i < timeData.size(); i++) {
                    values.add(new Entry(timeData.get(i) - referenceTime, priceData.get(i)));
                }

                LineDataSet dataSet = new LineDataSet(values, "DataSet 1");


                LineData data = new LineData(dataSet);


                // set data
                mChart.setData(data);
                dataSet.setDrawFilled(true);
                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                XAxis xAxis = mChart.getXAxis();
                xAxis.setValueFormatter(new XAxisDateFormatter(dateFormat, referenceTime));
                xAxis.setDrawGridLines(false);
                xAxis.setAxisLineColor(white);
                xAxis.setAxisLineWidth(1.5f);
                xAxis.setTextColor(white);
                xAxis.setTextSize(12f);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


                YAxis yAxisRight = mChart.getAxisRight();
                yAxisRight.setEnabled(false);

                YAxis yAxis = mChart.getAxisLeft();
                yAxis.setValueFormatter(new YAxisPriceFormatter());
                yAxis.setDrawGridLines(false);
                yAxis.setAxisLineColor(white);
                yAxis.setAxisLineWidth(1.5f);
                yAxis.setTextColor(white);
                yAxis.setTextSize(12f);

                CustomMarkerView customMarkerView = new CustomMarkerView(getActivity(),
                        R.layout.marker_view, getLastButOneData(values), referenceTime);


                Legend legend = mChart.getLegend();
                legend.setEnabled(false);

                mChart.setMarker(customMarkerView);

            }


        }


    }


}
