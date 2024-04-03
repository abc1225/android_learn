package com.example.myapplication;


import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoshuyu on 2017/6/16.
 */

public class CustomActionWebView extends WebView {

    static String TAG = "CustomActionWebView";

    ActionMode mActionMode;

    List<String> mActionList = new ArrayList<>();

    ActionSelectListener mActionSelectListener;

    public interface ActionSelectListener {
        void onClick(String title, String selectText, String seq);
    }

    public CustomActionWebView(Context context) {
        super(context);
    }

    public CustomActionWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomActionWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 处理item，处理点击
     * @param actionMode
     */
    private ActionMode resolveActionMode(ActionMode actionMode) {
        if (actionMode != null) {
            final Menu menu = actionMode.getMenu();
            mActionMode = actionMode;
            menu.clear();
            for (int i = 0; i < mActionList.size(); i++) {
                menu.add(mActionList.get(i));
            }
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        getSelectedData((String) item.getTitle());
                        releaseAction();
                        return true;
                    }
                });
            }
        }
        mActionMode = actionMode;
        return actionMode;
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        ActionMode actionMode = super.startActionMode(callback);
        return resolveActionMode(actionMode);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        ActionMode actionMode = super.startActionMode(callback, type);
        return resolveActionMode(actionMode);
    }

    private void releaseAction() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    /**
     * 点击的时候，获取网页中选择的文本，回掉到原生中的js接口
     * @param title 传入点击的item文本，一起通过js返回给原生接口
     */
    private void getSelectedData(String title) {

        Log.d("sloth", "click data: " + title);

        String js = "(function getSelectedText() {" +
                "var txt;" +
                "var seq = 0;" +
                "var title = \"" + title + "\";" +
                "if (window.getSelection) {" +
                "seq = 1;" +
                "txt = window.getSelection().toString();" +

                "var range = window.getSelection().getRangeAt(0);" +
                "var selectedText = range.extractContents();" +
                "var span = document.createElement('span');" +
                "span.style.color = 'red';" +
                "span.appendChild(selectedText);" +
                "range.insertNode(span);" +

                "} else if (window.document.getSelection) {" +
                "seq = 2;" +
                "txt = window.document.getSelection().toString();" +

                "var range = window.document.getSelection().getRangeAt(0);" +
                "var selectedText = range.extractContents();" +
                "var span = document.createElement('span');" +
                "span.style.color = 'red';" +
                "span.appendChild(selectedText);" +
                "range.insertNode(span);" +

                "} else if (window.document.selection) {" +
                "seq = 3;" +
                "txt = window.document.selection.createRange().text;" +

                "var range = window.document.selection.getRangeAt(0);" +
                "var selectedText = range.extractContents();" +
                "var span = document.createElement('span');" +
                "span.style.color = 'red';" +
                "span.appendChild(selectedText);" +
                "range.insertNode(span);" +

                "}" +
                "JSInterface.callback(txt,title,seq);" +
                "})()";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript("javascript:" + js, null);
        } else {
            loadUrl("javascript:" + js);
        }
    }

    public void linkJSInterface() {
        addJavascriptInterface(new ActionSelectInterface(this), "JSInterface");
    }

    /**
     * 设置弹出action列表
     * @param actionList
     */
    public void setActionList(List<String> actionList) {
        mActionList = actionList;
    }

    /**
     * 设置点击回掉
     * @param actionSelectListener
     */
    public void setActionSelectListener(ActionSelectListener actionSelectListener) {
        this.mActionSelectListener = actionSelectListener;
    }

    /**
     * 隐藏消失Action
     */
    public void dismissAction() {
        releaseAction();
    }


    /**
     * js选中的回掉接口
     */
    private class ActionSelectInterface {

        CustomActionWebView mContext;

        ActionSelectInterface(CustomActionWebView c) {
            mContext = c;
        }

        @JavascriptInterface
        public void callback(final String value, final String title, final String seq) {
            if(mActionSelectListener != null) {
                mActionSelectListener.onClick(title, value, seq);
            }
        }
    }
}
