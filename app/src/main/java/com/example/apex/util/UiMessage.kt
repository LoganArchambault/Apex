package com.example.apex.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

data class UiMessage(@param:StringRes val resource: Int, val arguments: List<Any> = emptyList()) {
    @Composable
    fun asString(): String = stringResource(resource, *arguments.toTypedArray())
}

class ResourceException(val uiMessage: UiMessage, cause: Throwable? = null) :
    RuntimeException(cause)

fun Throwable.toUiMessage(@StringRes fallback: Int): UiMessage =
    (this as? ResourceException)?.uiMessage ?: UiMessage(fallback)
