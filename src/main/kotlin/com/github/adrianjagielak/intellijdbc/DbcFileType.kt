package com.github.adrianjagielak.intellijdbc

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class DbcFileType private constructor() : LanguageFileType(DbcLanguage.INSTANCE) {
    companion object {
        @JvmField
        val INSTANCE = DbcFileType()
    }

    override fun getName(): String = "DBC"

    override fun getDescription(): String = "CAN database definition file"

    override fun getDefaultExtension(): String = "dbc"

    override fun getIcon(): Icon = DbcIcons.FILE
}
