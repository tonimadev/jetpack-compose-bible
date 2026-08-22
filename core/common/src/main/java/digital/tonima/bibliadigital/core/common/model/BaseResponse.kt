package digital.tonima.bibliadigital.core.common.model

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("data")
    val data: T,
)
