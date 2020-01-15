package com.example.mapapp;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class MyProgressDialog extends DialogFragment {
    public static MyProgressDialog newInstance(){
        return new MyProgressDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_progress);
        dialog.setCancelable(false);
        dialog.setTitle("ロード中...");
        return dialog;
    }
}
