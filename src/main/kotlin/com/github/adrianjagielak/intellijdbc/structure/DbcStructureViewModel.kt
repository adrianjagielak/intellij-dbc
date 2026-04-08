package com.github.adrianjagielak.intellijdbc.structure

import com.github.adrianjagielak.intellijdbc.psi.*
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcEnvironmentVariableDefImpl
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcMessageDefImpl
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcNodeNameImpl
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcSignalDefImpl
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcValueTableDefImpl
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class DbcStructureViewModel(psiFile: PsiFile, editor: Editor?) :
    StructureViewModelBase(psiFile, editor, DbcStructureViewElement(psiFile)),
    StructureViewModel.ElementInfoProvider {

    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        return element.value is DbcMessageDef
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is DbcSignalDef || value is DbcNodeName ||
               value is DbcValueTableDef || value is DbcEnvironmentVariableDef
    }

    override fun getSuitableClasses(): Array<Class<*>> = arrayOf(
        DbcMessageDefImpl::class.java,
        DbcSignalDefImpl::class.java,
        DbcNodeNameImpl::class.java,
        DbcValueTableDefImpl::class.java,
        DbcEnvironmentVariableDefImpl::class.java,
    )
}
