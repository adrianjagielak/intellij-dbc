package com.github.adrianjagielak.intellijdbc.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class DbcLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var startOffset: Int = 0
    private var endOffset: Int = 0
    private var position: Int = 0
    private var tokenStart: Int = 0
    private var tokenEnd: Int = 0
    private var tokenType: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.position = startOffset
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        this.tokenType = null
        advance()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = tokenType

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = endOffset

    override fun advance() {
        tokenStart = position
        if (position >= endOffset) {
            tokenType = null
            return
        }

        val c = buffer[position]

        when {
            c == '/' && position + 1 < endOffset && buffer[position + 1] == '/' -> lexLineComment()
            c.isWhitespace() -> lexWhitespace()
            c == '"' -> lexString()
            c == '-' && position + 1 < endOffset && buffer[position + 1].isDigit() && shouldLexNegativeNumber() -> lexNumber()
            c.isDigit() -> lexNumber()
            c.isLetter() || c == '_' -> lexIdentifierOrKeyword()
            c == ':' -> lexSingleChar(DbcTokenTypes.COLON)
            c == ';' -> lexSingleChar(DbcTokenTypes.SEMICOLON)
            c == ',' -> lexSingleChar(DbcTokenTypes.COMMA)
            c == '|' -> lexSingleChar(DbcTokenTypes.PIPE)
            c == '@' -> lexSingleChar(DbcTokenTypes.AT)
            c == '+' -> lexSingleChar(DbcTokenTypes.PLUS)
            c == '-' -> lexSingleChar(DbcTokenTypes.MINUS)
            c == '(' -> lexSingleChar(DbcTokenTypes.LPAREN)
            c == ')' -> lexSingleChar(DbcTokenTypes.RPAREN)
            c == '[' -> lexSingleChar(DbcTokenTypes.LBRACKET)
            c == ']' -> lexSingleChar(DbcTokenTypes.RBRACKET)
            else -> {
                position++
                tokenEnd = position
                tokenType = DbcTokenTypes.BAD_CHARACTER
            }
        }
    }

    private fun shouldLexNegativeNumber(): Boolean {
        // Look backward past whitespace to see if the previous token context suggests a number
        // Negative numbers appear after (, [, ,, :, |, and at line starts
        var lookBack = tokenStart - 1
        while (lookBack >= startOffset && buffer[lookBack].isWhitespace()) {
            lookBack--
        }
        if (lookBack < startOffset) return true
        val prevChar = buffer[lookBack]
        return prevChar in "([,:|@;" || prevChar == '"'
    }

    private fun lexLineComment() {
        position += 2 // skip //
        while (position < endOffset && buffer[position] != '\n') {
            position++
        }
        tokenEnd = position
        tokenType = DbcTokenTypes.LINE_COMMENT
    }

    private fun lexWhitespace() {
        while (position < endOffset && buffer[position].isWhitespace()) {
            position++
        }
        tokenEnd = position
        tokenType = DbcTokenTypes.WHITE_SPACE
    }

    private fun lexString() {
        position++ // skip opening quote
        while (position < endOffset && buffer[position] != '"') {
            if (buffer[position] == '\\' && position + 1 < endOffset) {
                position += 2 // skip escape sequence
            } else {
                position++
            }
        }
        if (position < endOffset) {
            position++ // skip closing quote
        }
        tokenEnd = position
        tokenType = DbcTokenTypes.STRING
    }

    private fun lexNumber() {
        if (position < endOffset && buffer[position] == '-') {
            position++
        }

        // Check for hex
        if (position + 1 < endOffset && buffer[position] == '0' && (buffer[position + 1] == 'x' || buffer[position + 1] == 'X')) {
            position += 2
            while (position < endOffset && isHexDigit(buffer[position])) {
                position++
            }
            tokenEnd = position
            tokenType = DbcTokenTypes.INTEGER
            return
        }

        while (position < endOffset && buffer[position].isDigit()) {
            position++
        }

        // Check for float
        if (position < endOffset && buffer[position] == '.' && position + 1 < endOffset && buffer[position + 1].isDigit()) {
            position++ // skip dot
            while (position < endOffset && buffer[position].isDigit()) {
                position++
            }
            // Check for scientific notation
            if (position < endOffset && (buffer[position] == 'e' || buffer[position] == 'E')) {
                position++
                if (position < endOffset && (buffer[position] == '+' || buffer[position] == '-')) {
                    position++
                }
                while (position < endOffset && buffer[position].isDigit()) {
                    position++
                }
            }
            tokenEnd = position
            tokenType = DbcTokenTypes.FLOAT
        } else if (position < endOffset && (buffer[position] == 'e' || buffer[position] == 'E')) {
            position++
            if (position < endOffset && (buffer[position] == '+' || buffer[position] == '-')) {
                position++
            }
            while (position < endOffset && buffer[position].isDigit()) {
                position++
            }
            tokenEnd = position
            tokenType = DbcTokenTypes.FLOAT
        } else {
            tokenEnd = position
            tokenType = DbcTokenTypes.INTEGER
        }
    }

    private fun lexIdentifierOrKeyword() {
        while (position < endOffset && isIdentifierPart(buffer[position])) {
            position++
        }
        val text = buffer.subSequence(tokenStart, position).toString()

        // Check for multiplexer indicator patterns: m0_3, m123, M (capital only for multiplex switch)
        if (isMuxIndicator(text)) {
            tokenEnd = position
            tokenType = DbcTokenTypes.MUX_INDICATOR
            return
        }

        val keyword = DbcTokenTypes.lookupKeyword(text)
        tokenEnd = position
        tokenType = keyword ?: DbcTokenTypes.IDENTIFIER
    }

    private fun isMuxIndicator(text: String): Boolean {
        // M = multiplex switch indicator
        if (text == "M") return false // treat as identifier, parser handles context
        // m<number> = multiplexed signal, m<number>_<number> = extended mux
        if (text.length >= 2 && text[0] == 'm' && text[1].isDigit()) {
            // Validate: m<digits> or m<digits>_<digits>
            var i = 1
            while (i < text.length && text[i].isDigit()) i++
            if (i == text.length) return true
            if (text[i] == '_') {
                i++
                while (i < text.length && text[i].isDigit()) i++
                return i == text.length
            }
        }
        return false
    }

    private fun lexSingleChar(type: IElementType) {
        position++
        tokenEnd = position
        tokenType = type
    }

    private fun isIdentifierPart(c: Char): Boolean =
        c.isLetterOrDigit() || c == '_'

    private fun isHexDigit(c: Char): Boolean =
        c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'
}
