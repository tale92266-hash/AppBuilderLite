package com.nobi.appbuilder.history;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.LayoutModel;

/** Resizes (and repositions) one component; one entry per resize gesture. */
public class ResizeComponentCommand implements EditorCommand {

    private final ComponentModel model;
    private final int startX;
    private final int startY;
    private final int startWidth;
    private final int startHeight;
    private final int endX;
    private final int endY;
    private final int endWidth;
    private final int endHeight;

    public ResizeComponentCommand(ComponentModel model,
                                  int startX, int startY, int startWidth, int startHeight,
                                  int endX, int endY, int endWidth, int endHeight) {
        this.model = model;
        this.startX = startX;
        this.startY = startY;
        this.startWidth = startWidth;
        this.startHeight = startHeight;
        this.endX = endX;
        this.endY = endY;
        this.endWidth = endWidth;
        this.endHeight = endHeight;
    }

    @Override
    public boolean canExecute() {
        return model != null && !model.isLocked()
                && (startX != endX || startY != endY
                || startWidth != endWidth || startHeight != endHeight);
    }

    @Override
    public void execute() {
        apply(endX, endY, endWidth, endHeight);
    }

    @Override
    public void undo() {
        apply(startX, startY, startWidth, startHeight);
    }

    private void apply(int x, int y, int width, int height) {
        model.setX(x);
        model.setY(y);
        model.setWidth(width);
        model.setHeight(height);
        LayoutModel layout = model.getLayout();
        layout.setWidth(LayoutModel.dimensionToString(width));
        layout.setHeight(LayoutModel.dimensionToString(height));
    }

    @Override
    public String getDescription() {
        return "Resize " + model.getType();
    }
}
