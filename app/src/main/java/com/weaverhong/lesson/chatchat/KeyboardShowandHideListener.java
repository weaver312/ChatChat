package com.weaverhong.lesson.chatchat;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * 学习了一下，引用了csdn找到的一个类，做了一点适配修改。
 * 因为google没有对键盘弹出收回的监听类，只好自己写个，简单说就是通过监听activity尺寸大小来进行通知
 * 需要注意的是，这个对全屏应用不太好用，因为全屏状态Activity大小固定。
 * 全屏的处理方法，我觉得可以参考bilibili全屏播放，需要发弹幕的情况。它在屏幕上自己绘制了一个小号的键盘。
 * 引用说明：
 * ------------------------------------
 * 来源：
 * 作者：paleful
 * 来源：CSDN
 * 原文：https://blog.csdn.net/u011181222/article/details/52043001
 * 版权声明：本文为博主原创文章，转载请附上博文链接！
 * Created by liujinhua on 15/10/25.
 * ------------------------------------
 */
public class KeyboardShowandHideListener {
    private View rootView;//activity的根视图
    int rootViewVisibleHeight;//纪录根视图的显示高度
    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;

    public KeyboardShowandHideListener(Activity activity) {
        // 获取activity的根视图
        rootView = activity.getWindow().getDecorView();

        // 监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 获取当前根视图在屏幕上显示的大小
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int visibleHeight = r.height();
                System.out.println("" + visibleHeight);
                if (rootViewVisibleHeight == 0) {
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                // 根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
                if (rootViewVisibleHeight == visibleHeight) {
                    return;
                }

                // 根视图显示高度变小超过200，可以看作软键盘显示了
                if (rootViewVisibleHeight - visibleHeight > 200) {
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardShow(rootViewVisibleHeight - visibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                // 根视图显示高度变大超过200，可以看作软键盘隐藏了
                if (visibleHeight - rootViewVisibleHeight > 200) {
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardHide(visibleHeight - rootViewVisibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

            }
        });
    }

    private void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
    }

    public interface OnSoftKeyBoardChangeListener {
        void keyBoardShow(int height);

        void keyBoardHide(int height);
    }

    public static void setListener(Activity activity, OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        KeyboardShowandHideListener softKeyBoardListener = new KeyboardShowandHideListener(activity);
        softKeyBoardListener.setOnSoftKeyBoardChangeListener(onSoftKeyBoardChangeListener);
    }
}