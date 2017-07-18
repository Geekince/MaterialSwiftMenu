package com.kince.widget.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.kince.widget.MaterialSwiftMenu;
import com.kince.widget.listenter.ComboClickListener;
import com.kince.widget.listenter.StateChangeListener;

public class MainActivity extends AppCompatActivity {

    private MaterialSwiftMenu materialSwiftMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        materialSwiftMenu = (MaterialSwiftMenu) findViewById(R.id.arcMenu);
        materialSwiftMenu.setStateChangeListener(stateChangeListener);
        materialSwiftMenu.setComboClickListener(comboClickListener);
        materialSwiftMenu.updateCountView(1);
    }

    private StateChangeListener stateChangeListener = new StateChangeListener() {

        @Override
        public void onMenuOpened() {

        }

        @Override
        public void onMenuClosed() {

        }

    };

    private ComboClickListener comboClickListener = new ComboClickListener() {

        @Override
        public void onComboClick() {
            Log.i("Kince1","onComboClick");
        }

        @Override
        public void onSingleClick() {
            Log.i("Kince1","onSingleClick");
        }

        @Override
        public void onMenuClick(int num) {
            Log.i("Kince1","onMenuClick");
        }

        @Override
        public void onMenuClosed() {
            Log.i("Kince1","onMenuClosed");
        }

        @Override
        public boolean isOpenMenu() {
            return true;
        }

    };

}
