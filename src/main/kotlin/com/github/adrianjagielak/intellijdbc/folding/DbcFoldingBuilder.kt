package com.github.adrianjagielak.intellijdbc.folding

import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class DbcFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        // Fold message definitions (BO_ ... signals)
        PsiTreeUtil.findChildrenOfType(root, DbcMessageDef::class.java).forEach { message ->
            if (message.signals.isNotEmpty() && message.textRange.length > 1) {
                descriptors.add(FoldingDescriptor(
                    message.node,
                    message.textRange,
                    FoldingGroup.newGroup("message")
                ))
            }
        }

        // Fold comment blocks (consecutive CM_ entries)
        foldConsecutiveElements<DbcCommentDef>(root, descriptors, "comments")

        // Fold attribute definition blocks
        foldConsecutiveElements<DbcAttributeDefinitionDef>(root, descriptors, "attribute_defs")

        // Fold attribute default blocks
        foldConsecutiveElements<DbcAttributeDefaultDef>(root, descriptors, "attribute_defaults")

        // Fold attribute value blocks
        foldConsecutiveElements<DbcAttributeValueDef>(root, descriptors, "attribute_values")

        // Fold value description blocks
        foldConsecutiveElements<DbcValueDescriptionDef>(root, descriptors, "value_descriptions")

        // Fold nodes definition
        PsiTreeUtil.findChildrenOfType(root, DbcNodesDef::class.java).forEach { nodes ->
            if (nodes.nodeNames.size > 5 && nodes.textRange.length > 1) {
                descriptors.add(FoldingDescriptor(nodes.node, nodes.textRange))
            }
        }

        return descriptors.toTypedArray()
    }

    private inline fun <reified T : PsiElement> foldConsecutiveElements(
        root: PsiElement,
        descriptors: MutableList<FoldingDescriptor>,
        groupName: String
    ) {
        val elements = PsiTreeUtil.findChildrenOfType(root, T::class.java).toList()
        if (elements.size < 3) return

        var startIdx = 0
        while (startIdx < elements.size) {
            var endIdx = startIdx
            // Find consecutive elements (allowing whitespace between them)
            while (endIdx + 1 < elements.size) {
                val current = elements[endIdx]
                val next = elements[endIdx + 1]
                // Check if they're adjacent (no other significant elements between them)
                var sibling = current.nextSibling
                var onlyWhitespace = true
                while (sibling != null && sibling != next) {
                    if (sibling !is com.intellij.psi.PsiWhiteSpace &&
                        sibling !is com.intellij.psi.PsiComment) {
                        onlyWhitespace = false
                        break
                    }
                    sibling = sibling.nextSibling
                }
                if (onlyWhitespace) {
                    endIdx++
                } else {
                    break
                }
            }

            if (endIdx > startIdx + 1) {
                val range = TextRange(elements[startIdx].textRange.startOffset, elements[endIdx].textRange.endOffset)
                descriptors.add(FoldingDescriptor(
                    elements[startIdx].node,
                    range,
                    FoldingGroup.newGroup(groupName)
                ))
            }
            startIdx = endIdx + 1
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
