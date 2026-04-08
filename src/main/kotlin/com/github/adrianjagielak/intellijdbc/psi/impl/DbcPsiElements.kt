package com.github.adrianjagielak.intellijdbc.psi.impl

import com.github.adrianjagielak.intellijdbc.lexer.DbcTokenTypes
import com.github.adrianjagielak.intellijdbc.psi.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class DbcVersionDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcVersionDef {
    override val versionString: String?
        get() = node.findChildByType(DbcTokenTypes.STRING)?.text?.removeSurrounding("\"")
}

class DbcNodesDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcNodesDef {
    override val nodeNames: List<DbcNodeName>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, DbcNodeName::class.java)
}

class DbcNodeNameImpl(node: ASTNode) : DbcNamedElementImpl(node), DbcNodeName

class DbcMessageDefImpl(node: ASTNode) : DbcNamedElementImpl(node), DbcMessageDef {
    override val messageId: Long?
        get() {
            val idNode = node.findChildByType(DbcTokenTypes.INTEGER) ?: return null
            return idNode.text.toLongOrNull()
        }

    override val messageName: String?
        get() = name

    override val dlc: Int?
        get() {
            // DLC is the second integer in the message definition
            val integers = node.getChildren(null).filter { it.elementType == DbcTokenTypes.INTEGER }
            return if (integers.size >= 2) integers[1].text.toIntOrNull() else null
        }

    override val transmitterNode: String?
        get() {
            val child = PsiTreeUtil.findChildOfType(this, DbcTransmitterNameImpl::class.java)
            return child?.text
        }

    override val signals: List<DbcSignalDef>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, DbcSignalDef::class.java)

    override fun getNameIdentifier(): PsiElement? {
        val children = node.getChildren(null)
        // Message name is the identifier after the message ID
        val idIndex = children.indexOfFirst { it.elementType == DbcTokenTypes.INTEGER }
        if (idIndex < 0) return null
        for (i in (idIndex + 1) until children.size) {
            if (children[i].elementType == DbcTokenTypes.IDENTIFIER) {
                return children[i].psi
            }
        }
        return null
    }
}

class DbcTransmitterNameImpl(node: ASTNode) : ASTWrapperPsiElement(node)

class DbcSignalDefImpl(node: ASTNode) : DbcNamedElementImpl(node), DbcSignalDef {
    override val signalName: String?
        get() = name

    override val startBit: Int?
        get() {
            val integers = collectIntegers()
            return integers.getOrNull(0)?.toIntOrNull()
        }

    override val bitLength: Int?
        get() {
            val integers = collectIntegers()
            return integers.getOrNull(1)?.toIntOrNull()
        }

    override val byteOrder: Int?
        get() {
            val integers = collectIntegers()
            return integers.getOrNull(2)?.toIntOrNull()
        }

    override val valueType: Int?
        get() {
            // Look for + or - after @<byteOrder>
            val text = node.text
            val atIndex = text.indexOf('@')
            if (atIndex < 0 || atIndex + 2 >= text.length) return null
            return when (text[atIndex + 2]) {
                '+' -> 0 // unsigned
                '-' -> 1 // signed
                else -> null
            }
        }

    override val factor: Double?
        get() {
            val floats = collectFloatsInParens()
            return floats.getOrNull(0)
        }

    override val offset: Double?
        get() {
            val floats = collectFloatsInParens()
            return floats.getOrNull(1)
        }

    override val minimum: Double?
        get() {
            val floats = collectFloatsInBrackets()
            return floats.getOrNull(0)
        }

    override val maximum: Double?
        get() {
            val floats = collectFloatsInBrackets()
            return floats.getOrNull(1)
        }

    override val unit: String?
        get() {
            // Find string token after brackets
            val stringNodes = node.getChildren(null).filter { it.elementType == DbcTokenTypes.STRING }
            return stringNodes.firstOrNull()?.text?.removeSurrounding("\"")
        }

    override val receiverNodes: List<String>
        get() {
            val receivers = PsiTreeUtil.getChildrenOfType(this, DbcReceiverNameImpl::class.java)
            return receivers?.map { it.text } ?: emptyList()
        }

    override val muxIndicator: String?
        get() {
            val muxNode = node.findChildByType(DbcTokenTypes.MUX_INDICATOR)
            if (muxNode != null) return muxNode.text
            // Check for 'M' identifier used as multiplex switch
            val children = node.getChildren(null).toList()
            val colonIndex = children.indexOfFirst { it.elementType == DbcTokenTypes.COLON }
            if (colonIndex >= 0) {
                for (i in (colonIndex + 1) until children.size) {
                    val child = children[i]
                    if (child.elementType == DbcTokenTypes.WHITE_SPACE) continue
                    if (child.elementType == DbcTokenTypes.IDENTIFIER && child.text == "M") return "M"
                    break
                }
            }
            return null
        }

    private fun collectIntegers(): List<String> {
        return node.getChildren(null)
            .filter { it.elementType == DbcTokenTypes.INTEGER }
            .map { it.text }
    }

    private fun collectFloatsInParens(): List<Double> {
        val text = node.text
        val parenStart = text.indexOf('(')
        val parenEnd = text.indexOf(')', parenStart + 1)
        if (parenStart < 0 || parenEnd < 0) return emptyList()
        val content = text.substring(parenStart + 1, parenEnd)
        return content.split(',').mapNotNull { it.trim().toDoubleOrNull() }
    }

    private fun collectFloatsInBrackets(): List<Double> {
        val text = node.text
        val bracketStart = text.indexOf('[')
        val bracketEnd = text.indexOf(']', bracketStart + 1)
        if (bracketStart < 0 || bracketEnd < 0) return emptyList()
        val content = text.substring(bracketStart + 1, bracketEnd)
        return content.split('|').mapNotNull { it.trim().toDoubleOrNull() }
    }
}

class DbcReceiverNameImpl(node: ASTNode) : ASTWrapperPsiElement(node)

class DbcCommentDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcCommentDef {
    override val commentType: String?
        get() {
            val children = node.getChildren(null)
            for (child in children) {
                val type = child.elementType
                if (type == DbcTokenTypes.BU_ || type == DbcTokenTypes.BO_ ||
                    type == DbcTokenTypes.SG_ || type == DbcTokenTypes.EV_) {
                    return child.text
                }
            }
            return null
        }

    override val targetId: Long?
        get() {
            if (commentType == "BO_" || commentType == "SG_") {
                return node.findChildByType(DbcTokenTypes.INTEGER)?.text?.toLongOrNull()
            }
            return null
        }

    override val targetName: String?
        get() {
            if (commentType == "BU_" || commentType == "EV_") {
                return node.findChildByType(DbcTokenTypes.IDENTIFIER)?.text
            }
            if (commentType == "SG_") {
                // Signal name is the identifier after the message ID
                val children = node.getChildren(null)
                val idIndex = children.indexOfFirst { it.elementType == DbcTokenTypes.INTEGER }
                if (idIndex >= 0) {
                    for (i in (idIndex + 1) until children.size) {
                        if (children[i].elementType == DbcTokenTypes.IDENTIFIER) {
                            return children[i].text
                        }
                    }
                }
            }
            return null
        }

    override val commentText: String?
        get() {
            val strings = node.getChildren(null).filter { it.elementType == DbcTokenTypes.STRING }
            return strings.lastOrNull()?.text?.removeSurrounding("\"")
        }
}

class DbcValueTableDefImpl(node: ASTNode) : DbcNamedElementImpl(node), DbcValueTableDef {
    override val tableName: String?
        get() = name

    override val entries: List<Pair<Long, String>>
        get() {
            val result = mutableListOf<Pair<Long, String>>()
            val children = node.getChildren(null).toList()
            var i = 0
            // Skip keyword and name
            while (i < children.size && children[i].elementType != DbcTokenTypes.INTEGER) i++
            while (i < children.size) {
                if (children[i].elementType == DbcTokenTypes.INTEGER) {
                    val value = children[i].text.toLongOrNull()
                    // Find next string
                    i++
                    while (i < children.size && children[i].elementType != DbcTokenTypes.STRING) i++
                    if (i < children.size && value != null) {
                        result.add(value to children[i].text.removeSurrounding("\""))
                    }
                }
                i++
            }
            return result
        }
}

class DbcAttributeDefinitionDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcAttributeDefinitionDef {
    override val objectType: String?
        get() {
            val children = node.getChildren(null)
            for (child in children) {
                val type = child.elementType
                if (type == DbcTokenTypes.BU_ || type == DbcTokenTypes.BO_ ||
                    type == DbcTokenTypes.SG_ || type == DbcTokenTypes.EV_) {
                    return child.text
                }
            }
            return null
        }

    override val attributeName: String?
        get() = node.findChildByType(DbcTokenTypes.STRING)?.text?.removeSurrounding("\"")

    override val valueType: String?
        get() {
            val children = node.getChildren(null)
            for (child in children) {
                when (child.elementType) {
                    DbcTokenTypes.INT_KEYWORD -> return "INT"
                    DbcTokenTypes.FLOAT_KEYWORD -> return "FLOAT"
                    DbcTokenTypes.STRING_KEYWORD -> return "STRING"
                    DbcTokenTypes.HEX_KEYWORD -> return "HEX"
                    DbcTokenTypes.ENUM_KEYWORD -> return "ENUM"
                }
            }
            return null
        }
}

class DbcAttributeDefaultDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcAttributeDefaultDef {
    override val attributeName: String?
        get() = node.findChildByType(DbcTokenTypes.STRING)?.text?.removeSurrounding("\"")
}

class DbcAttributeValueDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcAttributeValueDef {
    override val attributeName: String?
        get() = node.findChildByType(DbcTokenTypes.STRING)?.text?.removeSurrounding("\"")

    override val objectType: String?
        get() {
            val children = node.getChildren(null)
            for (child in children) {
                val type = child.elementType
                if (type == DbcTokenTypes.BU_ || type == DbcTokenTypes.BO_ ||
                    type == DbcTokenTypes.SG_ || type == DbcTokenTypes.EV_) {
                    return child.text
                }
            }
            return null
        }

    override val targetId: Long?
        get() = node.findChildByType(DbcTokenTypes.INTEGER)?.text?.toLongOrNull()

    override val targetName: String?
        get() = node.findChildByType(DbcTokenTypes.IDENTIFIER)?.text
}

class DbcValueDescriptionDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcValueDescriptionDef {
    override val messageId: Long?
        get() {
            val integers = node.getChildren(null).filter { it.elementType == DbcTokenTypes.INTEGER }
            return integers.firstOrNull()?.text?.toLongOrNull()
        }

    override val signalName: String?
        get() = node.findChildByType(DbcTokenTypes.IDENTIFIER)?.text

    override val entries: List<Pair<Long, String>>
        get() {
            val result = mutableListOf<Pair<Long, String>>()
            val children = node.getChildren(null).toList()
            // Skip first integer (message ID) and identifier (signal name)
            var foundSignalName = false
            var i = 0
            while (i < children.size) {
                if (children[i].elementType == DbcTokenTypes.IDENTIFIER) {
                    foundSignalName = true
                    i++
                    break
                }
                i++
            }
            if (!foundSignalName) return result
            while (i < children.size) {
                if (children[i].elementType == DbcTokenTypes.INTEGER) {
                    val value = children[i].text.toLongOrNull()
                    i++
                    while (i < children.size && children[i].elementType != DbcTokenTypes.STRING) i++
                    if (i < children.size && value != null) {
                        result.add(value to children[i].text.removeSurrounding("\""))
                    }
                }
                i++
            }
            return result
        }
}

class DbcSignalGroupDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcSignalGroupDef {
    override val messageId: Long?
        get() = node.findChildByType(DbcTokenTypes.INTEGER)?.text?.toLongOrNull()

    override val groupName: String?
        get() = node.findChildByType(DbcTokenTypes.IDENTIFIER)?.text

    override val signalNames: List<String>
        get() {
            val identifiers = node.getChildren(null).filter { it.elementType == DbcTokenTypes.IDENTIFIER }
            // First identifier is group name, rest are signal names
            return identifiers.drop(1).map { it.text }
        }
}

class DbcMessageTransmittersDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcMessageTransmittersDef {
    override val messageId: Long?
        get() = node.findChildByType(DbcTokenTypes.INTEGER)?.text?.toLongOrNull()

    override val transmitterNodes: List<String>
        get() {
            val identifiers = node.getChildren(null).filter { it.elementType == DbcTokenTypes.IDENTIFIER }
            return identifiers.map { it.text }
        }
}

class DbcEnvironmentVariableDefImpl(node: ASTNode) : DbcNamedElementImpl(node), DbcEnvironmentVariableDef {
    override val envVarName: String?
        get() = name

    override val envVarType: Int?
        get() {
            val integers = node.getChildren(null).filter { it.elementType == DbcTokenTypes.INTEGER }
            return integers.firstOrNull()?.text?.toIntOrNull()
        }
}

class DbcMuxSignalDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcMuxSignalDef

class DbcSignalValtypeDefImpl(node: ASTNode) : ASTWrapperPsiElement(node), DbcSignalValtypeDef {
    override val messageId: Long?
        get() {
            val integers = node.getChildren(null).filter { it.elementType == DbcTokenTypes.INTEGER }
            return integers.firstOrNull()?.text?.toLongOrNull()
        }

    override val signalName: String?
        get() = node.findChildByType(DbcTokenTypes.IDENTIFIER)?.text

    override val extendedValueType: Int?
        get() {
            val integers = node.getChildren(null).filter { it.elementType == DbcTokenTypes.INTEGER }
            return integers.getOrNull(1)?.text?.toIntOrNull()
        }
}

// Generic wrapper for elements that don't need special handling
class DbcGenericElementImpl(node: ASTNode) : ASTWrapperPsiElement(node)
