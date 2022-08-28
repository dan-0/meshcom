package me.danlowe.meshcommunicator.features.db.conversations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "userName")
    val userName: String,
    @ColumnInfo(name = "userId")
    val userId: String,
)