package digital.tonima.bibliadigital.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.VERSIONS_TABLE

@Entity(tableName = VERSIONS_TABLE)
data class Version(
    @PrimaryKey
    @SerializedName("code")
    val code: String,
    @SerializedName("copyright")
    val copyright: String,
    @SerializedName("permissions")
    val permissions: String,
    @SerializedName("language")
    val language: String,
)
