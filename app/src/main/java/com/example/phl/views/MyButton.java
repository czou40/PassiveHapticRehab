package com.example.phl.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.ViewCompat;

import com.example.phl.R;
import com.example.phl.services.RemoteControlService;
import com.google.android.material.button.MaterialButton;

public class MyButton extends MaterialButton {

    private Group group;

    private boolean forceRemoteControl = false;

    private boolean isReceiverRegistered = false;

    private BroadcastReceiver remoteControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra("command");
            Log.d("MyButton", "Received command: " + command);
            if (command != null) {
                Group commandGroup = Group.fromString(command);
                if (command.trim().equalsIgnoreCase(MyButton.this.getText().toString().trim())) {
                    if ((forceRemoteControl || isShown()) && isEnabled()) {
                        performClick();
                    }
                } else if (commandGroup != Group.OTHER && commandGroup == group) {
                    if ((forceRemoteControl || isShown()) && isEnabled()) {
                        performClick();
                    }
                }
            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerReceiverIfNotRegistered();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiverIfRegistered();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            registerReceiverIfNotRegistered();
        } else {
            // get container
            // if container is visible
            // then register receiver
            // else unregister receiver
            ViewParent parent = getParent();
            if (parent instanceof View) {
                View parentView = (View) parent;
                if (parentView.isShown() && forceRemoteControl) {
                    registerReceiverIfNotRegistered();
                } else {
                    unregisterReceiverIfRegistered();
                }
            } else {
                unregisterReceiverIfRegistered();
            }
        }
    }

    private void registerReceiverIfNotRegistered() {
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(RemoteControlService.ACTION);
            getContext().registerReceiver(remoteControlReceiver, filter);
            isReceiverRegistered = true;
            Log.d("MyButton", "Registered receiver");
        }
    }

    private void unregisterReceiverIfRegistered() {
        if (isReceiverRegistered) {
            getContext().unregisterReceiver(remoteControlReceiver);
            isReceiverRegistered = false;
            Log.d("MyButton", "Unregistered receiver");
        }
    }

    public MyButton(Context context) {
        super(context);
        init(context, null);
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
       if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyButton);
            try {
                int groupValue = typedArray.getInt(R.styleable.MyButton_group, Group.OTHER.getValue());
                group = Group.fromInt(groupValue);
                forceRemoteControl = typedArray.getBoolean(R.styleable.MyButton_force_remote_control, false);
            } finally {
                typedArray.recycle();
            }
        } else {
            group = Group.OTHER;
            forceRemoteControl = false;
        }
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group newGroup) {
        this.group = newGroup;
    }

    public enum Group {
        FORWARD(0),
        BACKWARD(1),
        EXIT(2),
        YES(3),
        NO(4),
        UNCERTAIN(5),
        START(6),
        STOP(7),
        OTHER(8);

        private final int value;

        Group(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Group fromInt(int value) {
            for (Group group : Group.values()) {
                if (group.getValue() == value) {
                    return group;
                }
            }
            return OTHER;
        }
        public static Group fromString(String name) {
            if (name != null) {
                for (Group group : Group.values()) {
                    if (name.equalsIgnoreCase(group.name())) {
                        return group;
                    }
                }
            }
            return OTHER;
        }
    }

}
