package com.kince.widget;

/**
 * Created by Kince183 on 2017/7/13.
 * Present position for sub menu item
 */
public enum MenuSideEnum {

    ARC_BOTTOM_LEFT(0), ARC_BOTTOM_RIGHT(1), ARC_TOP_LEFT(2), ARC_TOP_RIGHT(3);

    int id;

    MenuSideEnum(int id) {
        this.id = id;
    }

    public static MenuSideEnum fromId(int id) {
        for (MenuSideEnum f : values()) {
            if (f.id == id) return f;
        }
        return ARC_BOTTOM_LEFT;
    }

}