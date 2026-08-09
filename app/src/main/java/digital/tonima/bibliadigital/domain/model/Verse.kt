package digital.tonima.bibliadigital.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Verse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("number")
    val number: Int = 0,
    @SerializedName("text")
    val text: String = "",
)
