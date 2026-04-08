package com.github.adrianjagielak.intellijdbc.psi.impl

import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.github.adrianjagielak.intellijdbc.psi.DbcNamedElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement

abstract class DbcNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcNamedElement {

    override fun getNameIdentifier(): PsiElement? {
        val childNode = node.findChildByType(DbcTokenTypes.IDENTIFIER) ?: return null
        return childNode.psi
    }

    override fun getName(): String? {
        return nameIdentifier?.text
    }

    override fun setName(name: String): PsiElement {
        val nameNode = nameIdentifier?.node ?: return this
        val newNode = LeafPsiElement(DbcTokenTypes.IDENTIFIER, name)
        node.replaceChild(nameNode, newNode.node)
        return this
    }

    override fun getTextOffset(): Int {
        return nameIdentifier?.textOffset ?: super.getTextOffset()
    }
}
