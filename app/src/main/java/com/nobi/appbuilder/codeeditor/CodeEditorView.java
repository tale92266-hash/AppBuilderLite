package com.nobi.appbuilder.codeeditor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import androidx.appcompat.widget.AppCompatEditText;

import com.nobi.appbuilder.codeeditor.navigation.BracketMatcher;
import com.nobi.appbuilder.codeeditor.syntax.SyntaxColorResolver;
import com.nobi.appbuilder.codeeditor.syntax.SyntaxHighlighter;
import com.nobi.appbuilder.codeeditor.syntax.SyntaxTheme;
import com.nobi.appbuilder.codeeditor.syntax.SyntaxToken;

/**
 * A real code editor view: line-number gutter, current-line
 * highlight, incremental line-range syntax highlighting, bracket
 * matching, auto-indent and tab-to-spaces — without interfering with
 * the normal Android keyboard. Highlighting is applied as spans on
 * the live Editable, so the cursor and selection are never disturbed.
 */
public class CodeEditorView extends AppCompatEditText {

    private static final long HIGHLIGHT_DEBOUNCE_MS = 150;

    private final Paint gutterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gutterBackgroundPaint = new Paint();
    private final Paint currentLinePaint = new Paint();
    private final Handler highlightHandler = new Handler(Looper.getMainLooper());
    private final Runnable highlightRunnable = this::highlightVisibleRegion;

    private SyntaxHighlighter highlighter;
    private SyntaxColorResolver colorResolver;
    private EditorSettings settings;
    private boolean highlightScheduled;
    private BackgroundColorSpan bracketSpan;

    public CodeEditorView(Context context) {
        super(context);
        init();
    }

    public CodeEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CodeEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setHorizontallyScrolling(true);
        setTypeface(android.graphics.Typeface.MONOSPACE);
        gutterPaint.setTextSize(getTextSize() * 0.85f);
        gutterPaint.setTextAlign(Paint.Align.RIGHT);
        addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                scheduleHighlight();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setHighlighter(SyntaxHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    public void setColorResolver(SyntaxColorResolver colorResolver) {
        this.colorResolver = colorResolver;
        applyThemeColors();
    }

    public void setEditorSettings(EditorSettings settings) {
        this.settings = settings;
        setTextSize(settings.getFontSizeSp());
        applyThemeColors();
        updateGutterPadding();
    }

    private void applyThemeColors() {
        if (colorResolver == null) {
            return;
        }
        SyntaxTheme theme = colorResolver.getTheme();
        setBackgroundColor(theme.getBackground());
        setTextColor(theme.getTextColor());
        gutterPaint.setColor(theme.getLineNumberColor());
        gutterBackgroundPaint.setColor(theme.getBackground());
        currentLinePaint.setColor(theme.getCurrentLineColor());
    }

    public void scheduleHighlight() {
        if (highlightScheduled || highlighter == null
                || colorResolver == null) {
            return;
        }
        highlightScheduled = true;
        highlightHandler.postDelayed(highlightRunnable,
                HIGHLIGHT_DEBOUNCE_MS);
    }

    public void highlightNow() {
        highlightHandler.removeCallbacks(highlightRunnable);
        highlightScheduled = false;
        highlightVisibleRegion();
    }

    /** Highlights only the visible lines (+2 margin), not the whole
     * document, so typing stays responsive on long files. */
    private void highlightVisibleRegion() {
        highlightScheduled = false;
        if (highlighter == null || colorResolver == null
                || getLayout() == null || getText() == null) {
            return;
        }
        if (settings != null && !settings.isSyntaxHighlighting()) {
            return;
        }
        Editable editable = getText();
        int firstLine = Math.max(0, getLayout().getLineForVertical(
                getScrollY()) - 2);
        int lastLine = Math.min(getLineCount() - 1,
                getLayout().getLineForVertical(
                        getScrollY() + getHeight()) + 2);
        int start = getLayout().getLineStart(firstLine);
        int end = getLayout().getLineEnd(lastLine);
        ForegroundColorSpan[] oldSpans = editable.getSpans(start, end,
                ForegroundColorSpan.class);
        for (ForegroundColorSpan span : oldSpans) {
            editable.removeSpan(span);
        }
        String text = editable.toString();
        for (SyntaxToken token : highlighter.tokenize(text, start, end)) {
            int color = colorResolver.colorFor(token.getType());
            editable.setSpan(new ForegroundColorSpan(color),
                    token.getStart(), token.getEnd(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start,
                                 int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        int needed = getGutterWidth();
        if (getPaddingLeft() != needed) {
            setPadding(needed, getPaddingTop(), getPaddingRight(),
                    getPaddingBottom());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getLayout() == null) {
            super.onDraw(canvas);
            return;
        }
        // Current-line highlight.
        if (settings == null || settings.isCurrentLineHighlight()) {
            int line = getLayout().getLineForOffset(getSelectionStart());
            int top = getLayout().getLineTop(line);
            int bottom = getLayout().getLineBottom(line);
            canvas.drawRect(0, top + getPaddingTop(),
                    getWidth(), bottom + getPaddingTop(), currentLinePaint);
        }
        super.onDraw(canvas);
        // Line-number gutter.
        if (settings == null || settings.isLineNumbers()) {
            int gutterWidth = getGutterWidth();
            canvas.drawRect(0, 0, gutterWidth, getHeight(),
                    gutterBackgroundPaint);
            int firstLine = getLayout().getLineForVertical(getScrollY());
            int lastLine = getLayout().getLineForVertical(
                    getScrollY() + getHeight());
            for (int line = firstLine; line <= lastLine; line++) {
                int baseline = getLayout().getLineBaseline(line)
                        + getPaddingTop();
                canvas.drawText(String.valueOf(line + 1),
                        gutterWidth - 8, baseline, gutterPaint);
            }
        }
    }

    private int getGutterWidth() {
        int digits = String.valueOf(Math.max(1, getLineCount())).length();
        return (int) (digits * gutterPaint.getTextSize() * 0.7f) + 24;
    }

    private void updateGutterPadding() {
        setPadding(getGutterWidth(), getPaddingTop(), getPaddingRight(),
                getPaddingBottom());
    }

    public interface SelectionListener {
        void onSelectionChanged(int selStart, int selEnd);
    }

    private SelectionListener selectionListener;

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        updateBracketMatch(selStart, selEnd);
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selStart, selEnd);
        }
    }

    private void updateBracketMatch(int selStart, int selEnd) {
        Editable editable = getText();
        if (editable == null) {
            return;
        }
        if (bracketSpan != null) {
            editable.removeSpan(bracketSpan);
            bracketSpan = null;
        }
        if (settings != null && !settings.isBracketMatching()) {
            return;
        }
        if (selStart != selEnd) {
            return;
        }
        int offset = -1;
        if (selStart > 0 && selStart <= editable.length()) {
            char before = editable.charAt(selStart - 1);
            if ("(){}[]".indexOf(before) >= 0) {
                offset = selStart - 1;
            }
        }
        if (offset < 0 && selStart < editable.length()) {
            char at = editable.charAt(selStart);
            if ("(){}[]".indexOf(at) >= 0) {
                offset = selStart;
            }
        }
        if (offset < 0) {
            return;
        }
        int match = BracketMatcher.findMatching(editable.toString(), offset);
        if (match < 0) {
            return;
        }
        bracketSpan = new BackgroundColorSpan(0x6633B5E5);
        editable.setSpan(bracketSpan, match, match + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection base = super.onCreateInputConnection(outAttrs);
        if (base == null) {
            return null;
        }
        return new InputConnectionWrapper(base, true) {
            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                if (settings != null && settings.isAutoIndent()
                        && "\n".contentEquals(text)) {
                    return commitAutoIndent();
                }
                if ("\t".contentEquals(text)) {
                    int width = settings == null ? 4 : settings.getTabWidth();
                    StringBuilder spaces = new StringBuilder();
                    for (int i = 0; i < width; i++) {
                        spaces.append(' ');
                    }
                    return super.commitText(spaces.toString(),
                            newCursorPosition);
                }
                return super.commitText(text, newCursorPosition);
            }

            private boolean commitAutoIndent() {
                Editable editable = getText();
                if (editable == null) {
                    return super.commitText("\n", 1);
                }
                int cursor = getSelectionStart();
                int lineStart = cursor;
                while (lineStart > 0
                        && editable.charAt(lineStart - 1) != '\n') {
                    lineStart--;
                }
                String line = editable.subSequence(lineStart, cursor)
                        .toString();
                StringBuilder indent = new StringBuilder();
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == ' ' || c == '\t') {
                        indent.append(c);
                    } else {
                        break;
                    }
                }
                String trimmed = line.trim();
                if (trimmed.endsWith("{") || trimmed.endsWith("(")
                        || trimmed.endsWith("[")) {
                    int width = settings.getTabWidth();
                    for (int i = 0; i < width; i++) {
                        indent.append(' ');
                    }
                }
                return super.commitText("\n" + indent.toString(), 1);
            }
        };
    }

    @Override
    protected void onDetachedFromWindow() {
        highlightHandler.removeCallbacks(highlightRunnable);
        super.onDetachedFromWindow();
    }
}
