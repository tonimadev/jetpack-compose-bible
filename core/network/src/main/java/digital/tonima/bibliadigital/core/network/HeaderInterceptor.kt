package digital.tonima.bibliadigital.core.network

import digital.tonima.bibliadigital.core.common.constants.PLATFORM
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.run {
            proceed(
                request()
                    .newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("platform", PLATFORM)
                    .addHeader("app_version", BuildConfig.VERSION_NAME)
                    .addHeader("Access-Control-Allow-Origin", "*")
                    .addHeader("Connection", "Keep-Alive")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer ${BuildConfig.BIBLIA_DIGITAL_TOKEN}")
                    .build(),
            )
        }
}
