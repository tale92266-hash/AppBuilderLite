package com.nobi.appbuilder.ui.code;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nobi.appbuilder.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shows one generated file in a monospace TextView with lightweight
 * regex syntax highlighting (Java keywords/strings/comments, XML
 * tags/attributes/values) and in-file search with next-match
 * scrolling. Generated files are read-only.
 */
public class CodePreviewFragment extends Fragment {

    private static final Pattern JAVA_KEYWORD = Pattern.compile(
            "\\b(public|private|protected|class|extends|implements|static|"
                    + "final|void|int|long|float|double|boolean|new|return|"
                    + "if|else|for|while|switch|case|break|continue|this|"
                    + "super|import|package|try|catch|finally|throw|null|"
                    + "true|false|void|extends)\\b");
    private static final Pattern JAVA_STRING = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"");
    private static final Pattern JAVA_COMMENT = Pattern.compile(
            "//[^\n]*|/\\*[\\s\\S]*?\\*/");
    private static final Pattern XML_TAG = Pattern.compile(
            "</?[A-Za-z][A-Za-z0-9._:-]*|/?>");
    private static final Pattern XML_ATTR = Pattern.compile(
            "([A-Za-z_:][A-Za-z0-9_.:-]*)(=)");
    private static final Pattern XML_VALUE = Pattern.compile(
            "\"[^\"]*\"");

    private static final int COLOR_KEYWORD = Color.parseColor("#7C4DFF");
    private static final int COLOR_STRING = Color.parseColor("#2E7D32");
    private static final int COLOR_COMMENT = Color.parseColor("#9E9E9E");
    private static final int COLOR_TAG = Color.parseColor("#1565C0");
    private static final int COLOR_ATTR = Color.parseColor("#6A1B9A");
    private static final int COLOR_MATCH = Color.parseColor("#FFEB3B");

    private TextView codeText;
    private ScrollView scrollView;
    private String path;
    private String content = "";
    private final List<int[]> matches = new ArrayList<>();
    private int currentMatch = -1;
    private String lastQuery;

    public static CodePreviewFragment newInstance() {
        return new CodePreviewFragment();
    }

    public void setFile(String path, String content) {
        this.path = path;
        this.content = content == null ? "" : content;
        this.matches.clear();
        this.currentMatch = -1;
        this.lastQuery = null;
        if (codeText != null) {
            applyContent();
        }
    }

    public String getCurrentPath() {
        return path;
    }

    public String getCurrentContent() {
        return content;
    }

    /** Returns the number of matches for the query. */
    public int search(String query) {
        lastQuery = query;
        matches.clear();
        currentMatch = -1;
        if (codeText == null || query == null || query.isEmpty()) {
            if (codeText != null) {
                applyContent();
            }
            return 0;
        }
        Matcher matcher = Pattern.compile(Pattern.quote(query),
                Pattern.CASE_INSENSITIVE).matcher(content);
        while (matcher.find()) {
            matches.add(new int[]{matcher.start(), matcher.end()});
        }
        applyContent();
        if (!matches.isEmpty()) {
            nextMatch();
        }
        return matches.size();
    }

    /** Moves to the next match, wrapping around. Returns false if none. */
    public boolean nextMatch() {
        if (matches.isEmpty() || codeText == null) {
            return false;
        }
        currentMatch = (currentMatch + 1) % matches.size();
        applyContent();
        int[] span = matches.get(currentMatch);
        codeText.post(() -> {
            int line = codeText.getLayout() == null ? 0
                    : codeText.getLayout().getLineForOffset(span[0]);
            int y = codeText.getLayout() == null ? 0
                    : codeText.getLayout().getLineTop(line);
            scrollView.scrollTo(0, Math.max(0, y - 100));
        });
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_code_preview,
                container, false);
        codeText = view.findViewById(R.id.text_code_content);
        scrollView = view.findViewById(R.id.scroll_code_content);
        applyContent();
        return view;
    }

    private void applyContent() {
        SpannableStringBuilder highlighted = highlight(content);
        if (lastQuery != null && !lastQuery.isEmpty() && !matches.isEmpty()) {
            for (int i = 0; i < matches.size(); i++) {
                int[] span = matches.get(i);
                int color = i == currentMatch ? Color.parseColor("#FF9800")
                        : COLOR_MATCH;
                highlighted.setSpan(new BackgroundColorSpan(color),
                        span[0], span[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        codeText.setText(highlighted);
    }

    private SpannableStringBuilder highlight(String text) {
        SpannableStringBuilder out = new SpannableStringBuilder(text);
        boolean isXml = path != null && path.endsWith(".xml");
        boolean isGradle = path != null && path.endsWith(".gradle");
        if (isXml) {
            applyPattern(out, XML_TAG, COLOR_TAG);
            applyPattern(out, XML_ATTR, COLOR_ATTR);
            applyPattern(out, XML_VALUE, COLOR_STRING);
        } else if (isGradle) {
            applyPattern(out, JAVA_STRING, COLOR_STRING);
            applyPattern(out, JAVA_COMMENT, COLOR_COMMENT);
        } else {
            applyPattern(out, JAVA_COMMENT, COLOR_COMMENT);
            applyPattern(out, JAVA_STRING, COLOR_STRING);
            applyPattern(out, JAVA_KEYWORD, COLOR_KEYWORD);
        }
        return out;
    }

    private void applyPattern(SpannableStringBuilder out, Pattern pattern,
                              int color) {
        Matcher matcher = pattern.matcher(out);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (pattern == XML_ATTR) {
                // Highlight only the attribute name (group 1), not "=".
                start = matcher.start(1);
                end = matcher.end(1);
            }
            boolean overlapped = false;
            for (ForegroundColorSpan span
                    : out.getSpans(start, end, ForegroundColorSpan.class)) {
                int spanStart = out.getSpanStart(span);
                int spanEnd = out.getSpanEnd(span);
                if (spanStart < end && start < spanEnd) {
                    overlapped = true;
                    break;
                }
            }
            if (!overlapped) {
                out.setSpan(new ForegroundColorSpan(color), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
