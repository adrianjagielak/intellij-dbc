package com.github.adrianjagielak.intellijdbc.highlighting

import com.github.adrianjagielak.intellijdbc.DbcFileType
import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.openapi.editor.EditorLinePainter
import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
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

        // Quick check: signal lines start with whitespace + "SG_"
        val lineText = document.charsSequence.subSequence(lineStartOffset, lineEndOffset)
        if (!lineText.contains("SG_")) return null

        // Find signal definition on this line
        val element = psiFile.findElementAt(lineStartOffset) ?: return null
        var signalDef: DbcSignalDef? = null
        // Walk from line start to find the signal PSI element
        var current = element
        while (current != null && current.textRange.startOffset < lineEndOffset) {
            val signal = PsiTreeUtil.getParentOfType(current, DbcSignalDef::class.java)
            if (signal != null && signal.textRange.startOffset >= lineStartOffset) {
                signalDef = signal
                break
            }
            val next = PsiTreeUtil.nextLeaf(current)
            if (next == null || next.textRange.startOffset >= lineEndOffset) break
            current = next
        }

        if (signalDef == null) return null

        val signalName = signalDef.signalName ?: return null
        val parentMessage = signalDef.parent as? DbcMessageDef ?: return null
        val messageId = parentMessage.messageId ?: return null

        // Use cached lookup maps (rebuilt only when file changes)
        val cache = getOrBuildCache(psiFile)
        val key = "$messageId:$signalName"

        val comment = cache.signalComments[key]
        val valueDescs = cache.signalValues[key]

        if (comment == null && (valueDescs == null || valueDescs.isEmpty())) return null

        val sb = StringBuilder("  //")

        // Comment first
        if (comment != null) {
            val singleLine = comment.replace('\n', ' ').replace("  ", " ").trim()
            val truncated = if (singleLine.length > 60) singleLine.substring(0, 57) + "..." else singleLine
            sb.append(" ").append(truncated)
        }

        // Values: show as {IDLE, PREPARE, ENCRYPT_BEGIN, ...} — names only, no numeric keys
        if (valueDescs != null && valueDescs.isNotEmpty()) {
            if (comment != null) sb.append("  ")
            sb.append("{")
            val maxShow = 5
            val names = valueDescs.map { it.second }
            sb.append(names.take(maxShow).joinToString(", "))
            if (names.size > maxShow) sb.append(", ...")
            sb.append("}")
        }

        val text = sb.toString()
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

    private fun getOrBuildCache(psiFile: PsiFile): SignalInfoCache {
        return CachedValuesManager.getCachedValue(psiFile) {
            CachedValueProvider.Result.create(
                buildCache(psiFile),
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

    private fun buildCache(psiFile: PsiFile): SignalInfoCache {
        val signalComments = mutableMapOf<String, String>()
        val signalValues = mutableMapOf<String, List<Pair<Long, String>>>()

        // Single pass: collect all signal comments
        PsiTreeUtil.findChildrenOfType(psiFile, DbcCommentDef::class.java).forEach { cm ->
            if (cm.commentType == "SG_") {
                val id = cm.targetId ?: return@forEach
                val name = cm.targetName ?: return@forEach
                val text = cm.commentText ?: return@forEach
                signalComments["$id:$name"] = text
            }
        }

        // Single pass: collect all value descriptions
        PsiTreeUtil.findChildrenOfType(psiFile, DbcValueDescriptionDef::class.java).forEach { vd ->
            val id = vd.messageId ?: return@forEach
            val name = vd.signalName ?: return@forEach
            val entries = vd.entries
            if (entries.isNotEmpty()) {
                signalValues["$id:$name"] = entries
            }
        }

        return SignalInfoCache(signalComments, signalValues)
    }

    private data class SignalInfoCache(
        val signalComments: Map<String, String>,
        val signalValues: Map<String, List<Pair<Long, String>>>
    )
}
