package com.smartpantry.app.ui;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.app.R;
import com.smartpantry.app.ui.widget.ThinProgressView;

/** Custom adapter for the Suggested Recipes list (both "Ready to cook" and "Almost there"). */
public class RecipeAdapter extends ListAdapter<RecipeAdapter.Row, RecipeAdapter.ViewHolder> {

    /** What one list row needs to display. */
    public static final class Row {
        final int recipeId;
        final String title;
        final String subtitle;
        /** Share of the recipe's ingredients the pantry covers (1 = everything, shown in green). */
        final float coverage;

        public Row(int recipeId, String title, String subtitle, float coverage) {
            this.recipeId = recipeId;
            this.title = title;
            this.subtitle = subtitle;
            this.coverage = coverage;
        }
    }

    public interface Listener {
        void onRecipeClick(int recipeId);
    }

    private static final DiffUtil.ItemCallback<Row> DIFF = new DiffUtil.ItemCallback<Row>() {
        @Override
        public boolean areItemsTheSame(@NonNull Row a, @NonNull Row b) {
            return a.recipeId == b.recipeId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Row a, @NonNull Row b) {
            return a.title.equals(b.title) && a.subtitle.equals(b.subtitle) && a.coverage == b.coverage;
        }
    };

    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Row row = getItem(position);
        holder.tvTitle.setText(row.title);
        holder.tvSubtitle.setText(row.subtitle);
        holder.progress.setFraction(row.coverage);
        holder.progress.setFillColor(ContextCompat.getColor(holder.itemView.getContext(),
                row.coverage >= 1f ? R.color.pantry_green : R.color.pantry_red));
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(row.recipeId));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvSubtitle;
        final ThinProgressView progress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_recipe_title);
            tvSubtitle = itemView.findViewById(R.id.tv_recipe_subtitle);
            progress = itemView.findViewById(R.id.progress_recipe);
        }
    }
}
