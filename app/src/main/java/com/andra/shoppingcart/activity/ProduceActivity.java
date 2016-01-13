package com.andra.shoppingcart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.andra.shoppingcart.R;
import com.andra.shoppingcart.adapter.ProduceAdapter;
import com.andra.shoppingcart.data.Produce;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class ProduceActivity extends AppCompatActivity {

    private List<Produce> mProduceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produce);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mProduceList = (List<Produce>) savedInstanceState.getSerializable("produce");
        }

        if (mProduceList == null) {
            mProduceList = getProduceList();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new ProduceAdapter(mProduceList));

        // When clicking on the floating action button, the user is taken to the page
        // containing the total price of the selected produce.
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProduceActivity.this, BasketActivity.class);
                intent.putExtra("produce", (Serializable) mProduceList);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        // We save the produce list, so that we can remember what the user has added to the basket.
        // Each produce item in the list counts the number of times it has been added to the basket.
        outState.putSerializable("produce", (Serializable) mProduceList);
    }

    /**
     * Creates a static list of Produce objects. These objects could come from a dedicated API.
     *
     * @return a List<Produce> to be used for e.g. a list adapter
     */
    private List<Produce> getProduceList() {
        List<Produce> produceList = new ArrayList<>();

        Produce peas = new Produce("Garden Peas", "900G bag", "kg", 0.9, 0.95,
                Currency.getInstance("GBP"), "http://img.tesco.com/Groceries/pi/711/5031021614711/IDShot_110x110.jpg");
        Produce eggs = new Produce("Free Range Eggs", "Large Box Of 12", "pc", 12, 2.1,
                Currency.getInstance("GBP"), "http://img.tesco.com/Groceries/pi/471/5051140150471/IDShot_110x110.jpg");
        Produce milk = new Produce("Semi Skimmed Milk", "1L Bottle", "l", 1, 1.3,
                Currency.getInstance("GBP"), "http://img.tesco.com/Groceries/pi/457/5000436589457/IDShot_110x110.jpg");
        Produce beans = new Produce("Organic Red Kidney Beans", "380G", "kg", 0.38, 0.73,
                Currency.getInstance("GBP"), "http://img.tesco.com/Groceries/pi/574/5052004745574/IDShot_110x110.jpg");

        produceList.add(peas);
        produceList.add(eggs);
        produceList.add(milk);
        produceList.add(beans);

        return produceList;
    }

}
