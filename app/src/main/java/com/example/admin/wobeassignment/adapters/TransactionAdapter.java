package com.example.admin.wobeassignment.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.admin.wobeassignment.R;
import com.example.admin.wobeassignment.model.TransactionModel;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.FontManager;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/*
   Adapter to set the transaction list obtained as response from the dashboard API to the recycler view
*/
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private Context context;
    private List<TransactionModel> transactionModelList;

    public TransactionAdapter(Context context) {
        this.context = context;
        transactionModelList = new ArrayList<>();
    }

    public void setDataInAdapter(List<TransactionModel> transaction) {
        this.transactionModelList = transaction;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_recent_transaction, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final TransactionModel model = transactionModelList.get(position);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        holder.ivTransactionImage.setTypeface(iconFont);

        /*
          Data from the list is shown in the screen

           If the fromCustomerId received from the JSON response = customerId saved in Shared Preference,
           then the credits is a sent value. Icon and receiver details are shown

           If the toCustomerId received from the JSON response = customerId saved in Shared Preference,
            then the credits is a received value

            The received value can be either from WOBE(10000 credits on regitration) or any other customer
            So, if the fromCustomerId = 999999999(WOBE customerId), then the credits is an added value
            else it is a received value. Icon and sender value is shown

         */
        if (model != null) {

            if (model.getTransactionDate() != null) {
                holder.tvTransactionTimestamp.setText(model.getTransactionDate());
            } else {
                holder.tvTransactionTimestamp.setVisibility(View.GONE);
            }

            if (SharedPreferenceManager.getInstance(context).getString(Constants.EMAIL).
                    equalsIgnoreCase(model.getFromCustomerID())) {
                holder.ivTransactionImage.setText(context.getResources().getString(R.string.sent_icon));
                if (model.getToFirstName() != null && model.getToLastName() != null) {
                    holder.tvTransactionName.setText(model.getToFirstName() + " " + model.getToLastName());
                } else if (model.getToFirstName() != null) {
                    holder.tvTransactionName.setText(model.getToFirstName());
                } else {
                    holder.tvTransactionName.setVisibility(View.GONE);
                }
                holder.ivTransactionImage.setTextColor(context.getResources().getColor(R.color.google_red));
            } else if (SharedPreferenceManager.getInstance(context).getString(Constants.EMAIL).
                    equalsIgnoreCase(model.getToCustomerID())) {
                if (model.getFromCustomerID().compareTo(BigInteger.valueOf(999999999).toString()) == 0) {
                    holder.ivTransactionImage.setText(context.getResources().getString(R.string.added_icon));
                    holder.ivTransactionImage.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    if (model.getFromFirstName() != null && model.getFromLastName() != null) {
                        holder.tvTransactionName.setText(model.getFromFirstName() + " " + model.getFromLastName());
                    } else if (model.getFromFirstName() != null) {
                        holder.tvTransactionName.setText(model.getFromFirstName());
                    } else {
                        holder.tvTransactionName.setVisibility(View.GONE);
                    }
                } else {
                    holder.ivTransactionImage.setText(context.getResources().getString(R.string.received_icon));
                    holder.ivTransactionImage.setTextColor(context.getResources().getColor(R.color.colorAccent));
                    if (model.getFromFirstName() != null && model.getFromLastName() != null) {
                        holder.tvTransactionName.setText(model.getFromFirstName() + " " + model.getFromLastName());
                    } else if (model.getFromFirstName() != null) {
                        holder.tvTransactionName.setText(model.getFromFirstName());
                    } else {
                        holder.tvTransactionName.setVisibility(View.GONE);
                    }
                }
            }

            if (model.getCredits() != 0) {
                holder.tvCredits.setText(Float.toString(model.getCredits()));
            } else {
                holder.tvCredits.setVisibility(View.GONE);
            }

            if (model.getDescription() != null) {
                holder.tvTransactionDescription.setText(model.getDescription());
            } else {
                holder.tvTransactionDescription.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return transactionModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView tvTransactionTimestamp, tvCredits, tvTransactionName, tvTransactionDescription;
        private TextView ivTransactionImage;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTransactionTimestamp = (TextView) itemView.findViewById(R.id.tvTransactionTimestamp);
            tvCredits = (TextView) itemView.findViewById(R.id.tvCredits);
            tvTransactionName = (TextView) itemView.findViewById(R.id.tvTransactionName);
            tvTransactionDescription = (TextView) itemView.findViewById(R.id.tvTransactionDescription);
            ivTransactionImage = (TextView) itemView.findViewById(R.id.ivTransactionImage);
            mView = itemView;
        }

    }
}
