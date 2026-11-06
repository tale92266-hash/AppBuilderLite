package com.nobi.appbuilder.ui.code;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.generator.GenerationError;
import com.nobi.appbuilder.generator.GenerationResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Code Studio: the file explorer over the generated project on top,
 * the read-only source viewer below. Refresh re-runs generation;
 * copy copies the current file; search searches inside it.
 */
public class CodeStudioActivity extends AppCompatActivity
        implements CodeGenerationController.Listener {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private CodeGenerationController controller;
    private CodeFileAdapter adapter;
    private CodePreviewFragment previewFragment;
    private ProgressBar progressBar;
    private TextView statusText;
    private View contentView;
    private String currentPath;
    private GenerationResult lastResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_studio);

        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(this, R.string.code_load_failed,
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar_code_studio);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.code_studio_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progress_code_generating);
        statusText = findViewById(R.id.text_code_status);
        contentView = findViewById(R.id.layout_code_content);

        RecyclerView fileList = findViewById(R.id.recycler_code_files);
        fileList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CodeFileAdapter(path -> showFile(path));
        fileList.setAdapter(adapter);

        previewFragment = CodePreviewFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_code_preview, previewFragment)
                .commit();

        controller = new CodeGenerationController(this, projectId, this);
        generate();
    }

    private void generate() {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText(R.string.code_generating);
        contentView.setVisibility(View.GONE);
        controller.generate();
    }

    @Override
    public void onGenerated(GenerationResult result) {
        lastResult = result;
        progressBar.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        Set<String> errorFiles = new HashSet<>();
        for (GenerationError error : result.getReport().getErrors()) {
            if (error.getFile() != null) {
                errorFiles.add(error.getFile());
            }
        }
        adapter.setFiles(result.getAllPaths(), errorFiles);
        String summary = getString(R.string.code_files_count,
                result.getAllPaths().size(),
                result.getReport().getScreensGenerated(),
                result.getReport().getComponentsGenerated());
        if (!result.isSuccess()) {
            statusText.setText(getString(R.string.code_generation_failed,
                    result.getReport().getErrors().size()));
            showReportDialog(result);
        } else {
            statusText.setText(summary);
        }
        // Open the first layout file by default.
        for (String path : result.getAllPaths()) {
            if (path.contains("/layout/") && path.endsWith(".xml")) {
                showFile(path);
                break;
            }
        }
    }

    @Override
    public void onFailed(String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void showFile(String path) {
        if (lastResult == null) {
            return;
        }
        String content = lastResult.getTextFile(path);
        if (content == null) {
            Toast.makeText(this, R.string.code_binary_file,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        currentPath = path;
        adapter.setSelectedPath(path);
        previewFragment.setFile(path, content);
    }

    private void showReportDialog(GenerationResult result) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.code_generation_failed_title)
                .setMessage(result.getReport().toText())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_code_studio, menu);
        MenuItem searchItem = menu.findItem(R.id.action_code_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.code_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                int count = previewFragment.search(query);
                if (count == 0) {
                    Toast.makeText(CodeStudioActivity.this,
                            R.string.code_no_results, Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_code_refresh) {
            generate();
            return true;
        }
        if (id == R.id.action_code_copy) {
            String content = previewFragment.getCurrentContent();
            if (content == null || content.isEmpty()) {
                return true;
            }
            ClipboardManager clipboard =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(
                    currentPath == null ? "code" : currentPath, content));
            Toast.makeText(this, R.string.code_copied,
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_code_search_next) {
            if (!previewFragment.nextMatch()) {
                Toast.makeText(this, R.string.code_no_results,
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.release();
        }
    }
}
