package org.example.openaitts.feature.conversation.domain.utils

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
internal fun String.decode(): ByteArray = Base64.decode(this)

@OptIn(ExperimentalEncodingApi::class)
internal fun ByteArray.encode(): String = Base64.encode(this)