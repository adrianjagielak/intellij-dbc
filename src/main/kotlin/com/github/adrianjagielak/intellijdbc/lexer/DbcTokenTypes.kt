package com.github.adrianjagielak.intellijdbc.lexer

import com.github.adrianjagielak.intellijdbc.DbcLanguage
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class DbcTokenType(debugName: String) : IElementType(debugName, DbcLanguage.INSTANCE) {
    override fun toString(): String = "DbcTokenType.${super.toString()}"
}

object DbcTokenTypes {
    // Whitespace & comments
    @JvmField val WHITE_SPACE = DbcTokenType("WHITE_SPACE")
    @JvmField val LINE_COMMENT = DbcTokenType("LINE_COMMENT")

    // Literals
    @JvmField val INTEGER = DbcTokenType("INTEGER")
    @JvmField val FLOAT = DbcTokenType("FLOAT")
    @JvmField val STRING = DbcTokenType("STRING")
    @JvmField val IDENTIFIER = DbcTokenType("IDENTIFIER")

    // Punctuation
    @JvmField val COLON = DbcTokenType("COLON")
    @JvmField val SEMICOLON = DbcTokenType("SEMICOLON")
    @JvmField val COMMA = DbcTokenType("COMMA")
    @JvmField val PIPE = DbcTokenType("PIPE")
    @JvmField val AT = DbcTokenType("AT")
    @JvmField val PLUS = DbcTokenType("PLUS")
    @JvmField val MINUS = DbcTokenType("MINUS")
    @JvmField val LPAREN = DbcTokenType("LPAREN")
    @JvmField val RPAREN = DbcTokenType("RPAREN")
    @JvmField val LBRACKET = DbcTokenType("LBRACKET")
    @JvmField val RBRACKET = DbcTokenType("RBRACKET")

    // Keywords
    @JvmField val VERSION = DbcTokenType("VERSION")
    @JvmField val NS_ = DbcTokenType("NS_")
    @JvmField val BS_ = DbcTokenType("BS_")
    @JvmField val BU_ = DbcTokenType("BU_")
    @JvmField val BO_ = DbcTokenType("BO_")
    @JvmField val SG_ = DbcTokenType("SG_")
    @JvmField val CM_ = DbcTokenType("CM_")
    @JvmField val BA_DEF_ = DbcTokenType("BA_DEF_")
    @JvmField val BA_DEF_DEF_ = DbcTokenType("BA_DEF_DEF_")
    @JvmField val BA_ = DbcTokenType("BA_")
    @JvmField val VAL_ = DbcTokenType("VAL_")
    @JvmField val VAL_TABLE_ = DbcTokenType("VAL_TABLE_")
    @JvmField val SIG_GROUP_ = DbcTokenType("SIG_GROUP_")
    @JvmField val BO_TX_BU_ = DbcTokenType("BO_TX_BU_")
    @JvmField val EV_ = DbcTokenType("EV_")
    @JvmField val SG_MUL_VAL_ = DbcTokenType("SG_MUL_VAL_")
    @JvmField val SGTYPE_ = DbcTokenType("SGTYPE_")
    @JvmField val SIG_TYPE_REF_ = DbcTokenType("SIG_TYPE_REF_")
    @JvmField val BA_REL_ = DbcTokenType("BA_REL_")
    @JvmField val BA_DEF_REL_ = DbcTokenType("BA_DEF_REL_")
    @JvmField val BA_SGTYPE_ = DbcTokenType("BA_SGTYPE_")
    @JvmField val SIG_VALTYPE_ = DbcTokenType("SIG_VALTYPE_")
    @JvmField val ENVVAR_DATA_ = DbcTokenType("ENVVAR_DATA_")
    @JvmField val SGTYPE_VAL_ = DbcTokenType("SGTYPE_VAL_")

    // Special keyword-like tokens used in attribute definitions
    @JvmField val INT_KEYWORD = DbcTokenType("INT_KEYWORD")
    @JvmField val FLOAT_KEYWORD = DbcTokenType("FLOAT_KEYWORD")
    @JvmField val STRING_KEYWORD = DbcTokenType("STRING_KEYWORD")
    @JvmField val HEX_KEYWORD = DbcTokenType("HEX_KEYWORD")
    @JvmField val ENUM_KEYWORD = DbcTokenType("ENUM_KEYWORD")

    // Multiplexer indicators
    @JvmField val MUX_INDICATOR = DbcTokenType("MUX_INDICATOR")

    // Bad character
    @JvmField val BAD_CHARACTER = DbcTokenType("BAD_CHARACTER")

    // Token sets
    @JvmField val KEYWORDS = TokenSet.create(
        VERSION, NS_, BS_, BU_, BO_, SG_, CM_,
        BA_DEF_, BA_DEF_DEF_, BA_, VAL_, VAL_TABLE_,
        SIG_GROUP_, BO_TX_BU_, EV_, SG_MUL_VAL_,
        SGTYPE_, SIG_TYPE_REF_, BA_REL_, BA_DEF_REL_,
        BA_SGTYPE_, SIG_VALTYPE_, ENVVAR_DATA_, SGTYPE_VAL_,
        INT_KEYWORD, FLOAT_KEYWORD, STRING_KEYWORD, HEX_KEYWORD, ENUM_KEYWORD
    )

    @JvmField val COMMENTS = TokenSet.create(LINE_COMMENT)

    @JvmField val STRINGS = TokenSet.create(STRING)

    @JvmField val NUMBERS = TokenSet.create(INTEGER, FLOAT)

    @JvmField val WHITE_SPACES = TokenSet.create(WHITE_SPACE)

    private val KEYWORD_MAP: Map<String, IElementType> = mapOf(
        "VERSION" to VERSION,
        "NS_" to NS_,
        "BS_" to BS_,
        "BU_" to BU_,
        "BO_" to BO_,
        "SG_" to SG_,
        "CM_" to CM_,
        "BA_DEF_DEF_" to BA_DEF_DEF_,
        "BA_DEF_REL_" to BA_DEF_REL_,
        "BA_DEF_" to BA_DEF_,
        "BA_REL_" to BA_REL_,
        "BA_SGTYPE_" to BA_SGTYPE_,
        "BA_" to BA_,
        "VAL_TABLE_" to VAL_TABLE_,
        "VAL_" to VAL_,
        "SIG_GROUP_" to SIG_GROUP_,
        "SIG_TYPE_REF_" to SIG_TYPE_REF_,
        "SIG_VALTYPE_" to SIG_VALTYPE_,
        "BO_TX_BU_" to BO_TX_BU_,
        "EV_" to EV_,
        "SG_MUL_VAL_" to SG_MUL_VAL_,
        "SGTYPE_VAL_" to SGTYPE_VAL_,
        "SGTYPE_" to SGTYPE_,
        "ENVVAR_DATA_" to ENVVAR_DATA_,
        "INT" to INT_KEYWORD,
        "FLOAT" to FLOAT_KEYWORD,
        "STRING" to STRING_KEYWORD,
        "HEX" to HEX_KEYWORD,
        "ENUM" to ENUM_KEYWORD,
    )

    fun lookupKeyword(text: String): IElementType? = KEYWORD_MAP[text]
}
