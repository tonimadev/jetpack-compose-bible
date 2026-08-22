package digital.tonima.bibliadigital.core.common.model

import com.google.gson.annotations.SerializedName

data class Abbrev(
    @SerializedName("pt")
    val pt: String = "",
    @SerializedName("en")
    val en: String = "",
)
