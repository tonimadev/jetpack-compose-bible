package digital.tonima.bibliadigital.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import digital.tonima.bibliadigital.data.local.room.ChurchDatabase.Companion.ABBREVS_TABLE

@Entity(tableName = ABBREVS_TABLE)
data class AbbrevRoomModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bookName: String = "",
    val abbrev: String = "",
)
