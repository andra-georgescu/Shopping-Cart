package com.andra.shoppingcart.data;

import java.io.Serializable;
import java.util.Currency;

public class Produce implements Serializable {
    private String mName;
    private String mQuantity;
    private String mUnitDescription;
    private double mNumberOfUnits;
    private double mPrice;
    private double mPricePerUnit;
    private Currency mCurrency;
    private String mThumbnailUrl;
    private int mAddedQty = 0;

    public Produce(String name, String quantity, String unitDescription, double numberOfUnits, double price, Currency currency, String thumbnailUrl) {
        mName = name;
        mQuantity = quantity;
        mUnitDescription = unitDescription;
        mNumberOfUnits = numberOfUnits;
        mPrice = price;
        mPricePerUnit = mPrice / mNumberOfUnits;
        mCurrency = currency;
        mThumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public String getName() {
        return mName;
    }

    public String getQuantity() {
        return mQuantity;
    }

    public String getUnitDescription() {
        return mUnitDescription;
    }

    public double getNumberOfUnits() {
        return mNumberOfUnits;
    }

    public double getPrice() {
        return mPrice;
    }

    public double getPricePerUnit() {
        return mPricePerUnit;
    }

    public Currency getCurrency() {
        return mCurrency;
    }

    public int increaseQty() {
        return ++mAddedQty;
    }

    public int decreaseQty() {
        return --mAddedQty;
    }

    public int getAddedQuantity() {
        return mAddedQty;
    }
}
