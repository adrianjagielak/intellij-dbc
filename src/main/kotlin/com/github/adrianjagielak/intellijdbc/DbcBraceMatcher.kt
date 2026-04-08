package com.github.adrianjagielak.intellijdbc

import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class DbcBraceMatcher : PairedBraceMatcher {
    companion object {
        private val BRACE_PAIRS = arrayOf(
            BracePair(DbcTokenTypes.LPAREN, DbcTokenTypes.RPAREN, false),
            BracePair(DbcTokenTypes.LBRACKET, DbcTokenTypes.RBRACKET, false),
        )
    }

    override fun getPairs(): Array<BracePair> = BRACE_PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = openingBraceOffset
}
