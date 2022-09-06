package com.mccarty.ritmo.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.codelab.android.datastore.AlbumPreference
import com.codelab.android.datastore.AlbumPreference.getDefaultInstance
import com.google.protobuf.InvalidProtocolBufferException

import java.io.InputStream
import java.io.OutputStream

object AlbumPreferenceSerializer : Serializer<AlbumPreference> {
    override suspend fun readFrom(input: InputStream): AlbumPreference {
        try {
            return AlbumPreference.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AlbumPreference, output: OutputStream) = t.writeTo(output)
    override val defaultValue: AlbumPreference
        get() = getDefaultInstance()

}