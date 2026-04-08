package com.github.adrianjagielak.intellijdbc.navigation

import com.github.adrianjagielak.intellijdbc.DbcFileType
import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter

class DbcChooseByNameContributor : ChooseByNameContributorEx {

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        val project = scope.project ?: return
        FileTypeIndex.processFiles(DbcFileType.INSTANCE, { virtualFile ->
            val psiFile = com.intellij.psi.PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile != null) {
                // Messages
                PsiTreeUtil.findChildrenOfType(psiFile, DbcMessageDef::class.java).forEach { msg ->
                    msg.messageName?.let { processor.process(it) }
                }
                // Signals
                PsiTreeUtil.findChildrenOfType(psiFile, DbcSignalDef::class.java).forEach { sig ->
                    sig.signalName?.let { processor.process(it) }
                }
                // Nodes
                PsiTreeUtil.findChildrenOfType(psiFile, DbcNodeName::class.java).forEach { node ->
                    node.name?.let { processor.process(it) }
                }
                // Value tables
                PsiTreeUtil.findChildrenOfType(psiFile, DbcValueTableDef::class.java).forEach { vt ->
                    vt.tableName?.let { processor.process(it) }
                }
                // Environment variables
                PsiTreeUtil.findChildrenOfType(psiFile, DbcEnvironmentVariableDef::class.java).forEach { ev ->
                    ev.envVarName?.let { processor.process(it) }
                }
            }
            true
        }, scope)
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters
    ) {
        val scope = parameters.searchScope
        val project = scope.project ?: return
        FileTypeIndex.processFiles(DbcFileType.INSTANCE, { virtualFile ->
            val psiFile = com.intellij.psi.PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile != null) {
                PsiTreeUtil.findChildrenOfType(psiFile, DbcMessageDef::class.java).forEach { msg ->
                    if (msg.messageName == name) processor.process(msg as NavigationItem)
                }
                PsiTreeUtil.findChildrenOfType(psiFile, DbcSignalDef::class.java).forEach { sig ->
                    if (sig.signalName == name) processor.process(sig as NavigationItem)
                }
                PsiTreeUtil.findChildrenOfType(psiFile, DbcNodeName::class.java).forEach { node ->
                    if (node.name == name) processor.process(node as NavigationItem)
                }
                PsiTreeUtil.findChildrenOfType(psiFile, DbcValueTableDef::class.java).forEach { vt ->
                    if (vt.tableName == name) processor.process(vt as NavigationItem)
                }
                PsiTreeUtil.findChildrenOfType(psiFile, DbcEnvironmentVariableDef::class.java).forEach { ev ->
                    if (ev.envVarName == name) processor.process(ev as NavigationItem)
                }
            }
            true
        }, scope)
    }
}
