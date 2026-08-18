package digital.tonima.bibliadigital.domain.core.plataform

import android.content.Context
import android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.tonima.bibliadigital.domain.core.computation.NetworkConnectivity
import digital.tonima.bibliadigital.domain.core.extension.connectivityManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkHandler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : NetworkConnectivity {
        fun isNetworkAvailable(): Boolean {
            val connectivityManager = context.connectivityManager

            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(TRANSPORT_ETHERNET) -> true
                activeNetwork.hasTransport(TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        }
    }
