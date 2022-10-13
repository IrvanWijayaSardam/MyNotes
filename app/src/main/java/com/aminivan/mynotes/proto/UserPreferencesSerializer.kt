package com.aminivan.mynotes.proto

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.aminivan.mynotes.UserDataProto
import java.io.InputStream
import java.io.OutputStream

object UserPreferencesSerializer : Serializer<UserDataProto> {
    override val defaultValue: UserDataProto
        get() = UserDataProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserDataProto {
        try {
            return UserDataProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: UserDataProto, output: OutputStream) = t.writeTo(output)

}