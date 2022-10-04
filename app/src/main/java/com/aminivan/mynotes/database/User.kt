package com.aminivan.mynotes.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @ColumnInfo(name = "name")
    var name: String? = null,
    @ColumnInfo(name = "email")
    var email: String? = null,
    @ColumnInfo(name = "password")
    var password: String? = null,
    @ColumnInfo(name = "profile")
    var profile: String? = null,
    @ColumnInfo(name = "jk")
    var jk: String? = null,
) : Parcelable