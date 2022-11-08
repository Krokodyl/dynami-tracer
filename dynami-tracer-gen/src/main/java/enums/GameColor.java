package enums;

import java.awt.*;

public enum GameColor {

    TRANSPARENT(new Color(100,0,0,0), Color.red.getRGB()),
    WHITE(Color.white, Color.white.getRGB()),
    BLACK(Color.black, Color.black.getRGB()),
    GREY(Color.darkGray, Color.darkGray.getRGB());

    private Color color;

    private int fileColor;

    GameColor(Color color, int fileColor) {
        this.color = color;
        this.fileColor = fileColor;
    }

    public Color getColor() {
        return color;
    }

    public static GameColor getGameColor(int fileColor) {
        for (GameColor value : values()) {
            if (value.fileColor == fileColor) return value;
        }
        return null;
    }

    public static GameColor getGameColor(EditorColor editorColor, CharSide side) {
        switch (side) {
            case ONE: switch (editorColor) {
                case BLACK:
                    //return TRANSPARENT;
                case RED:
                case DARK_BLUE:
                case YELLOW:
                    return BLACK;
                case LIGHT_BLUE:
                    return GREY;
                default:
                    return WHITE;
            }
            case TWO: switch (editorColor) {
                case BLACK:
                case RED:
                    return TRANSPARENT;
                case DARK_BLUE:
                    return BLACK;
                case YELLOW:
                case LIGHT_BLUE:
                    return GREY;
                default:
                    return WHITE;
            }
        }
        return null;
    }
}
