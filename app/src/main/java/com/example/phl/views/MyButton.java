package com.example.phl.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

import com.example.phl.R;

public class MyButton extends AppCompatButton {

    private Group group;

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
            } finally {
                typedArray.recycle();
            }
        } else {
            group = Group.OTHER;
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
    }

}
