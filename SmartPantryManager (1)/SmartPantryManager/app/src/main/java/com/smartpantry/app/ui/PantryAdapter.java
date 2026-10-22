package com.smartpantry.app.ui;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.app.R;
import com.smartpantry.app.data.PantryItem;
import com.smartpantry.app.util.AppPrefs;
import com.smartpantry.app.util.TextFormat;

/**
 * Custom RecyclerView adapter that binds PantryItem rows from the database to item_pantry.xml.
 * ListAdapter + DiffUtil animates only the rows that actually changed.
 */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.ViewHolder> {

    /** Callbacks implemented by the Fragment; the adapter itself never touches the database. */
    public interface Listener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private static final DiffUtil.ItemCallback<PantryItem> DIFF = new DiffUtil.ItemCallback<PantryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
            return a.getId() == b.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
            return a.getName().equals(b.getName())
                    && a.getQuantity() == b.getQuantity()
                    && a.getUnit().equals(b.getUnit())
                    && a.getExpiryDate() == b.getExpiryDate()
                    && a.getMinQuantity() == b.getMinQuantity();
        }
    };

    private final Listener listener;
    private boolean alertsEnabled = true;

    public PantryAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    /** Called when the "expiring-soon alerts" setting changes so rows are re-drawn. */
    public void setAlertsEnabled(boolean enabled) {
        if (alertsEnabled != enabled) {
            alertsEnabled = enabled;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvQuantity;
        private final TextView tvExpiry;
        private final TextView tvLow;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvQuantity = itemView.findViewById(R.id.tv_item_quantity);
            tvExpiry = itemView.findViewById(R.id.tv_item_expiry);
            tvLow = itemView.findViewById(R.id.tv_item_low);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(final PantryItem item) {
            Context context = itemView.getContext();
            tvName.setText(item.getName());
            tvLow.setVisibility(item.isLowStock() ? View.VISIBLE : View.GONE);
            tvQuantity.setText(TextFormat.formatQuantity(item.getQuantity()) + " " + item.getUnit());

            int color = R.color.pantry_text_secondary;
            String expiryText;
            if (!item.hasExpiry()) {
                expiryText = context.getString(R.string.no_expiry);
            } else {
                String date = TextFormat.formatDate(item.getExpiryDate());
                int days = TextFormat.daysUntil(item.getExpiryDate(), System.currentTimeMillis());
                if (days < 0) {
                    expiryText = context.getString(R.string.expired_on, date);
                    color = R.color.pantry_red;
                } else if (alertsEnabled && days <= AppPrefs.EXPIRY_WARNING_DAYS) {
                    expiryText = context.getString(R.string.expiring_soon, date);
                    color = R.color.pantry_orange;
                } else {
                    expiryText = context.getString(R.string.expires_on, date);
                }
            }
            tvExpiry.setText(expiryText);
            tvExpiry.setTextColor(ContextCompat.getColor(context, color));

            itemView.setOnClickListener(v -> listener.onEdit(item));
            btnDelete.setOnClickListener(v -> listener.onDelete(item));
        }
    }
}
