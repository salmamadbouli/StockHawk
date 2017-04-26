package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class DetailActivity extends AppCompatActivity {

    Stock stock;
    StockQuote quote;
    List<HistoricalQuote> stockHistQuotes;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        final String symbol = intent.getStringExtra(MainActivity.EXTRA_STOCK_SYMBOL);
        setTitle(symbol);
        final TextView tv = (TextView) findViewById(R.id.tv_detail);

        new AsyncTask<String, Void, Stock>() {

            @Override
            protected Stock doInBackground(String... strings) {
                try {

                    Calendar from = Calendar.getInstance();
                    Calendar to = Calendar.getInstance();
                    from.add(Calendar.YEAR, -5);

                    stock = YahooFinance.get(symbol, true);
                    stockHistQuotes = stock.getHistory();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return stock;
            }

            @Override
            protected void onPostExecute(Stock stock) {
                super.onPostExecute(stock);
                quote = stock.getQuote();
                setTitle(stock.getName());
                Collections.sort(stockHistQuotes, new QuoteComparator());


                LineChart chart = (LineChart) findViewById(R.id.chart);
                List<Entry> entries = new ArrayList<Entry>();
                for (HistoricalQuote h : stockHistQuotes) {
                    entries.add(new Entry(Float.valueOf(h.getDate().get(Calendar.MONTH)), Float.valueOf(String.valueOf(h.getHigh()))));
                }
                LineDataSet dataSet = new LineDataSet(entries, "Stock price in USD");
                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);
                chart.invalidate();
            }
        }.execute();

    }

    public class QuoteComparator implements Comparator<HistoricalQuote> {
        @Override
        public int compare(HistoricalQuote e1, HistoricalQuote e2) {

            if (e1.getDate().get(Calendar.MONTH) <
                    e2.getDate().get(Calendar.MONTH))
                return -1;
            else
                return 1;
        }
    }
}
