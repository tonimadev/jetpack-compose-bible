package digital.tonima.bibliadigital.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Abbrev(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("pt")
    val pt: String = "",
) : Parcelable
