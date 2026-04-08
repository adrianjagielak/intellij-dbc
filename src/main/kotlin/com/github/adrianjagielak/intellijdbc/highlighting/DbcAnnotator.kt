package com.github.adrianjagielak.intellijdbc.highlighting

import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.github.adrianjagielak.intellijdbc.psi.DbcElementTypes
import com.github.adrianjagielak.intellijdbc.psi.DbcMessageDef
import com.github.adrianjagielak.intellijdbc.psi.DbcNodeName
import com.github.adrianjagielak.intellijdbc.psi.DbcSignalDef
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcReceiverNameImpl
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcTransmitterNameImpl
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.psi.PsiElement

class DbcAnnotator : Annotator {

    companion object {
        @JvmField
        val MESSAGE_NAME_ATTR = createTextAttributesKey("DBC_MESSAGE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
        @JvmField
        val SIGNAL_NAME_ATTR = createTextAttributesKey("DBC_SIGNAL_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        @JvmField
        val NODE_NAME_ATTR = createTextAttributesKey("DBC_NODE_NAME", DefaultLanguageHighlighterColors.CONSTANT)
        @JvmField
        val MESSAGE_ID_ATTR = createTextAttributesKey("DBC_MESSAGE_ID", DefaultLanguageHighlighterColors.NUMBER)
        @JvmField
        val UNIT_ATTR = createTextAttributesKey("DBC_UNIT", DefaultLanguageHighlighterColors.STRING)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when {
            element is DbcMessageDef -> annotateMessageDef(element, holder)
            element is DbcSignalDef -> annotateSignalDef(element, holder)
            element is DbcNodeName -> annotateNodeName(element, holder)
            element is DbcTransmitterNameImpl -> annotateNodeRef(element, holder)
            element is DbcReceiverNameImpl -> annotateNodeRef(element, holder)
        }
    }

    private fun annotateMessageDef(element: DbcMessageDef, holder: AnnotationHolder) {
        // Highlight message name
        val nameElement = element.nameIdentifier
        if (nameElement != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(nameElement)
                .textAttributes(MESSAGE_NAME_ATTR)
                .create()
        }

        // Highlight message ID
        val idNode = element.node.findChildByType(DbcTokenTypes.INTEGER)
        if (idNode != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(idNode)
                .textAttributes(MESSAGE_ID_ATTR)
                .create()
        }
    }

    private fun annotateSignalDef(element: DbcSignalDef, holder: AnnotationHolder) {
        val nameElement = element.nameIdentifier
        if (nameElement != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(nameElement)
                .textAttributes(SIGNAL_NAME_ATTR)
                .create()
        }
    }

    private fun annotateNodeName(element: DbcNodeName, holder: AnnotationHolder) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element)
            .textAttributes(NODE_NAME_ATTR)
            .create()
    }

    private fun annotateNodeRef(element: PsiElement, holder: AnnotationHolder) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element)
            .textAttributes(NODE_NAME_ATTR)
            .create()
    }
}
