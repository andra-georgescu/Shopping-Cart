package com.andra.shoppingcart.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andra.shoppingcart.R;
import com.andra.shoppingcart.data.Produce;
import com.andra.shoppingcart.network.VolleySingleton;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

public class ProduceAdapter extends RecyclerView.Adapter<ProduceAdapter.ProduceViewHolder> {
    private List<Produce> mProduceList;

    public ProduceAdapter(List<Produce> produceList) {
        mProduceList = produceList;
    }

    @Override
    public ProduceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item_produce, parent, false);

        ProduceViewHolder vh = new ProduceViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ProduceViewHolder holder, int position) {
        final Produce produce = mProduceList.get(position);

        holder.mImage.setImageUrl(
                produce.getThumbnailUrl(), VolleySingleton.getInstance().getImageLoader());
        holder.mDescription.setText(
                produce.getName() + " " + produce.getQuantity());
        holder.mPrice.setText(
                produce.getCurrency().getSymbol() + Double.toString(produce.getPrice()));
        holder.mPricePerUnit.setText(
                "(" + produce.getCurrency().getSymbol() +
                        String.format("%.2f", produce.getPricePerUnit()) +
                        "/" + produce.getUnitDescription() + ")");

        holder.mAddToBasket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mAddToBasket.setVisibility(View.GONE);
                holder.mAddedToBasket.setVisibility(View.VISIBLE);
                holder.mQuantity.setText("x" + produce.increaseQty());
            }
        });

        holder.mMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = produce.decreaseQty();
                if (qty > 0) {
                    holder.mQuantity.setText("x" + qty);
                } else {
                    holder.mAddToBasket.setVisibility(View.VISIBLE);
                    holder.mAddedToBasket.setVisibility(View.GONE);
                }
            }
        });

        holder.mPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mQuantity.setText("x" + produce.increaseQty());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProduceList.size();
    }

    public static class ProduceViewHolder extends RecyclerView.ViewHolder {

        NetworkImageView mImage;
        TextView mDescription;
        TextView mPrice;
        TextView mPricePerUnit;
        TextView mAddToBasket;
        View mAddedToBasket;
        ImageView mMinus;
        ImageView mPlus;
        TextView mQuantity;

        public ProduceViewHolder(View itemView) {
            super(itemView);

            mImage = (NetworkImageView) itemView.findViewById(R.id.produceNetworkImage);
            mDescription = (TextView) itemView.findViewById(R.id.produceName);
            mPrice = (TextView) itemView.findViewById(R.id.producePrice);
            mPricePerUnit = (TextView) itemView.findViewById(R.id.pricePerUnit);
            mAddToBasket = (TextView) itemView.findViewById(R.id.addToBasket);
            mAddedToBasket = itemView.findViewById(R.id.addedToBasket);
            mMinus = (ImageView) itemView.findViewById(R.id.minus);
            mPlus = (ImageView) itemView.findViewById(R.id.plus);
            mQuantity = (TextView) itemView.findViewById(R.id.quantity);
        }
    }
}
