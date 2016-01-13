package com.andra.shoppingcart.application;

import android.app.Application;
import android.content.Context;

public class ShoppingCartApplication extends Application {

    private static final String CURRENCY_API_KEY = "a82f846b2360a196a59c7b8619dfa377";

    private static ShoppingCartApplication mInstance;
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        setAppContext(getApplicationContext());
    }

    public static ShoppingCartApplication getInstance() {
        return mInstance;
    }

    public static Context getAppContext() {
        return mAppContext;
    }

    public static String getCurrencyApiKey() {
        return CURRENCY_API_KEY;
    }

    public void setAppContext(Context appContext) {
        mAppContext = appContext;
    }
}
