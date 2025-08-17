package com.ahccode.common.context;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class GameContextUI {

    public static final Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    public static final double SCREEN_WIDTH = screenBounds.getWidth();
    public static final double SCREEN_HEIGHT = screenBounds.getHeight();

}
