package com.example.matte.smartkickertisch;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class PlayerButtonTag extends android.support.v7.widget.AppCompatButton {

    String playerUID;
    String playerNickName;
    boolean isHost = false;
    public PlayerButtonTag(Context context) {
        super(context);
    }

    public PlayerButtonTag(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerButtonTag(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
