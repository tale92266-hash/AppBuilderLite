package com.nobi.appbuilder.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.library.design.DesignToken;

import java.util.ArrayList;
import java.util.List;

/** Token rows: name + current value, tap to edit. */
public class DesignTokenController
        extends RecyclerView.Adapter<DesignTokenController.Holder> {

    public interface OnTokenActionListener {
        void onEditToken(DesignToken token);
    }

    private final List<DesignToken> tokens = new ArrayList<>();
    private final OnTokenActionListener listener;

    public DesignTokenController(OnTokenActionListener listener) {
        this.listener = listener;
    }

    public void setTokens(List<DesignToken> tokens) {
        this.tokens.clear();
        this.tokens.addAll(tokens);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_design_token, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        DesignToken token = tokens.get(position);
        holder.name.setText(token.getName());
        holder.value.setText(token.getValue());
        holder.itemView.setOnClickListener(v -> listener.onEditToken(token));
    }

    @Override
    public int getItemCount() {
        return tokens.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView value;

        Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_design_token_name);
            value = itemView.findViewById(R.id.text_design_token_value);
        }
    }
}
