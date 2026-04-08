package com.github.adrianjagielak.intellijdbc.inspection

import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.codeInspection.*
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class DbcDuplicateMessageIdInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Duplicate message ID"

    override fun getGroupDisplayName(): String = "DBC"

    override fun getShortName(): String = "DbcDuplicateMessageId"

    override fun isEnabledByDefault(): Boolean = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                val messages = PsiTreeUtil.findChildrenOfType(file, DbcMessageDef::class.java)
                val idMap = mutableMapOf<Long, MutableList<DbcMessageDef>>()

                for (msg in messages) {
                    val id = msg.messageId ?: continue
                    idMap.getOrPut(id) { mutableListOf() }.add(msg)
                }

                for ((id, msgs) in idMap) {
                    if (msgs.size > 1) {
                        for (msg in msgs) {
                            val nameElement = msg.nameIdentifier ?: continue
                            holder.registerProblem(
                                nameElement,
                                "Duplicate message ID 0x${"%X".format(id)} (${id})",
                                ProblemHighlightType.WARNING
                            )
                        }
                    }
                }
            }
        }
    }
}

class DbcSignalOverflowInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Signal exceeds message length"

    override fun getGroupDisplayName(): String = "DBC"

    override fun getShortName(): String = "DbcSignalOverflow"

    override fun isEnabledByDefault(): Boolean = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                val messages = PsiTreeUtil.findChildrenOfType(file, DbcMessageDef::class.java)

                for (msg in messages) {
                    val dlc = msg.dlc ?: continue
                    val maxBits = dlc * 8

                    for (signal in msg.signals) {
                        val startBit = signal.startBit ?: continue
                        val bitLength = signal.bitLength ?: continue
                        val byteOrder = signal.byteOrder ?: continue

                        if (byteOrder == 1) {
                            // Intel/Little Endian: simple contiguous range
                            val endBit = startBit + bitLength
                            if (endBit > maxBits) {
                                val nameElement = signal.nameIdentifier ?: continue
                                holder.registerProblem(
                                    nameElement,
                                    "Signal '${signal.signalName}' exceeds message length: needs bit ${endBit - 1} but DLC=$dlc (max bit ${maxBits - 1})",
                                    ProblemHighlightType.WARNING
                                )
                            }
                        }
                        // Motorola byte order validation is complex, skip detailed check
                    }
                }
            }
        }
    }
}

class DbcUndefinedNodeInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Undefined node reference"

    override fun getGroupDisplayName(): String = "DBC"

    override fun getShortName(): String = "DbcUndefinedNode"

    override fun isEnabledByDefault(): Boolean = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                // Collect all defined node names
                val definedNodes = mutableSetOf("Vector__XXX")
                PsiTreeUtil.findChildrenOfType(file, DbcNodeName::class.java).forEach { node ->
                    node.name?.let { definedNodes.add(it) }
                }

                // Check transmitter references
                PsiTreeUtil.findChildrenOfType(file, DbcMessageDef::class.java).forEach { msg ->
                    val txNode = msg.transmitterNode
                    if (txNode != null && txNode !in definedNodes) {
                        val txElement = PsiTreeUtil.findChildOfType(msg, com.github.adrianjagielak.intellijdbc.psi.impl.DbcTransmitterNameImpl::class.java)
                        if (txElement != null) {
                            holder.registerProblem(
                                txElement,
                                "Undefined node '$txNode'",
                                ProblemHighlightType.WARNING
                            )
                        }
                    }

                    // Check receiver references in signals
                    for (signal in msg.signals) {
                        for (receiver in signal.receiverNodes) {
                            if (receiver !in definedNodes) {
                                // We'd need the specific PSI element for the receiver name
                                // For now, flag at the signal level
                                val nameEl = signal.nameIdentifier
                                if (nameEl != null) {
                                    holder.registerProblem(
                                        nameEl,
                                        "Signal '${signal.signalName}' references undefined receiver node '$receiver'",
                                        ProblemHighlightType.WEAK_WARNING
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class DbcDuplicateSignalNameInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Duplicate signal name in message"

    override fun getGroupDisplayName(): String = "DBC"

    override fun getShortName(): String = "DbcDuplicateSignalName"

    override fun isEnabledByDefault(): Boolean = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                val messages = PsiTreeUtil.findChildrenOfType(file, DbcMessageDef::class.java)
                for (msg in messages) {
                    val signalNameMap = mutableMapOf<String, MutableList<DbcSignalDef>>()
                    for (signal in msg.signals) {
                        val name = signal.signalName ?: continue
                        signalNameMap.getOrPut(name) { mutableListOf() }.add(signal)
                    }
                    for ((name, sigs) in signalNameMap) {
                        if (sigs.size > 1) {
                            for (sig in sigs) {
                                val nameElement = sig.nameIdentifier ?: continue
                                holder.registerProblem(
                                    nameElement,
                                    "Duplicate signal name '$name' in message '${msg.messageName}'",
                                    ProblemHighlightType.WARNING
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class DbcSignalOverlapInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Overlapping signals"

    override fun getGroupDisplayName(): String = "DBC"

    override fun getShortName(): String = "DbcSignalOverlap"

    override fun isEnabledByDefault(): Boolean = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                val messages = PsiTreeUtil.findChildrenOfType(file, DbcMessageDef::class.java)
                for (msg in messages) {
                    val signals = msg.signals.filter { it.byteOrder == 1 && it.muxIndicator == null } // Only check Intel LE non-mux
                    for (i in signals.indices) {
                        for (j in (i + 1) until signals.size) {
                            val sig1 = signals[i]
                            val sig2 = signals[j]
                            val start1 = sig1.startBit ?: continue
                            val len1 = sig1.bitLength ?: continue
                            val start2 = sig2.startBit ?: continue
                            val len2 = sig2.bitLength ?: continue

                            val end1 = start1 + len1
                            val end2 = start2 + len2

                            if (start1 < end2 && start2 < end1) {
                                val nameElement = sig2.nameIdentifier ?: continue
                                holder.registerProblem(
                                    nameElement,
                                    "Signal '${sig2.signalName}' overlaps with '${sig1.signalName}' (bits ${start1}-${end1 - 1} vs ${start2}-${end2 - 1})",
                                    ProblemHighlightType.WARNING
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
