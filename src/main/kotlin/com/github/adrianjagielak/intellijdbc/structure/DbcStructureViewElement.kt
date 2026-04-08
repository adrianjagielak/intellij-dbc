package com.github.adrianjagielak.intellijdbc.structure

import com.github.adrianjagielak.intellijdbc.DbcFile
import com.github.adrianjagielak.intellijdbc.DbcIcons
import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class DbcStructureViewElement(private val myElement: NavigatablePsiElement) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = myElement

    override fun navigate(requestFocus: Boolean) = myElement.navigate(requestFocus)

    override fun canNavigate(): Boolean = myElement.canNavigate()

    override fun canNavigateToSource(): Boolean = myElement.canNavigateToSource()

    override fun getAlphaSortKey(): String {
        return when (myElement) {
            is DbcMessageDef -> {
                val id = myElement.messageId ?: 0
                "%08d_%s".format(id, myElement.messageName ?: "")
            }
            is DbcSignalDef -> myElement.signalName ?: ""
            is DbcNodeName -> (myElement as DbcNamedElement).name ?: ""
            is DbcValueTableDef -> myElement.tableName ?: ""
            is DbcEnvironmentVariableDef -> myElement.envVarName ?: ""
            else -> myElement.text ?: ""
        }
    }

    override fun getPresentation(): ItemPresentation {
        return when (myElement) {
            is DbcFile -> PresentationData(myElement.name, null, DbcIcons.FILE, null)
            is DbcMessageDef -> {
                val id = myElement.messageId
                val name = myElement.messageName ?: "?"
                val dlc = myElement.dlc
                val idStr = if (id != null) "0x%X (%d)".format(id, id) else "?"
                PresentationData("$name [$idStr] DLC=$dlc", myElement.transmitterNode, DbcIcons.MESSAGE, null)
            }
            is DbcSignalDef -> {
                val name = myElement.signalName ?: "?"
                val bits = "${myElement.startBit ?: "?"}|${myElement.bitLength ?: "?"}"
                val unit = myElement.unit
                val mux = myElement.muxIndicator?.let { " $it" } ?: ""
                val unitStr = if (!unit.isNullOrEmpty()) " [$unit]" else ""
                PresentationData("$name$mux ($bits)$unitStr", null, DbcIcons.SIGNAL, null)
            }
            is DbcNodeName -> PresentationData((myElement as DbcNamedElement).name ?: "?", null, DbcIcons.NODE, null)
            is DbcValueTableDef -> PresentationData(myElement.tableName ?: "?", null, DbcIcons.VALUE_TABLE, null)
            is DbcEnvironmentVariableDef -> PresentationData(myElement.envVarName ?: "?", null, DbcIcons.ENV_VAR, null)
            else -> PresentationData(myElement.text, null, null, null)
        }
    }

    override fun getChildren(): Array<TreeElement> {
        if (myElement is DbcFile) {
            return buildFileChildren(myElement)
        }
        if (myElement is DbcMessageDef) {
            return myElement.signals.map { DbcStructureViewElement(it as NavigatablePsiElement) }.toTypedArray()
        }
        return TreeElement.EMPTY_ARRAY
    }

    private fun buildFileChildren(file: DbcFile): Array<TreeElement> {
        val children = mutableListOf<TreeElement>()

        // Add nodes
        val nodesDefs = PsiTreeUtil.getChildrenOfType(file, DbcNodesDef::class.java)
        nodesDefs?.forEach { nodesDef ->
            nodesDef.nodeNames.forEach { nodeName ->
                children.add(DbcStructureViewElement(nodeName as NavigatablePsiElement))
            }
        }

        // Add messages
        val messages = PsiTreeUtil.getChildrenOfType(file, DbcMessageDef::class.java)
        messages?.forEach { children.add(DbcStructureViewElement(it as NavigatablePsiElement)) }

        // Add value tables
        val valueTables = PsiTreeUtil.getChildrenOfType(file, DbcValueTableDef::class.java)
        valueTables?.forEach { children.add(DbcStructureViewElement(it as NavigatablePsiElement)) }

        // Add environment variables
        val envVars = PsiTreeUtil.getChildrenOfType(file, DbcEnvironmentVariableDef::class.java)
        envVars?.forEach { children.add(DbcStructureViewElement(it as NavigatablePsiElement)) }

        return children.toTypedArray()
    }
}
