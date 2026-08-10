package digital.tonima.bibliadigital.domain.model

import com.google.gson.annotations.SerializedName

data class Version(
    @SerializedName("code")
    val code: String,
    @SerializedName("copyright")
    val copyright: String,
    @SerializedName("permissions")
    val permissions: String,
    @SerializedName("language")
    val language: String,
)
