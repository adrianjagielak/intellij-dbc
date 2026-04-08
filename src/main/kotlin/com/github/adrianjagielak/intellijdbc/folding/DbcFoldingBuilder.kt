package com.github.adrianjagielak.intellijdbc.folding

import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class DbcFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        // Fold message definitions (BO_ ... signals) — always foldable if multi-line
        PsiTreeUtil.findChildrenOfType(root, DbcMessageDef::class.java).forEach { message ->
            val range = message.textRange
            if (document.getLineNumber(range.startOffset) < document.getLineNumber(range.endOffset)) {
                descriptors.add(FoldingDescriptor(message.node, range))
            }
        }

        // Skip expensive consecutive-block folding in quick mode (initial file open)
        if (!quick) {
            // O(n) line-based grouping instead of O(n*m) sibling walking
            foldConsecutiveByLine<DbcCommentDef>(root, document, descriptors)
            foldConsecutiveByLine<DbcAttributeDefinitionDef>(root, document, descriptors)
            foldConsecutiveByLine<DbcAttributeDefaultDef>(root, document, descriptors)
            foldConsecutiveByLine<DbcAttributeValueDef>(root, document, descriptors)
            foldConsecutiveByLine<DbcValueDescriptionDef>(root, document, descriptors)
        }

        // Fold nodes definition
        PsiTreeUtil.findChildrenOfType(root, DbcNodesDef::class.java).forEach { nodes ->
            if (nodes.nodeNames.size > 5 && nodes.textRange.length > 1) {
                descriptors.add(FoldingDescriptor(nodes.node, nodes.textRange))
            }
        }

        return descriptors.toTypedArray()
    }

    private inline fun <reified T : PsiElement> foldConsecutiveByLine(
        root: PsiElement,
        document: Document,
        descriptors: MutableList<FoldingDescriptor>
    ) {
        val elements = PsiTreeUtil.findChildrenOfType(root, T::class.java).toList()
        if (elements.size < 3) return

        var groupStart = 0
        for (i in 1..elements.size) {
            val consecutive = if (i < elements.size) {
                val prevEndLine = document.getLineNumber(elements[i - 1].textRange.endOffset)
                val currStartLine = document.getLineNumber(elements[i].textRange.startOffset)
                currStartLine - prevEndLine <= 1
            } else false

            if (!consecutive) {
                if (i - groupStart >= 3) {
                    val range = TextRange(
                        elements[groupStart].textRange.startOffset,
                        elements[i - 1].textRange.endOffset
                    )
                    descriptors.add(FoldingDescriptor(elements[groupStart].node, range))
                }
                groupStart = i
            }
        }
    }

    override fun getPlaceholderText(node: ASTNode): String {
        val psi = node.psi
        return when (psi) {
            is DbcMessageDef -> {
                val name = psi.messageName ?: "?"
                val id = psi.messageId?.let { "0x%X".format(it) } ?: "?"
                "BO_ $id $name: ... (${psi.signals.size} signals)"
            }
            is DbcCommentDef -> "CM_ ..."
            is DbcAttributeDefinitionDef -> "BA_DEF_ ..."
            is DbcAttributeDefaultDef -> "BA_DEF_DEF_ ..."
            is DbcAttributeValueDef -> "BA_ ..."
            is DbcValueDescriptionDef -> "VAL_ ..."
            is DbcNodesDef -> {
                val count = psi.nodeNames.size
                "BU_: ... ($count nodes)"
            }
            else -> "..."
        }
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
