package com.github.adrianjagielak.intellijdbc.navigation

import com.github.adrianjagielak.intellijdbc.DbcIcons
import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.github.adrianjagielak.intellijdbc.psi.DbcElementTypes
import com.github.adrianjagielak.intellijdbc.psi.DbcMessageDef
import com.github.adrianjagielak.intellijdbc.psi.DbcSignalDef
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcNodeNameImpl
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement

class DbcLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Line markers should be attached to leaf elements for correct gutter placement
        if (element.node.elementType != DbcTokenTypes.IDENTIFIER) return null

        val parent = element.parent ?: return null

        return when {
            parent is DbcMessageDef && parent.nameIdentifier == element -> {
                val id = parent.messageId
                val idStr = if (id != null) "0x%X (%d)".format(id, id) else "?"
                LineMarkerInfo(
                    element,
                    element.textRange,
                    DbcIcons.MESSAGE,
                    { "Message: ${parent.messageName} [ID=$idStr, DLC=${parent.dlc}]" },
                    null,
                    GutterIconRenderer.Alignment.LEFT,
                    { "Message: ${parent.messageName}" }
                )
            }
            parent is DbcSignalDef && parent.nameIdentifier == element -> {
                val bits = "${parent.startBit ?: "?"}|${parent.bitLength ?: "?"}"
                val unit = parent.unit?.let { " [$it]" } ?: ""
                LineMarkerInfo(
                    element,
                    element.textRange,
                    DbcIcons.SIGNAL,
                    { "Signal: ${parent.signalName} ($bits)$unit" },
                    null,
                    GutterIconRenderer.Alignment.LEFT,
                    { "Signal: ${parent.signalName}" }
                )
            }
            parent is DbcNodeNameImpl -> {
                LineMarkerInfo(
                    element,
                    element.textRange,
                    DbcIcons.NODE,
                    { "Node: ${parent.name}" },
                    null,
                    GutterIconRenderer.Alignment.LEFT,
                    { "Node: ${parent.name}" }
                )
            }
            else -> null
        }
    }
}
