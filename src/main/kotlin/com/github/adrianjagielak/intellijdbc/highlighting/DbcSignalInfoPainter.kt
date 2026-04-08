package com.github.adrianjagielak.intellijdbc.highlighting

import com.github.adrianjagielak.intellijdbc.DbcFileType
import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.openapi.editor.EditorLinePainter
import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import java.awt.Color
import java.awt.Font

class DbcSignalInfoPainter : EditorLinePainter() {

    override fun getLineExtensions(
        project: Project,
        virtualFile: VirtualFile,
        lineNumber: Int
    ): Collection<LineExtensionInfo>? {
        if (virtualFile.fileType != DbcFileType.INSTANCE) return null

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return null
        val document = com.intellij.psi.PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return null

        if (lineNumber >= document.lineCount) return null

        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val lineEndOffset = document.getLineEndOffset(lineNumber)

        // Find signal definition on this line
        var offset = lineStartOffset
        var signalDef: DbcSignalDef? = null
        while (offset < lineEndOffset) {
            val element = psiFile.findElementAt(offset)
            if (element != null) {
                val signal = PsiTreeUtil.getParentOfType(element, DbcSignalDef::class.java)
                if (signal != null && signal.textRange.startOffset >= lineStartOffset) {
                    signalDef = signal
                    break
                }
                offset = element.textRange.endOffset
            } else {
                offset++
            }
        }

        if (signalDef == null) return null

        val signalName = signalDef.signalName ?: return null
        val parentMessage = signalDef.parent as? DbcMessageDef ?: return null
        val messageId = parentMessage.messageId ?: return null

        val parts = mutableListOf<String>()

        // Look up comment for this signal
        val comment = findSignalComment(psiFile, messageId, signalName)
        if (comment != null) {
            // Truncate long comments
            val truncated = if (comment.length > 80) comment.substring(0, 77) + "..." else comment
            parts.add(truncated)
        }

        // Look up value descriptions
        val valueDescs = findSignalValueDescriptions(psiFile, messageId, signalName)
        if (valueDescs.isNotEmpty()) {
            val valStr = valueDescs.joinToString(", ") { "${it.first}=\"${it.second}\"" }
            val truncated = if (valStr.length > 100) valStr.substring(0, 97) + "..." else valStr
            parts.add(truncated)
        }

        if (parts.isEmpty()) return null

        val text = "  // " + parts.joinToString(" | ")
        val color = getHintColor()

        return listOf(LineExtensionInfo(text, color, null, null, Font.ITALIC))
    }

    private fun getHintColor(): Color {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val commentAttrs = scheme.getAttributes(
            com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LINE_COMMENT
        )
        return commentAttrs?.foregroundColor ?: Color(128, 128, 128)
    }

    private fun findSignalComment(psiFile: com.intellij.psi.PsiFile, messageId: Long, signalName: String): String? {
        val comments = PsiTreeUtil.findChildrenOfType(psiFile, DbcCommentDef::class.java)
        return comments.find { it.commentType == "SG_" && it.targetId == messageId && it.targetName == signalName }
            ?.commentText
    }

    private fun findSignalValueDescriptions(
        psiFile: com.intellij.psi.PsiFile,
        messageId: Long,
        signalName: String
    ): List<Pair<Long, String>> {
        val valDescs = PsiTreeUtil.findChildrenOfType(psiFile, DbcValueDescriptionDef::class.java)
        return valDescs.find { it.messageId == messageId && it.signalName == signalName }
            ?.entries ?: emptyList()
    }
}
