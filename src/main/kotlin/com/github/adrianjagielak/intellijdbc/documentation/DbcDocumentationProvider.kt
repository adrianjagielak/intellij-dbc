package com.github.adrianjagielak.intellijdbc.documentation

import com.github.adrianjagielak.intellijdbc.DbcFileType
import com.github.adrianjagielak.intellijdbc.psi.*
import com.github.adrianjagielak.intellijdbc.psi.impl.DbcNodeNameImpl
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

class DbcDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        return when (element) {
            is DbcMessageDef -> generateMessageDoc(element)
            is DbcSignalDef -> generateSignalDoc(element)
            is DbcNodeNameImpl -> generateNodeDoc(element)
            is DbcValueTableDef -> generateValueTableDoc(element)
            is DbcEnvironmentVariableDef -> generateEnvVarDoc(element)
            else -> null
        }
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is DbcMessageDef -> {
                val id = element.messageId?.let { "0x%X (%d)".format(it, it) } ?: "?"
                "Message: ${element.messageName} [ID=$id, DLC=${element.dlc}]"
            }
            is DbcSignalDef -> {
                val bits = "${element.startBit}|${element.bitLength}"
                "Signal: ${element.signalName} ($bits) [${element.unit}]"
            }
            is DbcNodeNameImpl -> "Node: ${element.name}"
            else -> null
        }
    }

    private fun generateMessageDoc(msg: DbcMessageDef): String {
        val sb = StringBuilder()
        sb.append("<html><body>")
        sb.append("<h3>Message: ${escHtml(msg.messageName ?: "?")}</h3>")
        sb.append("<table>")
        sb.append("<tr><td><b>ID:</b></td><td>${msg.messageId?.let { "0x%X (%d)".format(it, it) } ?: "?"}</td></tr>")
        sb.append("<tr><td><b>DLC:</b></td><td>${msg.dlc ?: "?"} bytes</td></tr>")
        sb.append("<tr><td><b>Transmitter:</b></td><td>${escHtml(msg.transmitterNode ?: "?")}</td></tr>")

        val isExtended = (msg.messageId ?: 0) and 0x80000000L != 0L
        sb.append("<tr><td><b>Frame type:</b></td><td>${if (isExtended) "Extended (29-bit)" else "Standard (11-bit)"}</td></tr>")

        sb.append("</table>")

        // Find comment for this message
        val comment = findMessageComment(msg)
        if (comment != null) {
            sb.append("<p><i>${escHtml(comment)}</i></p>")
        }

        // List signals
        val signals = msg.signals
        if (signals.isNotEmpty()) {
            sb.append("<h4>Signals (${signals.size}):</h4>")
            sb.append("<table border='1' cellpadding='4' cellspacing='0'>")
            sb.append("<tr><th>Name</th><th>Bit Pos</th><th>Length</th><th>Factor</th><th>Offset</th><th>Range</th><th>Unit</th></tr>")
            for (sig in signals) {
                val mux = sig.muxIndicator?.let { " <b>$it</b>" } ?: ""
                sb.append("<tr>")
                sb.append("<td>${escHtml(sig.signalName ?: "?")}$mux</td>")
                sb.append("<td>${sig.startBit ?: "?"}</td>")
                sb.append("<td>${sig.bitLength ?: "?"}</td>")
                sb.append("<td>${sig.factor ?: "?"}</td>")
                sb.append("<td>${sig.offset ?: "?"}</td>")
                sb.append("<td>[${sig.minimum ?: "?"}..${sig.maximum ?: "?"}]</td>")
                sb.append("<td>${escHtml(sig.unit ?: "")}</td>")
                sb.append("</tr>")
            }
            sb.append("</table>")
        }

        sb.append("</body></html>")
        return sb.toString()
    }

    private fun generateSignalDoc(sig: DbcSignalDef): String {
        val sb = StringBuilder()
        sb.append("<html><body>")
        sb.append("<h3>Signal: ${escHtml(sig.signalName ?: "?")}</h3>")
        sb.append("<table>")
        sb.append("<tr><td><b>Start bit:</b></td><td>${sig.startBit ?: "?"}</td></tr>")
        sb.append("<tr><td><b>Length:</b></td><td>${sig.bitLength ?: "?"} bits</td></tr>")
        sb.append("<tr><td><b>Byte order:</b></td><td>${if (sig.byteOrder == 1) "Intel (Little Endian)" else "Motorola (Big Endian)"}</td></tr>")
        sb.append("<tr><td><b>Value type:</b></td><td>${if (sig.valueType == 0) "Unsigned" else "Signed"}</td></tr>")
        sb.append("<tr><td><b>Factor:</b></td><td>${sig.factor ?: "?"}</td></tr>")
        sb.append("<tr><td><b>Offset:</b></td><td>${sig.offset ?: "?"}</td></tr>")
        sb.append("<tr><td><b>Range:</b></td><td>[${sig.minimum ?: "?"} .. ${sig.maximum ?: "?"}]</td></tr>")
        sb.append("<tr><td><b>Unit:</b></td><td>${escHtml(sig.unit ?: "")}</td></tr>")

        val receivers = sig.receiverNodes
        if (receivers.isNotEmpty()) {
            sb.append("<tr><td><b>Receivers:</b></td><td>${receivers.joinToString(", ") { escHtml(it) }}</td></tr>")
        }

        val mux = sig.muxIndicator
        if (mux != null) {
            sb.append("<tr><td><b>Multiplexer:</b></td><td>${escHtml(mux)}")
            when {
                mux == "M" -> sb.append(" (multiplex switch)")
                mux.startsWith("m") -> sb.append(" (multiplexed signal)")
            }
            sb.append("</td></tr>")
        }

        sb.append("</table>")

        // Parent message info
        val parentMsg = sig.parent as? DbcMessageDef
        if (parentMsg != null) {
            sb.append("<p>In message: <b>${escHtml(parentMsg.messageName ?: "?")}</b> [0x${parentMsg.messageId?.let { "%X".format(it) } ?: "?"}]</p>")
        }

        // Physical value formula
        val factor = sig.factor
        val offset = sig.offset
        if (factor != null && offset != null) {
            sb.append("<p><b>Physical value:</b> raw_value × $factor + $offset ${escHtml(sig.unit ?: "")}</p>")
        }

        // Find comment
        val comment = findSignalComment(sig)
        if (comment != null) {
            sb.append("<p><i>${escHtml(comment)}</i></p>")
        }

        // Find value descriptions
        val valueDescs = findSignalValueDescriptions(sig)
        if (valueDescs.isNotEmpty()) {
            sb.append("<h4>Value Descriptions:</h4>")
            sb.append("<table border='1' cellpadding='3' cellspacing='0'>")
            sb.append("<tr><th>Value</th><th>Description</th></tr>")
            for ((value, desc) in valueDescs) {
                sb.append("<tr><td>$value</td><td>${escHtml(desc)}</td></tr>")
            }
            sb.append("</table>")
        }

        sb.append("</body></html>")
        return sb.toString()
    }

    private fun generateNodeDoc(node: DbcNodeNameImpl): String {
        val sb = StringBuilder()
        val name = node.name ?: "?"
        sb.append("<html><body>")
        sb.append("<h3>Node: ${escHtml(name)}</h3>")

        // Find comment
        val file = node.containingFile

        val comments = PsiTreeUtil.findChildrenOfType(file, DbcCommentDef::class.java)
        val comment = comments.find { it.commentType == "BU_" && it.targetName == name }
        if (comment != null) {
            sb.append("<p><i>${escHtml(comment.commentText ?: "")}</i></p>")
        }

        // Find messages transmitted by this node
        val txMessages = PsiTreeUtil.findChildrenOfType(file, DbcMessageDef::class.java)
            .filter { it.transmitterNode == name }
        if (txMessages.isNotEmpty()) {
            sb.append("<h4>Transmitted Messages (${txMessages.size}):</h4><ul>")
            for (msg in txMessages) {
                val id = msg.messageId?.let { "0x%X".format(it) } ?: "?"
                sb.append("<li>${escHtml(msg.messageName ?: "?")} [$id]</li>")
            }
            sb.append("</ul>")
        }

        // Find messages received by this node
        val rxSignals = PsiTreeUtil.findChildrenOfType(file, DbcSignalDef::class.java)
            .filter { name in it.receiverNodes }
        if (rxSignals.isNotEmpty()) {
            val rxMsgIds = rxSignals.mapNotNull { (it.parent as? DbcMessageDef)?.messageName }.distinct()
            sb.append("<h4>Receives from Messages:</h4><ul>")
            for (msgName in rxMsgIds) {
                sb.append("<li>${escHtml(msgName)}</li>")
            }
            sb.append("</ul>")
        }

        sb.append("</body></html>")
        return sb.toString()
    }

    private fun generateValueTableDoc(vt: DbcValueTableDef): String {
        val sb = StringBuilder()
        sb.append("<html><body>")
        sb.append("<h3>Value Table: ${escHtml(vt.tableName ?: "?")}</h3>")
        val entries = vt.entries
        if (entries.isNotEmpty()) {
            sb.append("<table border='1' cellpadding='3' cellspacing='0'>")
            sb.append("<tr><th>Value</th><th>Description</th></tr>")
            for ((value, desc) in entries) {
                sb.append("<tr><td>$value</td><td>${escHtml(desc)}</td></tr>")
            }
            sb.append("</table>")
        }
        sb.append("</body></html>")
        return sb.toString()
    }

    private fun generateEnvVarDoc(ev: DbcEnvironmentVariableDef): String {
        val sb = StringBuilder()
        sb.append("<html><body>")
        sb.append("<h3>Environment Variable: ${escHtml(ev.envVarName ?: "?")}</h3>")
        sb.append("<p>Type: ${ev.envVarType ?: "?"}</p>")
        sb.append("</body></html>")
        return sb.toString()
    }

    private fun findMessageComment(msg: DbcMessageDef): String? {
        val id = msg.messageId ?: return null
        val file = msg.containingFile ?: return null
        return PsiTreeUtil.findChildrenOfType(file, DbcCommentDef::class.java)
            .find { it.commentType == "BO_" && it.targetId == id }
            ?.commentText
    }

    private fun findSignalComment(sig: DbcSignalDef): String? {
        val name = sig.signalName ?: return null
        val msg = sig.parent as? DbcMessageDef ?: return null
        val id = msg.messageId ?: return null
        val file = sig.containingFile ?: return null
        return PsiTreeUtil.findChildrenOfType(file, DbcCommentDef::class.java)
            .find { it.commentType == "SG_" && it.targetId == id && it.targetName == name }
            ?.commentText
    }

    private fun findSignalValueDescriptions(sig: DbcSignalDef): List<Pair<Long, String>> {
        val name = sig.signalName ?: return emptyList()
        val msg = sig.parent as? DbcMessageDef ?: return emptyList()
        val id = msg.messageId ?: return emptyList()
        val file = sig.containingFile ?: return emptyList()
        return PsiTreeUtil.findChildrenOfType(file, DbcValueDescriptionDef::class.java)
            .find { it.messageId == id && it.signalName == name }
            ?.entries ?: emptyList()
    }

    private fun escHtml(text: String): String {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    }
}
