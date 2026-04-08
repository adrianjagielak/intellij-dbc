package com.github.adrianjagielak.intellijdbc

import com.intellij.lang.Language

class DbcLanguage private constructor() : Language("DBC") {
    companion object {
        @JvmField
        val INSTANCE = DbcLanguage()
    }

    override fun getDisplayName(): String = "DBC (CAN Database)"

    override fun isCaseSensitive(): Boolean = true
}
