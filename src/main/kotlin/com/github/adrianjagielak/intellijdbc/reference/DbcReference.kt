package com.github.adrianjagielak.intellijdbc.reference

import com.github.adrianjagielak.intellijdbc.DbcFileType
import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

class DbcNodeReference(
    element: PsiElement,
    private val rangeInElement: TextRange
) : PsiReferenceBase<PsiElement>(element, rangeInElement), PsiPolyVariantReference {

    private val referenceName: String
        get() = element.text.substring(rangeInElement.startOffset, rangeInElement.endOffset)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        val results = mutableListOf<ResolveResult>()

        FileTypeIndex.processFiles(DbcFileType.INSTANCE, { virtualFile ->
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile != null) {
                PsiTreeUtil.findChildrenOfType(psiFile, DbcNodeName::class.java).forEach { node ->
                    if (node.name == referenceName) {
                        results.add(PsiElementResolveResult(node))
                    }
                }
            }
            true
        }, GlobalSearchScope.allScope(project))

        return results.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results[0].element else null
    }

    override fun getVariants(): Array<Any> {
        val project = element.project
        val variants = mutableListOf<Any>()

        FileTypeIndex.processFiles(DbcFileType.INSTANCE, { virtualFile ->
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile != null) {
                PsiTreeUtil.findChildrenOfType(psiFile, DbcNodeName::class.java).forEach { node ->
                    node.name?.let { variants.add(it) }
                }
            }
            true
        }, GlobalSearchScope.allScope(project))

        return variants.toTypedArray()
    }
}

class DbcMessageReference(
    element: PsiElement,
    private val rangeInElement: TextRange
) : PsiReferenceBase<PsiElement>(element, rangeInElement), PsiPolyVariantReference {

    private val referenceId: Long?
        get() = element.text.substring(rangeInElement.startOffset, rangeInElement.endOffset).toLongOrNull()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val id = referenceId ?: return ResolveResult.EMPTY_ARRAY
        val project = element.project
        val results = mutableListOf<ResolveResult>()

        FileTypeIndex.processFiles(DbcFileType.INSTANCE, { virtualFile ->
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile != null) {
                PsiTreeUtil.findChildrenOfType(psiFile, DbcMessageDef::class.java).forEach { msg ->
                    if (msg.messageId == id) {
                        results.add(PsiElementResolveResult(msg))
                    }
                }
            }
            true
        }, GlobalSearchScope.allScope(project))

        return results.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results[0].element else null
    }

    override fun getVariants(): Array<Any> = emptyArray()
}

class DbcSignalReference(
    element: PsiElement,
    private val rangeInElement: TextRange,
    private val messageId: Long?
) : PsiReferenceBase<PsiElement>(element, rangeInElement), PsiPolyVariantReference {

    private val referenceName: String
        get() = element.text.substring(rangeInElement.startOffset, rangeInElement.endOffset)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        val results = mutableListOf<ResolveResult>()

        FileTypeIndex.processFiles(DbcFileType.INSTANCE, { virtualFile ->
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile != null) {
                PsiTreeUtil.findChildrenOfType(psiFile, DbcMessageDef::class.java).forEach { msg ->
                    if (messageId == null || msg.messageId == messageId) {
                        msg.signals.forEach { sig ->
                            if (sig.signalName == referenceName) {
                                results.add(PsiElementResolveResult(sig))
                            }
                        }
                    }
                }
            }
            true
        }, GlobalSearchScope.allScope(project))

        return results.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results[0].element else null
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
