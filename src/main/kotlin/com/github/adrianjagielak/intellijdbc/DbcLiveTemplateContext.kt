package com.github.adrianjagielak.intellijdbc

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

@Suppress("deprecation")
class DbcLiveTemplateContext : TemplateContextType("DBC", "DBC (CAN Database)") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        val file = templateActionContext.file
        return file.name.endsWith(".dbc")
    }
}
