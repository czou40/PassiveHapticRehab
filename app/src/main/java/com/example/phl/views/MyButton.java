package com.example.phl.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.Toast;

import com.shashank.sony.fancytoastlib.FancyToast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.example.phl.R;
import com.example.phl.services.RemoteControlService;
import com.google.android.material.button.MaterialButton;

import java.util.HashSet;
import java.util.Set;

public class MyButton extends MaterialButton {

    private Group group;

    private boolean allowRemoteControlWhenNotShown = false;

    private boolean isReceiverRegistered = false;

    private boolean preventAccidentalClicks = false;

    private OnClickListener onClickListener;

    private static Set<MyButton> buttonsBeingTouched = new HashSet<>();

    private static int longPressDuration;

    private String command;

    private static long firstTime = 0;
    private static long secondTime = 0;

    private static CountDownTimer countDownTimer;

    private static Toast toast;

    private static boolean isBadTouch = false;

    private BroadcastReceiver remoteControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra(RemoteControlService.COMMAND);
            if (command != null) {
                Group commandGroup = Group.fromString(command);
                if (command.trim().equalsIgnoreCase(MyButton.this.getText().toString().trim())) {
                    if ((allowRemoteControlWhenNotShown || isShown()) && isEnabled()) {
                        if (preventAccidentalClicks) {
                            if (onClickListener != null) {
                                onClickListener.onClick(MyButton.this);
                            }
                        } else {
                            abortBroadcast();
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
                            abortBroadcast();
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
        Log.d("MyButton", "preventAccidentalClicks: " + preventAccidentalClicks);
        if (preventAccidentalClicks) {
            if (onClickListener == null) {
                super.setOnTouchListener(null);
            } else {
                super.setOnTouchListener(new OnTouchListener() {

                    private boolean isInside(View v, MotionEvent e) {
                        return !(e.getX() < 0 || e.getY() < 0
                                || e.getX() > v.getMeasuredWidth()
                                || e.getY() > v.getMeasuredHeight());
                    }

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            buttonsBeingTouched.add((MyButton) view);
                        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            buttonsBeingTouched.remove((MyButton) view);
                        }
                        int numFingers = motionEvent.getPointerCount();
                        if (numFingers > 1) {
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            if (!isBadTouch) {
                                if (toast != null) {
                                    toast.cancel();
                                }
                                toast = FancyToast.makeText(getContext(), "You can only use one finger to press the button.", FancyToast.LENGTH_SHORT, FancyToast.INFO, false);
                                toast.setGravity(Gravity.TOP, 0, 0);
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
                                toast = FancyToast.makeText(getContext(), "You canceled the pressing by moving outside of the button", FancyToast.LENGTH_SHORT, FancyToast.INFO, false);
                                toast.setGravity(Gravity.TOP, 0, 0);
                                toast.show();
                                isBadTouch = true;
                            }
                            return false;
                        }
                        if (buttonsBeingTouched.size() >= 2) {
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            if (!isBadTouch) {
                                if (toast != null) {
                                    toast.cancel();
                                }
                                toast = FancyToast.makeText(getContext(), "You can press only one button at a time.", FancyToast.LENGTH_SHORT, FancyToast.INFO, false);
                                toast.setGravity(Gravity.TOP, 0, 0);
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
                            countDownTimer = new CountDownTimer(longPressDuration, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    if (toast != null) {
                                        toast.cancel();
                                    }
                                    toast = FancyToast.makeText(getContext(), "Press the button for " + (int) Math.ceil(millisUntilFinished / 1000.0) + " more second(s) to confirm.", FancyToast.LENGTH_SHORT, FancyToast.INFO, false);
                                    toast.setGravity(Gravity.TOP, 0, 0);
                                    toast.show();
                                }

                                @Override
                                public void onFinish() {
                                    if (toast != null) {
                                        toast.cancel();
                                    }
                                    toast = FancyToast.makeText(getContext(), "Release the button to confirm.", FancyToast.LENGTH_SHORT, FancyToast.INFO, false);
                                    toast.setGravity(Gravity.TOP, 0, 0);
                                    toast.show();
                                }
                            };
                            countDownTimer.start();
                        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            if (isBadTouch) {
                                return false;
                            }
                            secondTime = System.currentTimeMillis();
                            if (secondTime - firstTime > longPressDuration) {
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
            Log.d("MyButton", "Setting onClickListener without preventing accidental clicks");
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
            if (getText() != null && !getText().toString().trim().isEmpty()) {
                command = getText().toString().trim();
                RemoteControlService.notifyCommand(getContext(), command);
            }
            Log.d("MyButton", "Registered receiver");
        }
    }

    private void unregisterReceiverIfRegistered() {
        if (isReceiverRegistered) {
            getContext().unregisterReceiver(remoteControlReceiver);
            isReceiverRegistered = false;
            if (command != null) {
                RemoteControlService.notifyCommandRemoval(getContext(), command);
            }
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean globalPreventAccidentalTouchesSetting = sharedPreferences.getBoolean("prevent_accidental_touches", true);
        longPressDuration = sharedPreferences.getInt("button_hold_time", 2000);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyButton);
            try {
                int groupValue = typedArray.getInt(R.styleable.MyButton_group, Group.OTHER.getValue());
                group = Group.fromInt(groupValue);
                allowRemoteControlWhenNotShown = typedArray.getBoolean(R.styleable.MyButton_allow_remote_control_when_not_shown, false);
                preventAccidentalClicks = typedArray.getBoolean(R.styleable.MyButton_prevent_accidental_clicks, true) && globalPreventAccidentalTouchesSetting;
            } finally {
                typedArray.recycle();
            }
        } else {
            group = Group.OTHER;
            allowRemoteControlWhenNotShown = false;
            preventAccidentalClicks = true && globalPreventAccidentalTouchesSetting;
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
