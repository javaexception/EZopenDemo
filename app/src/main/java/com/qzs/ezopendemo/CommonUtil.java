package com.qzs.ezopendemo;

import android.widget.Toast;




/**
 * @author Created by Liberty on 2017/6/11.
 */

public class CommonUtil {
    private static Toast toast;

    public static void showToast(
            String content) {
        if (toast == null) {
            toast = Toast.makeText(APP.getContext(),
                    content,
                    Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}
