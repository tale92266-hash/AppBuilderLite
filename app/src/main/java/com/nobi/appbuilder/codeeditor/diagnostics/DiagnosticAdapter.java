package com.nobi.appbuilder.codeeditor.diagnostics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;

import java.util.ArrayList;
import java.util.List;

/** Diagnostic rows; tap navigates to the file and line. */
public class DiagnosticAdapter
        extends RecyclerView.Adapter<DiagnosticAdapter.Holder> {

    public interface OnDiagnosticListener {
        void onDiagnostic(CodeDiagnostic diagnostic);
    }

    private final List<CodeDiagnostic> items = new ArrayList<>();
    private final OnDiagnosticListener listener;

    public DiagnosticAdapter(OnDiagnosticListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CodeDiagnostic> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_code_diagnostic, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CodeDiagnostic diagnostic = items.get(position);
        holder.file.setText(diagnostic.getFile().getName() + ":"
                + diagnostic.getLine());
        holder.message.setText(diagnostic.getMessage());
        holder.type.setText(diagnostic.getType().name());
        holder.type.setTextColor(
                diagnostic.getType() == CodeDiagnosticType.ERROR
                        ? 0xFFB3261E
                        : diagnostic.getType() == CodeDiagnosticType.WARNING
                        ? 0xFF8D6E00 : 0xFF2E7D32);
        holder.itemView.setOnClickListener(v -> listener.onDiagnostic(
                diagnostic));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView file;
        final TextView message;
        final TextView type;

        Holder(@NonNull View itemView) {
            super(itemView);
            file = itemView.findViewById(R.id.text_diagnostic_file);
            message = itemView.findViewById(R.id.text_diagnostic_message);
            type = itemView.findViewById(R.id.text_diagnostic_type);
        }
    }
}
