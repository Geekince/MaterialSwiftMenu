package com.kince.widget.listenter;

public interface ComboClickListener {

    /**
     * 连续点击
     */
    void onComboClick();

    /**
     * 单击
     */
    void onSingleClick();

    /**
     * 动画结束
     */
    void onMenuClosed();

    /**
     * 判断是否显示动画效果
     *
     * @return
     */
    boolean isOpenMenu();

    /**
     * @param num
     */
    void onMenuClick(int num);

}