package top.niunaijun.blackboxa.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import top.niunaijun.blackbox.BlackBoxCore


class App : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var mContext: Context

        @JvmStatic
        fun getContext(): Context {
            return mContext
        }
    }

    override fun attachBaseContext(base: Context?) {
        try {
            super.attachBaseContext(base)

            try {
                BlackBoxCore.get().closeCodeInit()
            } catch (e: Exception) {
                Log.e("App", "Error in closeCodeInit: ${e.message}")
            }

            try {
                BlackBoxCore.get().onBeforeMainApplicationAttach(this, base)
            } catch (e: Exception) {
                Log.e("App", "Error in onBeforeMainApplicationAttach: ${e.message}")
            }

            mContext = base!!

            try {
                AppManager.doAttachBaseContext(base)
            } catch (e: Exception) {
                Log.e("App", "Error in doAttachBaseContext: ${e.message}")
            }

            try {
                BlackBoxCore.get().onAfterMainApplicationAttach(this, base)
            } catch (e: Exception) {
                Log.e("App", "Error in onAfterMainApplicationAttach: ${e.message}")
            }

        } catch (e: Exception) {
            Log.e("App", "Critical error in attachBaseContext: ${e.message}")

            if (base != null) {
                mContext = base
            }
        }
    }

    override fun onCreate() {
        try {
            super.onCreate()

            AppManager.doOnCreate(mContext)

            /*
             * BlackBox açıldığında User 0 içine
             * Google Play Services / GMS kurulumunu otomatik dene.
             */
            Thread {
                try {
                    Thread.sleep(1500)

                    val core = BlackBoxCore.get()

                    if (!core.isInstallGms(0)) {

                        Log.d("BlackBoxGMS", "GMS User 0 için kuruluyor...")

                        val result = core.installGms(0)

                        if (result.success) {
                            Log.d(
                                "BlackBoxGMS",
                                "GMS User 0 için başarıyla kuruldu."
                            )
                        } else {
                            Log.e(
                                "BlackBoxGMS",
                                "GMS kurulamadı: ${result.msg}"
                            )
                        }

                    } else {

                        Log.d(
                            "BlackBoxGMS",
                            "GMS User 0 içinde zaten kurulu."
                        )
                    }

                } catch (e: Exception) {
                    Log.e(
                        "BlackBoxGMS",
                        "GMS otomatik kurulum hatası: ${e.message}",
                        e
                    )
                }
            }.start()

        } catch (e: Exception) {
            Log.e("App", "Error in onCreate: ${e.message}")
        }
    }
}
