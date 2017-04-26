package com.udacity.stockhawk.Widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by abdelhadi madbouli on 26/04/2017.
 */

public class StocksWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = StocksWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] QUOTE_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
    };
    // these indices must match the projection
    static final int INDEX_QUOTE_ID = 0;
    static final int INDEX_QUOTE_SYMBOL = 1;
    static final int INDEX_QUOTE_PRICE = 2;
    static final int INDEX_QUOTE_ABSOLUTE_CHANGE = 3;
    static final int INDEX_QUOTE_PERCENT_CHANGE = 4;

    private DecimalFormat dollarFormatWithPlus;

    public StocksWidgetRemoteViewsService() {
    }

    public StocksWidgetRemoteViewsService(DecimalFormat dollarFormatWithPlus) {
        this.dollarFormatWithPlus = dollarFormatWithPlus;
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(data != null)
                    data.close();

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(Contract.Quote.URI,
                        QUOTE_COLUMNS, null, null, null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);

                String symbol = data.getString(INDEX_QUOTE_SYMBOL);
                int price = data.getInt(INDEX_QUOTE_PRICE);
                float rawAbsoluteChange = data.getFloat(INDEX_QUOTE_ABSOLUTE_CHANGE);
                int percentChange = data.getInt(INDEX_QUOTE_PERCENT_CHANGE);

                if (rawAbsoluteChange > 0) {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);

                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, price+"");
                views.setTextViewText(R.id.change, change);

                final Intent fillInIntent = new Intent();


                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_QUOTE_ID);
                return position;            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
