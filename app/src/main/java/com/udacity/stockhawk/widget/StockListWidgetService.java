package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by AK00481127 on 3/6/2017.
 */

public class StockListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new StockListRemoteViewsFactory(this.getApplicationContext(), intent);

    }

}
