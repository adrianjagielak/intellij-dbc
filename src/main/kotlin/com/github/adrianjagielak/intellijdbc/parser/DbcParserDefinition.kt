package com.github.adrianjagielak.intellijdbc.parser

import com.github.adrianjagielak.intellijdbc.DbcFile
import com.github.adrianjagielak.intellijdbc.lexer.DbcLexer
import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.github.adrianjagielak.intellijdbc.psi.DbcElementTypes
import com.github.adrianjagielak.intellijdbc.psi.DbcTokenSets
import com.github.adrianjagielak.intellijdbc.psi.impl.*
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class DbcParserDefinition : ParserDefinition {

    override fun createLexer(project: Project?): Lexer = DbcLexer()

    override fun createParser(project: Project?): PsiParser = DbcParser()

    override fun getFileNodeType(): IFileElementType = DbcElementTypes.FILE

    override fun getWhitespaceTokens(): TokenSet = DbcTokenSets.WHITE_SPACES

    override fun getCommentTokens(): TokenSet = DbcTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = DbcTokenSets.STRINGS

    override fun createElement(node: ASTNode): PsiElement {
        return when (node.elementType) {
            DbcElementTypes.VERSION_DEF -> DbcVersionDefImpl(node)
            DbcElementTypes.NEW_SYMBOLS_DEF -> DbcGenericElementImpl(node)
            DbcElementTypes.BUS_SPEED_DEF -> DbcGenericElementImpl(node)
            DbcElementTypes.NODES_DEF -> DbcNodesDefImpl(node)
            DbcElementTypes.NODE_NAME -> DbcNodeNameImpl(node)
            DbcElementTypes.MESSAGE_DEF -> DbcMessageDefImpl(node)
            DbcElementTypes.SIGNAL_DEF -> DbcSignalDefImpl(node)
            DbcElementTypes.TRANSMITTER_NAME -> DbcTransmitterNameImpl(node)
            DbcElementTypes.RECEIVER_NAME -> DbcReceiverNameImpl(node)
            DbcElementTypes.COMMENT_DEF -> DbcCommentDefImpl(node)
            DbcElementTypes.VALUE_TABLE_DEF -> DbcValueTableDefImpl(node)
            DbcElementTypes.ATTRIBUTE_DEFINITION -> DbcAttributeDefinitionDefImpl(node)
            DbcElementTypes.ATTRIBUTE_DEFAULT -> DbcAttributeDefaultDefImpl(node)
            DbcElementTypes.ATTRIBUTE_VALUE -> DbcAttributeValueDefImpl(node)
            DbcElementTypes.VALUE_DESCRIPTION -> DbcValueDescriptionDefImpl(node)
            DbcElementTypes.SIGNAL_GROUP_DEF -> DbcSignalGroupDefImpl(node)
            DbcElementTypes.MESSAGE_TRANSMITTERS_DEF -> DbcMessageTransmittersDefImpl(node)
            DbcElementTypes.ENVIRONMENT_VARIABLE_DEF -> DbcEnvironmentVariableDefImpl(node)
            DbcElementTypes.MUX_SIGNAL_DEF -> DbcMuxSignalDefImpl(node)
            DbcElementTypes.SIGNAL_VALTYPE_DEF -> DbcSignalValtypeDefImpl(node)
            DbcElementTypes.ATTRIBUTE_REL_VALUE -> DbcGenericElementImpl(node)
            DbcElementTypes.ATTRIBUTE_REL_DEFINITION -> DbcGenericElementImpl(node)
            DbcElementTypes.SIGNAL_TYPE_DEF -> DbcGenericElementImpl(node)
            DbcElementTypes.SIGNAL_TYPE_REF_DEF -> DbcGenericElementImpl(node)
            DbcElementTypes.SIGNAL_GROUP_SIGNAL_TYPE_DEF -> DbcGenericElementImpl(node)
            DbcElementTypes.ENVIRONMENT_VARIABLE_DATA_DEF -> DbcGenericElementImpl(node)
            else -> DbcGenericElementImpl(node)
        }
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile = DbcFile(viewProvider)
}
