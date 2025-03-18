package com.erkankaplan.isserveronline.helpers;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

import com.erkankaplan.isserveronline.R;
import com.erkankaplan.isserveronline.databinding.LoadingDialogBinding;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;


public class LoadingDialog {

    private LoadingDialogBinding loadingDialogBinding;

    private Dialog dialog;


    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }



    public void dismiss() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception ex) {
            Log.e("LoadingDialog", "dismiss deyiz: ", ex);
        }
    }


    public void showLoading(Context context, String message) {
        loadingDialogBinding = LoadingDialogBinding.inflate(LayoutInflater.from(context));

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        try {

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            // ViewBinding kullanarak içeriği ayarla
            dialog.setContentView(R.layout.loading_dialog);

            dialog.setCancelable(false);

            if (message != null) {
                loadingDialogBinding.txtText.setText(message);
            } else {
                loadingDialogBinding.txtText.setText("");
            }

            if (!dialog.isShowing()) {
                dialog.show();
            }

        } catch (Exception ex) {
            Log.e("LoadingDialog", "showLoading deyiz: "+ ex.getMessage());

        }
    }

    public void checkServerStatus(ServerStatusCallback callback) {

        int port = 1454;
        String host = "192.168.1.125";


        new Thread(() -> {
            boolean isOnline = false;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 5000); // 5 saniye zaman aşımı
                isOnline = true;
            } catch (IOException e) {
                isOnline = false;
            }
            boolean finalIsOnline = isOnline;
            new Handler(Looper.getMainLooper()).post(() -> callback.onServerStatusChecked(finalIsOnline));
        }).start();
    }







}
