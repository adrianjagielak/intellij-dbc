package com.github.adrianjagielak.intellijdbc.parser

import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.github.adrianjagielak.intellijdbc.psi.DbcElementTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class DbcParser : PsiParser {

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        while (!builder.eof()) {
            parseTopLevel(builder)
        }
        rootMarker.done(root)
        return builder.treeBuilt
    }

    private fun parseTopLevel(builder: PsiBuilder) {
        skipWhitespaceAndComments(builder)
        if (builder.eof()) return

        when (builder.tokenType) {
            DbcTokenTypes.VERSION -> parseVersion(builder)
            DbcTokenTypes.NS_ -> parseNewSymbols(builder)
            DbcTokenTypes.BS_ -> parseBusSpeed(builder)
            DbcTokenTypes.BU_ -> parseNodes(builder)
            DbcTokenTypes.BO_ -> parseMessage(builder)
            DbcTokenTypes.CM_ -> parseComment(builder)
            DbcTokenTypes.BA_DEF_DEF_ -> parseAttributeDefault(builder)
            DbcTokenTypes.BA_DEF_REL_ -> parseAttributeRelDefinition(builder)
            DbcTokenTypes.BA_DEF_ -> parseAttributeDefinition(builder)
            DbcTokenTypes.BA_REL_ -> parseAttributeRelValue(builder)
            DbcTokenTypes.BA_SGTYPE_ -> parseGenericUntilSemicolon(builder, DbcElementTypes.SIGNAL_GROUP_SIGNAL_TYPE_DEF)
            DbcTokenTypes.BA_ -> parseAttributeValue(builder)
            DbcTokenTypes.VAL_TABLE_ -> parseValueTable(builder)
            DbcTokenTypes.VAL_ -> parseValueDescription(builder)
            DbcTokenTypes.SIG_GROUP_ -> parseSignalGroup(builder)
            DbcTokenTypes.BO_TX_BU_ -> parseMessageTransmitters(builder)
            DbcTokenTypes.EV_ -> parseEnvironmentVariable(builder)
            DbcTokenTypes.SG_MUL_VAL_ -> parseMuxSignal(builder)
            DbcTokenTypes.SGTYPE_ -> parseGenericUntilSemicolon(builder, DbcElementTypes.SIGNAL_TYPE_DEF)
            DbcTokenTypes.SGTYPE_VAL_ -> parseGenericUntilSemicolon(builder, DbcElementTypes.SIGNAL_GROUP_SIGNAL_TYPE_DEF)
            DbcTokenTypes.SIG_TYPE_REF_ -> parseGenericUntilSemicolon(builder, DbcElementTypes.SIGNAL_TYPE_REF_DEF)
            DbcTokenTypes.SIG_VALTYPE_ -> parseSignalValtype(builder)
            DbcTokenTypes.ENVVAR_DATA_ -> parseGenericUntilSemicolon(builder, DbcElementTypes.ENVIRONMENT_VARIABLE_DATA_DEF)
            else -> {
                // Skip unrecognized tokens
                builder.advanceLexer()
            }
        }
    }

    private fun parseVersion(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.VERSION) // VERSION keyword
        skipWhitespace(builder)
        expect(builder, DbcTokenTypes.STRING)   // version string
        marker.done(DbcElementTypes.VERSION_DEF)
    }

    private fun parseNewSymbols(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.NS_)
        skipWhitespace(builder)
        // Expect colon
        if (builder.tokenType == DbcTokenTypes.COLON) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)
        // Read symbol identifiers until we hit a known keyword at top level
        while (!builder.eof() && builder.tokenType == DbcTokenTypes.IDENTIFIER) {
            builder.advanceLexer()
            skipWhitespace(builder)
        }
        marker.done(DbcElementTypes.NEW_SYMBOLS_DEF)
    }

    private fun parseBusSpeed(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.BS_)
        skipWhitespace(builder)
        if (builder.tokenType == DbcTokenTypes.COLON) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)
        // Optional bus speed value
        if (builder.tokenType == DbcTokenTypes.INTEGER) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.BUS_SPEED_DEF)
    }

    private fun parseNodes(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.BU_)
        skipWhitespace(builder)
        if (builder.tokenType == DbcTokenTypes.COLON) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)
        // Read node names
        while (!builder.eof() && builder.tokenType == DbcTokenTypes.IDENTIFIER) {
            val nodeMarker = builder.mark()
            builder.advanceLexer()
            nodeMarker.done(DbcElementTypes.NODE_NAME)
            skipWhitespace(builder)
        }
        marker.done(DbcElementTypes.NODES_DEF)
    }

    private fun parseMessage(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.BO_)
        skipWhitespace(builder)

        // Message ID
        expect(builder, DbcTokenTypes.INTEGER)
        skipWhitespace(builder)

        // Message name
        expect(builder, DbcTokenTypes.IDENTIFIER)
        skipWhitespace(builder)

        // Colon
        if (builder.tokenType == DbcTokenTypes.COLON) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)

        // DLC (message length)
        expect(builder, DbcTokenTypes.INTEGER)
        skipWhitespace(builder)

        // Transmitter node
        if (builder.tokenType == DbcTokenTypes.IDENTIFIER) {
            val txMarker = builder.mark()
            builder.advanceLexer()
            txMarker.done(DbcElementTypes.TRANSMITTER_NAME)
        }
        skipWhitespace(builder)

        // Parse signals (SG_ entries within the message)
        while (!builder.eof() && builder.tokenType == DbcTokenTypes.SG_) {
            parseSignal(builder)
            skipWhitespace(builder)
        }

        marker.done(DbcElementTypes.MESSAGE_DEF)
    }

    private fun parseSignal(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.SG_)
        skipWhitespace(builder)

        // Signal name
        expect(builder, DbcTokenTypes.IDENTIFIER)
        skipWhitespace(builder)

        // Optional multiplexer indicator (M, m0, m0_3, etc.)
        if (builder.tokenType == DbcTokenTypes.MUX_INDICATOR ||
            (builder.tokenType == DbcTokenTypes.IDENTIFIER && builder.tokenText == "M")) {
            builder.advanceLexer()
            skipWhitespace(builder)
        }

        // Colon
        if (builder.tokenType == DbcTokenTypes.COLON) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)

        // Start bit
        expect(builder, DbcTokenTypes.INTEGER)
        skipWhitespace(builder)

        // Pipe
        if (builder.tokenType == DbcTokenTypes.PIPE) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)

        // Bit length
        expect(builder, DbcTokenTypes.INTEGER)
        skipWhitespace(builder)

        // @ byte order
        if (builder.tokenType == DbcTokenTypes.AT) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)

        // Byte order (0 or 1)
        expect(builder, DbcTokenTypes.INTEGER)
        skipWhitespace(builder)

        // Value type (+ or -)
        if (builder.tokenType == DbcTokenTypes.PLUS || builder.tokenType == DbcTokenTypes.MINUS) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)

        // (factor,offset)
        if (builder.tokenType == DbcTokenTypes.LPAREN) {
            builder.advanceLexer()
            skipWhitespace(builder)
            parseNumberWithSign(builder)  // factor
            skipWhitespace(builder)
            if (builder.tokenType == DbcTokenTypes.COMMA) {
                builder.advanceLexer()
            }
            skipWhitespace(builder)
            parseNumberWithSign(builder)  // offset
            skipWhitespace(builder)
            if (builder.tokenType == DbcTokenTypes.RPAREN) {
                builder.advanceLexer()
            }
        }
        skipWhitespace(builder)

        // [min|max]
        if (builder.tokenType == DbcTokenTypes.LBRACKET) {
            builder.advanceLexer()
            skipWhitespace(builder)
            parseNumberWithSign(builder)  // min
            skipWhitespace(builder)
            if (builder.tokenType == DbcTokenTypes.PIPE) {
                builder.advanceLexer()
            }
            skipWhitespace(builder)
            parseNumberWithSign(builder)  // max
            skipWhitespace(builder)
            if (builder.tokenType == DbcTokenTypes.RBRACKET) {
                builder.advanceLexer()
            }
        }
        skipWhitespace(builder)

        // Unit string
        expect(builder, DbcTokenTypes.STRING)
        skipWhitespace(builder)

        // Receiver nodes (comma-separated)
        if (builder.tokenType == DbcTokenTypes.IDENTIFIER) {
            val recvMarker = builder.mark()
            builder.advanceLexer()
            recvMarker.done(DbcElementTypes.RECEIVER_NAME)
            skipWhitespace(builder)
            while (builder.tokenType == DbcTokenTypes.COMMA) {
                builder.advanceLexer()
                skipWhitespace(builder)
                if (builder.tokenType == DbcTokenTypes.IDENTIFIER) {
                    val rm = builder.mark()
                    builder.advanceLexer()
                    rm.done(DbcElementTypes.RECEIVER_NAME)
                }
                skipWhitespace(builder)
            }
        }

        marker.done(DbcElementTypes.SIGNAL_DEF)
    }

    private fun parseNumberWithSign(builder: PsiBuilder) {
        if (builder.tokenType == DbcTokenTypes.MINUS || builder.tokenType == DbcTokenTypes.PLUS) {
            builder.advanceLexer()
            skipWhitespace(builder)
        }
        if (builder.tokenType == DbcTokenTypes.INTEGER || builder.tokenType == DbcTokenTypes.FLOAT) {
            builder.advanceLexer()
        }
    }

    private fun parseComment(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.CM_)
        skipWhitespace(builder)

        // Check for object type (BU_, BO_, SG_, EV_)
        when (builder.tokenType) {
            DbcTokenTypes.BU_ -> {
                builder.advanceLexer()
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.IDENTIFIER)  // node name
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.STRING)       // comment text
            }
            DbcTokenTypes.BO_ -> {
                builder.advanceLexer()
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.INTEGER)      // message ID
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.STRING)       // comment text
            }
            DbcTokenTypes.SG_ -> {
                builder.advanceLexer()
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.INTEGER)      // message ID
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.IDENTIFIER)   // signal name
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.STRING)       // comment text
            }
            DbcTokenTypes.EV_ -> {
                builder.advanceLexer()
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.IDENTIFIER)   // env var name
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.STRING)       // comment text
            }
            DbcTokenTypes.STRING -> {
                // Global comment
                builder.advanceLexer()
            }
            else -> {
                // Try to recover
                advanceUntilSemicolon(builder)
            }
        }
        skipWhitespace(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.COMMENT_DEF)
    }

    private fun parseAttributeDefinition(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.BA_DEF_)
        skipWhitespace(builder)

        // Optional object type
        if (builder.tokenType == DbcTokenTypes.BU_ || builder.tokenType == DbcTokenTypes.BO_ ||
            builder.tokenType == DbcTokenTypes.SG_ || builder.tokenType == DbcTokenTypes.EV_) {
            builder.advanceLexer()
            skipWhitespace(builder)
        }

        // Attribute name (string)
        expect(builder, DbcTokenTypes.STRING)
        skipWhitespace(builder)

        // Value type and parameters - consume everything until semicolon
        advanceUntilSemicolon(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.ATTRIBUTE_DEFINITION)
    }

    private fun parseAttributeDefault(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.BA_DEF_DEF_)
        skipWhitespace(builder)

        // Attribute name
        expect(builder, DbcTokenTypes.STRING)
        skipWhitespace(builder)

        // Default value - consume until semicolon
        advanceUntilSemicolon(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.ATTRIBUTE_DEFAULT)
    }

    private fun parseAttributeValue(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.BA_)
        skipWhitespace(builder)

        // Attribute name
        expect(builder, DbcTokenTypes.STRING)
        skipWhitespace(builder)

        // Optional object type and ID/name
        if (builder.tokenType == DbcTokenTypes.BU_ || builder.tokenType == DbcTokenTypes.BO_ ||
            builder.tokenType == DbcTokenTypes.SG_ || builder.tokenType == DbcTokenTypes.EV_) {
            builder.advanceLexer()
            skipWhitespace(builder)
        }

        // Consume remaining tokens until semicolon
        advanceUntilSemicolon(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.ATTRIBUTE_VALUE)
    }

    private fun parseValueTable(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.VAL_TABLE_)
        skipWhitespace(builder)

        // Table name
        expect(builder, DbcTokenTypes.IDENTIFIER)
        skipWhitespace(builder)

        // Value-description pairs: integer "string" ...
        while (!builder.eof() && builder.tokenType != DbcTokenTypes.SEMICOLON) {
            if (builder.tokenType == DbcTokenTypes.INTEGER) {
                builder.advanceLexer()
                skipWhitespace(builder)
                expect(builder, DbcTokenTypes.STRING)
                skipWhitespace(builder)
            } else {
                builder.advanceLexer()
                skipWhitespace(builder)
            }
        }
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.VALUE_TABLE_DEF)
    }

    private fun parseValueDescription(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.VAL_)
        skipWhitespace(builder)

        // Message ID or environment variable name
        if (builder.tokenType == DbcTokenTypes.INTEGER) {
            builder.advanceLexer()
            skipWhitespace(builder)
            // Signal name
            expect(builder, DbcTokenTypes.IDENTIFIER)
            skipWhitespace(builder)
        } else if (builder.tokenType == DbcTokenTypes.IDENTIFIER) {
            builder.advanceLexer()
            skipWhitespace(builder)
        }

        // Value-description pairs
        while (!builder.eof() && builder.tokenType != DbcTokenTypes.SEMICOLON) {
            if (builder.tokenType == DbcTokenTypes.INTEGER) {
                builder.advanceLexer()
                skipWhitespace(builder)
                if (builder.tokenType == DbcTokenTypes.STRING) {
                    builder.advanceLexer()
                    skipWhitespace(builder)
                }
            } else {
                builder.advanceLexer()
                skipWhitespace(builder)
            }
        }
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.VALUE_DESCRIPTION)
    }

    private fun parseSignalGroup(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.SIG_GROUP_)
        skipWhitespace(builder)

        // Message ID
        expect(builder, DbcTokenTypes.INTEGER)
        skipWhitespace(builder)

        // Group name
        expect(builder, DbcTokenTypes.IDENTIFIER)
        skipWhitespace(builder)

        // Repetitions (integer)
        expect(builder, DbcTokenTypes.INTEGER)
        skipWhitespace(builder)

        // Colon
        if (builder.tokenType == DbcTokenTypes.COLON) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)

        // Signal names
        while (!builder.eof() && builder.tokenType == DbcTokenTypes.IDENTIFIER) {
            builder.advanceLexer()
            skipWhitespace(builder)
            if (builder.tokenType == DbcTokenTypes.COMMA) {
                builder.advanceLexer()
                skipWhitespace(builder)
            }
        }

        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.SIGNAL_GROUP_DEF)
    }

    private fun parseMessageTransmitters(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.BO_TX_BU_)
        skipWhitespace(builder)

        // Message ID
        expect(builder, DbcTokenTypes.INTEGER)
        skipWhitespace(builder)

        // Colon
        if (builder.tokenType == DbcTokenTypes.COLON) {
            builder.advanceLexer()
        }
        skipWhitespace(builder)

        // Transmitter nodes
        while (!builder.eof() && builder.tokenType == DbcTokenTypes.IDENTIFIER) {
            builder.advanceLexer()
            skipWhitespace(builder)
            if (builder.tokenType == DbcTokenTypes.COMMA) {
                builder.advanceLexer()
                skipWhitespace(builder)
            }
        }

        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.MESSAGE_TRANSMITTERS_DEF)
    }

    private fun parseEnvironmentVariable(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.EV_)
        skipWhitespace(builder)

        // Consume everything until semicolon
        advanceUntilSemicolon(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.ENVIRONMENT_VARIABLE_DEF)
    }

    private fun parseMuxSignal(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.SG_MUL_VAL_)
        skipWhitespace(builder)

        advanceUntilSemicolon(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.MUX_SIGNAL_DEF)
    }

    private fun parseSignalValtype(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.SIG_VALTYPE_)
        skipWhitespace(builder)

        advanceUntilSemicolon(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.SIGNAL_VALTYPE_DEF)
    }

    private fun parseAttributeRelDefinition(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.BA_DEF_REL_)
        skipWhitespace(builder)

        advanceUntilSemicolon(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.ATTRIBUTE_REL_DEFINITION)
    }

    private fun parseAttributeRelValue(builder: PsiBuilder) {
        val marker = builder.mark()
        expect(builder, DbcTokenTypes.BA_REL_)
        skipWhitespace(builder)

        advanceUntilSemicolon(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(DbcElementTypes.ATTRIBUTE_REL_VALUE)
    }

    private fun parseGenericUntilSemicolon(builder: PsiBuilder, elementType: IElementType) {
        val marker = builder.mark()
        builder.advanceLexer() // consume keyword
        skipWhitespace(builder)
        advanceUntilSemicolon(builder)
        if (builder.tokenType == DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
        marker.done(elementType)
    }

    // Utility methods

    private fun expect(builder: PsiBuilder, tokenType: IElementType): Boolean {
        if (builder.tokenType == tokenType) {
            builder.advanceLexer()
            return true
        }
        builder.error("Expected ${tokenType}")
        return false
    }

    private fun skipWhitespace(builder: PsiBuilder) {
        while (!builder.eof() && (builder.tokenType == DbcTokenTypes.WHITE_SPACE ||
                    builder.tokenType == DbcTokenTypes.LINE_COMMENT)) {
            builder.advanceLexer()
        }
    }

    private fun skipWhitespaceAndComments(builder: PsiBuilder) {
        while (!builder.eof() && (builder.tokenType == DbcTokenTypes.WHITE_SPACE ||
                    builder.tokenType == DbcTokenTypes.LINE_COMMENT)) {
            builder.advanceLexer()
        }
    }

    private fun advanceUntilSemicolon(builder: PsiBuilder) {
        while (!builder.eof() && builder.tokenType != DbcTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }
    }
}
