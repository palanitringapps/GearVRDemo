package tringapps.com.gearvrdemoifg

import android.app.Application


class IFGApplication : Application(){

    override fun onCreate() {
        super.onCreate()
       // BackgroundManager.get(this).registerListener(this)
        SocketIO.instance.connect()
    }
}
