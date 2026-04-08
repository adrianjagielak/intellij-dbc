package com.github.adrianjagielak.intellijdbc.reference

import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.github.adrianjagielak.intellijdbc.lexer.DbcLexer

class DbcFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            DbcLexer(),
            TokenSet.create(DbcTokenTypes.IDENTIFIER),
            DbcTokenTypes.COMMENTS,
            DbcTokenTypes.STRINGS
        )
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is DbcNamedElement
    }

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String {
        return when (element) {
            is DbcMessageDef -> "message"
            is DbcSignalDef -> "signal"
            is DbcNodeName -> "node"
            is DbcValueTableDef -> "value table"
            is DbcEnvironmentVariableDef -> "environment variable"
            else -> "element"
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return when (element) {
            is DbcMessageDef -> {
                val id = element.messageId?.let { "0x%X".format(it) } ?: "?"
                "${element.messageName ?: "?"} [$id]"
            }
            is DbcSignalDef -> element.signalName ?: "?"
            is DbcNodeName -> element.name ?: "?"
            is DbcValueTableDef -> element.tableName ?: "?"
            is DbcEnvironmentVariableDef -> element.envVarName ?: "?"
            else -> element.text ?: "?"
        }
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return getDescriptiveName(element)
    }
}
