package com.github.adrianjagielak.intellijdbc.completion

import com.github.adrianjagielak.intellijdbc.DbcFileType
import com.github.adrianjagielak.intellijdbc.DbcIcons
import com.github.adrianjagielak.intellijdbc.DbcLanguage
import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

class DbcCompletionContributor : CompletionContributor() {

    init {
        // Keyword completion at top level
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(DbcLanguage.INSTANCE),
            KeywordCompletionProvider()
        )
    }

    private class KeywordCompletionProvider : CompletionProvider<CompletionParameters>() {

        private val topLevelKeywords = listOf(
            "VERSION", "NS_", "BS_", "BU_", "BO_", "SG_", "CM_",
            "BA_DEF_", "BA_DEF_DEF_", "BA_", "VAL_", "VAL_TABLE_",
            "SIG_GROUP_", "BO_TX_BU_", "EV_", "SG_MUL_VAL_",
            "SIG_VALTYPE_", "ENVVAR_DATA_"
        )

        private val typeKeywords = listOf("INT", "FLOAT", "STRING", "HEX", "ENUM")

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val position = parameters.position
            val project = position.project

            // Gather all defined nodes for receiver/transmitter completion
            val nodes = mutableSetOf<String>()
            val messages = mutableListOf<DbcMessageDef>()
            val signals = mutableListOf<DbcSignalDef>()
            val attributeNames = mutableSetOf<String>()

            FileTypeIndex.processFiles(DbcFileType.INSTANCE, { virtualFile ->
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile != null) {
                    PsiTreeUtil.findChildrenOfType(psiFile, DbcNodeName::class.java).forEach { node ->
                        node.name?.let { nodes.add(it) }
                    }
                    PsiTreeUtil.findChildrenOfType(psiFile, DbcMessageDef::class.java).forEach { msg ->
                        messages.add(msg)
                    }
                    PsiTreeUtil.findChildrenOfType(psiFile, DbcSignalDef::class.java).forEach { sig ->
                        signals.add(sig)
                    }
                    PsiTreeUtil.findChildrenOfType(psiFile, DbcAttributeDefinitionDef::class.java).forEach { attr ->
                        attr.attributeName?.let { attributeNames.add(it) }
                    }
                }
                true
            }, GlobalSearchScope.allScope(project))

            // Add top-level keyword completions
            for (keyword in topLevelKeywords) {
                result.addElement(
                    LookupElementBuilder.create(keyword)
                        .bold()
                        .withTypeText("keyword")
                )
            }

            // Add type keyword completions
            for (keyword in typeKeywords) {
                result.addElement(
                    LookupElementBuilder.create(keyword)
                        .bold()
                        .withTypeText("type")
                )
            }

            // Add node name completions
            for (nodeName in nodes) {
                result.addElement(
                    LookupElementBuilder.create(nodeName)
                        .withIcon(DbcIcons.NODE)
                        .withTypeText("node")
                )
            }

            // Add message name completions
            for (msg in messages) {
                val name = msg.messageName ?: continue
                val id = msg.messageId?.let { "0x%X".format(it) } ?: "?"
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withIcon(DbcIcons.MESSAGE)
                        .withTypeText("message [$id]")
                        .withTailText(" DLC=${msg.dlc}", true)
                )
            }

            // Add signal name completions
            for (sig in signals) {
                val name = sig.signalName ?: continue
                val parent = sig.parent
                val msgName = if (parent is DbcMessageDef) parent.messageName else null
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withIcon(DbcIcons.SIGNAL)
                        .withTypeText("signal")
                        .withTailText(if (msgName != null) " in $msgName" else "", true)
                )
            }

            // Add attribute name completions (as strings)
            for (attrName in attributeNames) {
                result.addElement(
                    LookupElementBuilder.create("\"$attrName\"")
                        .withPresentableText(attrName)
                        .withIcon(DbcIcons.ATTRIBUTE)
                        .withTypeText("attribute")
                )
            }

            // Add common special identifiers
            result.addElement(
                LookupElementBuilder.create("Vector__XXX")
                    .withTypeText("default transmitter")
            )
        }
    }
}
