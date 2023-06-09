package com.example.phl.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.phl.R;
import com.example.phl.services.RemoteControlService;
import com.google.android.material.button.MaterialButton;

public class MyButton extends MaterialButton {

    private Group group;

    private boolean allowRemoteControlWhenNotShown = false;

    private boolean isReceiverRegistered = false;

    private boolean preventAccidentalClicks = true;

    private OnClickListener onClickListener;

    private static final int DEFAULT_LONG_CLICK_DURATION = 0;

    private BroadcastReceiver remoteControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra("command");
            Log.d("MyButton", "Received command: " + command);
            if (command != null) {
                Group commandGroup = Group.fromString(command);
                if (command.trim().equalsIgnoreCase(MyButton.this.getText().toString().trim())) {
                    if ((allowRemoteControlWhenNotShown || isShown()) && isEnabled()) {
                        if (preventAccidentalClicks) {
                            if (onClickListener != null) {
                                onClickListener.onClick(MyButton.this);
                            }
                        } else {
                            performClick();
                        }
                    }
                } else if (commandGroup != Group.OTHER && commandGroup == group) {
                    if ((allowRemoteControlWhenNotShown || isShown()) && isEnabled()) {
                        if (preventAccidentalClicks) {
                            if (onClickListener != null) {
                                onClickListener.onClick(MyButton.this);
                            }
                        } else {
                            performClick();
                        }
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
                if (parentView.isShown() && allowRemoteControlWhenNotShown) {
                    registerReceiverIfNotRegistered();
                } else {
                    unregisterReceiverIfRegistered();
                }
            } else {
                unregisterReceiverIfRegistered();
            }
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
        if (preventAccidentalClicks) {
            if (onClickListener == null) {
                super.setOnTouchListener(null);
            } else {
                super.setOnTouchListener(new OnTouchListener() {
                    private long firstTime=0;
                    private long secondTime=0;

                    private CountDownTimer countDownTimer;

                    private Toast toast;

                    private boolean isBadTouch = false;

                    private boolean isInside(View v, MotionEvent e) {
                        return !(e.getX() < 0 || e.getY() < 0
                                || e.getX() > v.getMeasuredWidth()
                                || e.getY() > v.getMeasuredHeight());
                    }

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        int numFingers = motionEvent.getPointerCount();
                        if (numFingers > 1) {
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            if (!isBadTouch) {
                                if (toast != null) {
                                    toast.cancel();
                                }
                                toast = Toast.makeText(getContext(), "You can only use one finger to click the button.", Toast.LENGTH_SHORT);
                                toast.show();
                                isBadTouch = true;
                            }
                            return false;
                        }
                        if (!isInside(view, motionEvent)) {
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            if (!isBadTouch) {
                                if (toast != null) {
                                    toast.cancel();
                                }
                                toast = Toast.makeText(getContext(), "You canceled the clicking by moving outside of the button", Toast.LENGTH_SHORT);
                                toast.show();
                                isBadTouch = true;
                            }
                            return false;
                        }
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            isBadTouch = false;
                            firstTime = System.currentTimeMillis();
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            countDownTimer = new CountDownTimer(DEFAULT_LONG_CLICK_DURATION, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    if (toast != null) {
                                        toast.cancel();
                                    }
                                    toast = Toast.makeText(getContext(), "Press the button for " + (int) Math.ceil(millisUntilFinished / 1000.0) + " more second(s) to confirm.", Toast.LENGTH_SHORT);
                                    toast.show();
                                }

                                @Override
                                public void onFinish() {
                                    if (toast != null) {
                                        toast.cancel();
                                    }
                                    toast = Toast.makeText(getContext(), "Release the button to confirm.", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            };
                            countDownTimer.start();
                        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            if (isBadTouch) {
                                return false;
                            }
                            secondTime = System.currentTimeMillis();
                            if (secondTime - firstTime > DEFAULT_LONG_CLICK_DURATION) {
                                onClickListener.onClick(view);
                            } else {
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                                if (toast != null) {
                                    toast.cancel();
                                }
                            }
                        }
                        return false;
                    }
                });
            }
        } else {
            super.setOnClickListener(onClickListener);
        }
    }

//    @Override
//    public boolean performClick() {
//        if (preventAccidentalClicks) {
//            if (onClickListener == null) {
//                return false;
//            } else {
//                onClickListener.onClick(this);
//                return true;
//            }
//        } else {
//            return super.performClick();
//        }
//    }

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
                allowRemoteControlWhenNotShown = typedArray.getBoolean(R.styleable.MyButton_allow_remote_control_when_not_shown, false);
                preventAccidentalClicks = typedArray.getBoolean(R.styleable.MyButton_prevent_accidental_clicks, true);
            } finally {
                typedArray.recycle();
            }
        } else {
            group = Group.OTHER;
            allowRemoteControlWhenNotShown = false;
            preventAccidentalClicks = true;
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
