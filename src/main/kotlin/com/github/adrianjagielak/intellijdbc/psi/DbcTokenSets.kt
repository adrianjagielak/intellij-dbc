package com.github.adrianjagielak.intellijdbc.psi

import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes

object DbcTokenSets {
    @JvmField val COMMENTS = DbcTokenTypes.COMMENTS
    @JvmField val WHITE_SPACES = DbcTokenTypes.WHITE_SPACES
    @JvmField val STRINGS = DbcTokenTypes.STRINGS
}
