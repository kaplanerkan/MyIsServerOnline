package com.erkankaplan.isserveronline;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.erkankaplan.isserveronline.databinding.ActivityMainBinding;
import com.erkankaplan.isserveronline.helpers.LoadingDialog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

    }

    private void initViews() {

        Toasty.Config.getInstance().tintIcon(true) // optional (apply textColor also to the icon)
                .setTextSize(18) // optional
                .allowQueue(true) // optional (prevents several Toastys from queuing)
                .setGravity(Gravity.CENTER) // optional (set toast gravity, offsets are optional)
                .apply(); // required


        binding.btnConnect.setOnClickListener(v -> {
            // 1. Yöntem
            // Kullanımı

            isServerOnline(isOnline -> {
                if (isOnline) {
                    runOnUiThread(() -> Toasty.success(MainActivity.this, getString(R.string.server_is_online), Toast.LENGTH_SHORT, true).show());
                } else {
                    runOnUiThread(() -> Toasty.error(MainActivity.this, getString(R.string.server_is_offline), Toast.LENGTH_SHORT, true).show());
                }
            });

        });


        binding.btnConnect2.setOnClickListener(v -> {
            // 2. Yöntem
            // Kullanımı
            isServerOnline2().thenAccept(isOnline -> {
                if (isOnline) {
                    runOnUiThread(() -> Toasty.success(MainActivity.this, getString(R.string.server_is_online), Toast.LENGTH_SHORT, true).show());
                } else {
                    runOnUiThread(() -> Toasty.error(MainActivity.this, getString(R.string.server_is_offline), Toast.LENGTH_SHORT, true).show());
                }
            }).exceptionally(ex -> {
                // Hata durumu
                String message = getString(R.string.server_is_offline) + " \n" + ex.getMessage();
                runOnUiThread(() -> Toasty.info(MainActivity.this, message, Toast.LENGTH_LONG, true).show());

                return null;
            });
        });


        binding.btnConnect3.setOnClickListener(v ->
                checkServerStatus("192.168.1.125", 1454).thenAccept(isOnline -> {


                    if (isOnline) {
                        // Sunucu çevrimiçi
                        Log.d("ServerStatus", "Sunucu çevrimiçi");
                        runOnUiThread(() -> Toasty.success(MainActivity.this, getString(R.string.server_is_online), Toast.LENGTH_SHORT, true).show());
                    } else {
                        // Sunucu çevrimdışı
                        Log.d("ServerStatus", "Sunucu çevrimdışı");
                        runOnUiThread(() -> Toasty.error(MainActivity.this, getString(R.string.server_is_offline), Toast.LENGTH_SHORT, true).show());
                    }
                }).exceptionally(ex -> {
                    // Hata durumu
                    if (ex.getCause() instanceof IOException) {
                        String errorMessage = ex.getCause().getMessage();
                        Log.e("ServerStatus", "Hata oluştu: " + errorMessage);

                        String message = "Hata olustu: \n" + errorMessage;
                        runOnUiThread(() -> Toasty.error(MainActivity.this, message, Toast.LENGTH_LONG, true).show());
                    } else {
                        Log.e("ServerStatus", "Bilinmeyen hata oluştu", ex);
                        String message = "Bilinmeyen hata oluştu \n" + ex.getMessage();
                        runOnUiThread(() -> Toasty.error(MainActivity.this, message, Toast.LENGTH_LONG, true).show());
                    }
                    return null;
                }));


    }


    /**
     * 1. Yöntem  ::   Callback Kullanma
     * Bir callback (geri çağırma) yöntemi ekleyerek, sunucu durumunu kontrol ettikten sonra sonucu dönebilirsiniz.
     */

    private void isServerOnline(Consumer<Boolean> callback) {
        AtomicBoolean serverStatus = new AtomicBoolean(false);

        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.showLoading(MainActivity.this, getString(R.string.checking_server));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Thread.sleep(1000);
                loadingDialog.checkServerStatus(isOnline -> {
                    Log.e("isServerOnline", "isServerOnline :: " + isOnline);
                    serverStatus.set(isOnline);
                    // Callback ile sonucu döndür
                    callback.accept(isOnline);
                });
            } catch (Exception ex1) {
                serverStatus.set(false);
                // Hata durumunda callback ile false döndür
                callback.accept(false);
            } finally {
                runOnUiThread(loadingDialog::dismiss);
            }
        }, 1000);
    }


    /**
     * 2. Yöntem :: CompletableFuture Kullanma
     * Java 8 ile gelen CompletableFuture kullanarak asenkron işlemleri daha mantıklı bir şekilde yönetebilirsiniz.
     * <p>
     * Java
     *
     * @return
     */


    private CompletableFuture<Boolean> isServerOnline2() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.showLoading(MainActivity.this, getString(R.string.checking_server));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Thread.sleep(1000);
                loadingDialog.checkServerStatus(isOnline -> {
                    Log.e("isServerOnline", "isServerOnline :: " + isOnline);
                    future.complete(isOnline);
                });
            } catch (Exception ex1) {
                future.completeExceptionally(ex1);
            } finally {
                runOnUiThread(loadingDialog::dismiss);
            }
        }, 1000);

        return future;
    }



    /**
     * 3. Yöntem :: CompletableFuture Kullanma
     * CompletableFuture kullanarak sunucu durumunu kontrol etme
     * @param host
     * @param port
     * @return

    Android uygulamanızda ana iş parçacığında (main thread) ağ işlemi yürütmeye çalıştığınızda ortaya çıkar.
    Android, kullanıcı arayüzünü (UI) bloklamamak için ağ işlemlerinin ana iş parçacığında gerçekleştirilmesine izin vermez.
    Bu sorunu çözmek için ağ işlemlerini arka plan iş parçacığında (background thread) gerçekleştirmeniz gerekir.

    Bu durumda, Socket bağlantısını bir arka plan iş parçacığında gerçekleştirebilirsiniz.
    AsyncTask veya Thread kullanarak bu işlemi yapabilirsiniz, ancak günümüzde AsyncTask kullanımı önerilmemektedir.
    Bunun yerine ExecutorService ve Runnable kullanarak işlemi gerçekleştirebilirsiniz.

     */

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private CompletableFuture<Boolean> checkServerStatus(String host, int port) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.showLoading(MainActivity.this, "Checking Server");

        // DEPRACED:
        // Calismaz cunku Main Thread da ag islemleri yapilamaz

//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            try (Socket socket = new Socket()) {
//                socket.connect(new InetSocketAddress(host, port), 5000); // 5 saniye zaman aşımı
//                future.complete(true);
//            } catch (IOException e) {
//                future.completeExceptionally(e);
//            } finally {
//                runOnUiThread(loadingDialog::dismiss);
//            }
//        }, 1000);



        /*  Main Thread da ag islemleri yapilamaz
         *  Bu yuzden ExecutorService kullanarak arka planda calistirilmasi gerekmektedir
         */

        executorService.execute(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 5000); // 5 saniye zaman aşımı
                future.complete(true);
            } catch (IOException e) {
                future.completeExceptionally(e);
            } finally {
                new Handler(Looper.getMainLooper()).post(loadingDialog::dismiss);
            }
        });



        return future;
    }


}