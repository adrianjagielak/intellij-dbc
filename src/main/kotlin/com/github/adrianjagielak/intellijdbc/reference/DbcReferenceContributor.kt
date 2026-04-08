package com.github.adrianjagielak.intellijdbc.reference

import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.github.adrianjagielak.intellijdbc.psi.*
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcReceiverNameImpl
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcTransmitterNameImpl
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class DbcReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // References from transmitter/receiver nodes to BU_ node definitions
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(DbcTransmitterNameImpl::class.java),
            NodeReferenceProvider()
        )
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(DbcReceiverNameImpl::class.java),
            NodeReferenceProvider()
        )
    }

    private class NodeReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
            val text = element.text
            if (text.isBlank()) return PsiReference.EMPTY_ARRAY
            return arrayOf(DbcNodeReference(element, TextRange(0, text.length)))
        }
    }
}
