package com.ys.model.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ys.model.R;
import com.ys.model.listener.EditTextDialogListener;
import com.ys.model.util.KeyBoardUtil;

/***
 * 带2个输入框的
 */
public class EditTextDoubleDialog {

    private Context context;
    private Dialog dialog;
    public Button btn_modify, btn_del_all, btn_hiddle;
    EditText et_username_edit;

    public EditTextDoubleDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context, R.style.MyDialog_Base);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialog_view = View.inflate(context, R.layout.dialog_edit_commit, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(700, 700);
        dialog.setContentView(dialog_view, params);
        initView(dialog_view);
    }

    private void initView(View dialog_view) {
        btn_hiddle = (Button) dialog_view.findViewById(R.id.btn_hiddle);
        btn_modify = (Button) dialog_view.findViewById(R.id.btn_modify);
        et_username_edit = (EditText) dialog_view.findViewById(R.id.et_username_edit);
        btn_del_all = (Button) dialog_view.findViewById(R.id.btn_del_all);

        btn_del_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                et_username_edit.setText("");
            }
        });

        btn_hiddle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dissmiss();
            }
        });

        btn_modify.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                KeyBoardUtil.hiddleBord(btn_modify);
                dissmiss();
            }
        });

        et_username_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    KeyBoardUtil.showKeyBord(view);
                } else {
                    KeyBoardUtil.hiddleBord(view);
                }
            }
        });

        dialog_view.findViewById(R.id.rela_bgg).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyBoardUtil.hiddleBord(view);
            }
        });
    }

    public void show(String content, String commit) {
        try {
            et_username_edit.setText(content);
            btn_modify.setText(commit);
            et_username_edit.requestFocus();
            dialog.show();
            handler.sendEmptyMessageDelayed(MESSAGE_DISSMISS_DIALOG_AUTO, 120 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static final int MESSAGE_DISSMISS_DIALOG_AUTO = 568945;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            handler.removeMessages(msg.what);
            switch (msg.what) {
                case MESSAGE_DISSMISS_DIALOG_AUTO:
                    dissmiss();
                    break;
            }
        }
    };

    public void dissmiss() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (handler != null) {
                handler.removeMessages(MESSAGE_DISSMISS_DIALOG_AUTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
