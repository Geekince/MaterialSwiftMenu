package com.kince.widget.listenter;

/**
 * Created by Kince183 on 2017/7/13.
 *
 * Interface for listening to the state changes of the menu
 */
public interface StateChangeListener {

    /**
     * Fired when the ArcMenu is opened
     */
    void onMenuOpened();

    /**
     * Fired when the arc menu is closed
     */
    void onMenuClosed();

}
