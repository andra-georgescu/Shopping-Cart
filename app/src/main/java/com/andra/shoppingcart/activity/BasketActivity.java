package com.andra.shoppingcart.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.andra.shoppingcart.R;
import com.andra.shoppingcart.application.ShoppingCartApplication;
import com.andra.shoppingcart.data.Produce;
import com.andra.shoppingcart.network.VolleySingleton;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class BasketActivity extends AppCompatActivity {

    // Since the API is free, we only have access to data with USD as a sole source currency
    private static final String API_DEFAULT_SOURCE_CURRENCY = "USD";

    private Spinner mCurrencySpinner;
    private TextView mTotalView;

    private ArrayAdapter<String> mCurrencySpinnerAdapter;

    // A map of all available currencies; it will be filled after a successful call to the API
    private TreeMap<String, String> mCurrencies;

    private List<Produce> mProduceList;

    // By default, the total price is shown in GBP; this may change
    // if the user selects another currency
    private String mCurrentCurrencyCode = "GBP";
    private String mCurrentCurrencyName;

    private double mTotal;

    // Network requests that must be cancelled should the activity be destroyed
    // before they complete, as they manipulate the UI
    private Request mCurrentRatesRequest;
    private Request mCurrentCurrenciesRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProduceList = (List<Produce>) getIntent().getSerializableExtra("produce");

        if (savedInstanceState != null) {
            if (mProduceList == null) {
                mProduceList = (List<Produce>) savedInstanceState.getSerializable("produce");
            }

            mCurrentCurrencyCode = savedInstanceState.getString("code", "GBP");
            mCurrentCurrencyName = savedInstanceState.getString("name");
        }

        if (mProduceList == null) {
            Log.e(BasketActivity.class.getSimpleName(), "null produce list");
            Toast.makeText(this,
                    "There was an error processing your basket. Please check the items and try again.",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        setContentView(R.layout.activity_basket);

        mCurrencySpinner = (Spinner) findViewById(R.id.currencySpinner);
        mTotalView = (TextView) findViewById(R.id.totalAmount);

        computeTotal();
        setTotal();

        mCurrencySpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        mCurrencySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCurrencySpinner.setAdapter(mCurrencySpinnerAdapter);

        // When the user selects a new currency, a new request for the involved rates is made to the API
        mCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currencyCode = mCurrencies.get(mCurrencySpinnerAdapter.getItem(position));
                requestRates(currencyCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Make the main request to get the list of all available currencies
        requestCurrencies();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putSerializable("produce", (Serializable) mProduceList);
        outState.putString("code", mCurrentCurrencyCode);
        outState.putString("name", mCurrentCurrencyName);
    }

    /**
     * Computes the total price of the produce added to the shopping basket.
     * Does not return anything, it just updates the mTotal variable.
     */
    private void computeTotal() {
        mTotal = 0;
        for (Produce produce : mProduceList) {
            mTotal += produce.getAddedQuantity() * produce.getPrice();
        }
    }

    /**
     * Shows the mTotal value inside a TextView. The final price is trimmed and only has two decimals.
     */
    private void setTotal() {
        mTotalView.setText(Currency.getInstance(mCurrentCurrencyCode).getSymbol() + " " + String.format("%.2f", mTotal));
    }

    /**
     * Network request for the list of all available currencies.
     */
    private void requestCurrencies() {
        cancelCurrentCurrenciesRequest();

        String url = "http://jsonrates.com/currencies.json";

        mCurrentCurrenciesRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        parseCurrenciesList(response);
                        updateCurrenciesSpinner();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BasketActivity.this,
                                "Something went wrong. Unable to get the list of currencies",
                                Toast.LENGTH_LONG).show();
                    }
                });

        VolleySingleton.getInstance().addToRequestQueue(mCurrentCurrenciesRequest);
    }

    /**
     * Renews the list of currencies contained in the spinner.
     */
    private void updateCurrenciesSpinner() {
        List<String> currencyNames = new ArrayList<>();
        currencyNames.addAll(mCurrencies.keySet());

        mCurrencySpinnerAdapter.clear();
        mCurrencySpinnerAdapter.addAll(currencyNames);
        mCurrencySpinner.setSelection(currencyNames.indexOf(mCurrentCurrencyName));
    }

    private void cancelCurrentCurrenciesRequest() {
        if (mCurrentCurrenciesRequest != null) {
            mCurrentCurrenciesRequest.cancel();
        }
    }

    /**
     * Network request for the rates needed to compute the new total.
     * Since the API can only give rates with USD as a source currency, we must do some math.
     * We request the rates for transforming USD to the currently displayed currency and to
     * the new currency (divide the total by the first rate to get the sum in USD,
     * multiply with the second to get the sum in the requested currency)
     */
    private void requestRates(final String requestedCurrency) {
        cancelCurrentRatesRequest();

        String url = "http://apilayer.net/api/live?access_key=" +
                ShoppingCartApplication.getCurrencyApiKey() +
                "&currencies=" + mCurrentCurrencyCode + "," + requestedCurrency;

        mCurrentRatesRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        parseRates(response, requestedCurrency);
                        setTotal();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showRatesErrorToast();
                    }
                });

        VolleySingleton.getInstance().addToRequestQueue(mCurrentRatesRequest);
    }

    private void cancelCurrentRatesRequest() {
        if (mCurrentRatesRequest != null) {
            mCurrentRatesRequest.cancel();
        }
    }

    /**
     * Parses the JSONObject received from the API as a response to the request to GET the
     * two rates needed to compute the new total.
     *
     * @param response the original JSONObject that was received from the API
     */
    private void parseRates(JSONObject response, String requestedCurrency) {
        try {
            JSONObject rates = response.getJSONObject("quotes");
            double divideBy = rates.getDouble(API_DEFAULT_SOURCE_CURRENCY + mCurrentCurrencyCode);
            double multiplyBy = rates.getDouble(API_DEFAULT_SOURCE_CURRENCY + requestedCurrency);

            mTotal = (mTotal / divideBy) * multiplyBy;
            mCurrentCurrencyCode = requestedCurrency;
        } catch (JSONException e) {
            Log.e(BasketActivity.class.getSimpleName(), "Error parsing rates JSON", e);
            showRatesErrorToast();
        }
    }

    private void showRatesErrorToast() {
        Toast.makeText(this, "Rates currently not available. Please try again later.", Toast.LENGTH_LONG).show();
    }

    /**
     * Parses the JSONObject received from the API as a response to the request to
     * GET the list of all currencies.
     *
     * @param response the original JSONObject that was received from the API
     */
    private void parseCurrenciesList(JSONObject response) {
        // We are using a tree map so that the keys are ordered as they're being added
        mCurrencies = new TreeMap<>();
        Iterator<String> currencyCodes = response.keys();
        while (currencyCodes.hasNext()) {
            String code = currencyCodes.next();
            if (code.equals(mCurrentCurrencyCode)) {
                mCurrentCurrencyName = response.optString(code);
            }
            mCurrencies.put(response.optString(code), code);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cancelAllRunningRequests();
    }

    private void cancelAllRunningRequests() {
        cancelCurrentCurrenciesRequest();
        cancelCurrentRatesRequest();
    }
}
