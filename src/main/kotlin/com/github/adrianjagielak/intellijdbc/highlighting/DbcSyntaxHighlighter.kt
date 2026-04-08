package com.github.adrianjagielak.intellijdbc.highlighting

import com.github.adrianjagielak.intellijdbc.lexer.DbcLexer
import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class DbcSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        @JvmField
        val KEYWORD = createTextAttributesKey("DBC_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        @JvmField
        val SECTION_KEYWORD = createTextAttributesKey("DBC_SECTION_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        @JvmField
        val STRING = createTextAttributesKey("DBC_STRING", DefaultLanguageHighlighterColors.STRING)
        @JvmField
        val NUMBER = createTextAttributesKey("DBC_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        @JvmField
        val LINE_COMMENT = createTextAttributesKey("DBC_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        @JvmField
        val IDENTIFIER = createTextAttributesKey("DBC_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
        @JvmField
        val SEMICOLON = createTextAttributesKey("DBC_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)
        @JvmField
        val COMMA = createTextAttributesKey("DBC_COMMA", DefaultLanguageHighlighterColors.COMMA)
        @JvmField
        val COLON = createTextAttributesKey("DBC_COLON", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        @JvmField
        val PARENTHESES = createTextAttributesKey("DBC_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        @JvmField
        val BRACKETS = createTextAttributesKey("DBC_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        @JvmField
        val OPERATOR = createTextAttributesKey("DBC_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        @JvmField
        val MUX_INDICATOR = createTextAttributesKey("DBC_MUX_INDICATOR", DefaultLanguageHighlighterColors.METADATA)
        @JvmField
        val BAD_CHARACTER = createTextAttributesKey("DBC_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val SECTION_KEYWORD_KEYS = arrayOf(SECTION_KEYWORD)
        private val STRING_KEYS = arrayOf(STRING)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val COMMENT_KEYS = arrayOf(LINE_COMMENT)
        private val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        private val SEMICOLON_KEYS = arrayOf(SEMICOLON)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val COLON_KEYS = arrayOf(COLON)
        private val PAREN_KEYS = arrayOf(PARENTHESES)
        private val BRACKET_KEYS = arrayOf(BRACKETS)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val MUX_KEYS = arrayOf(MUX_INDICATOR)
        private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer = DbcLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            // Section-level keywords get a distinct style
            DbcTokenTypes.VERSION, DbcTokenTypes.NS_, DbcTokenTypes.BS_,
            DbcTokenTypes.BU_, DbcTokenTypes.BO_, DbcTokenTypes.CM_,
            DbcTokenTypes.BA_DEF_, DbcTokenTypes.BA_DEF_DEF_, DbcTokenTypes.BA_,
            DbcTokenTypes.VAL_, DbcTokenTypes.VAL_TABLE_, DbcTokenTypes.SIG_GROUP_,
            DbcTokenTypes.BO_TX_BU_, DbcTokenTypes.EV_, DbcTokenTypes.SG_MUL_VAL_,
            DbcTokenTypes.SGTYPE_, DbcTokenTypes.SIG_TYPE_REF_,
            DbcTokenTypes.BA_REL_, DbcTokenTypes.BA_DEF_REL_,
            DbcTokenTypes.BA_SGTYPE_, DbcTokenTypes.SIG_VALTYPE_,
            DbcTokenTypes.ENVVAR_DATA_, DbcTokenTypes.SGTYPE_VAL_ -> SECTION_KEYWORD_KEYS

            DbcTokenTypes.SG_ -> KEYWORD_KEYS

            // Type keywords
            DbcTokenTypes.INT_KEYWORD, DbcTokenTypes.FLOAT_KEYWORD,
            DbcTokenTypes.STRING_KEYWORD, DbcTokenTypes.HEX_KEYWORD,
            DbcTokenTypes.ENUM_KEYWORD -> KEYWORD_KEYS

            DbcTokenTypes.STRING -> STRING_KEYS
            DbcTokenTypes.INTEGER, DbcTokenTypes.FLOAT -> NUMBER_KEYS
            DbcTokenTypes.LINE_COMMENT -> COMMENT_KEYS
            DbcTokenTypes.IDENTIFIER -> IDENTIFIER_KEYS
            DbcTokenTypes.SEMICOLON -> SEMICOLON_KEYS
            DbcTokenTypes.COMMA -> COMMA_KEYS
            DbcTokenTypes.COLON -> COLON_KEYS
            DbcTokenTypes.LPAREN, DbcTokenTypes.RPAREN -> PAREN_KEYS
            DbcTokenTypes.LBRACKET, DbcTokenTypes.RBRACKET -> BRACKET_KEYS
            DbcTokenTypes.PIPE, DbcTokenTypes.AT, DbcTokenTypes.PLUS, DbcTokenTypes.MINUS -> OPERATOR_KEYS
            DbcTokenTypes.MUX_INDICATOR -> MUX_KEYS
            DbcTokenTypes.BAD_CHARACTER -> BAD_CHAR_KEYS
            else -> EMPTY_KEYS
        }
    }
}
