package com.github.adrianjagielak.intellijdbc

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class DbcFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, DbcLanguage.INSTANCE) {
    override fun getFileType(): FileType = DbcFileType.INSTANCE

    override fun toString(): String = "DBC File"
}
