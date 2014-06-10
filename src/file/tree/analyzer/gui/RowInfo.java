/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer.gui;

import javafx.scene.paint.Color;

/**
 *
 * @author ansy
 */
public class RowInfo {

    private String key;
    private String value;
    private String newValue;
    private Color color;

    public RowInfo(String key, String value, String newValue, Color color) {
        this.key = key;
        this.value = value;
        this.newValue = newValue;
        this.color = color;
        if (!value.equals(newValue) && !"Name".equals(key)) {
            this.color = Color.BLUE;
        }
    }

    public RowInfo(String key, String value) {
        this(key, value, "", Color.BLACK);
    }

    public RowInfo(String key, String value, String newValue) {
        this(key, value, newValue, Color.BLACK);

    }

    /**
     * Get the value of color
     *
     * @return the value of color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get the value of newValue
     *
     * @return the value of newValue
     */
    public String getNewValue() {
        return newValue;
    }

    /**
     * Get the value of value
     *
     * @return the value of value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the value of key
     *
     * @return the value of key
     */
    public String getKey() {
        return key;
    }

}
