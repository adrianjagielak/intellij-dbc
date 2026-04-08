package com.github.adrianjagielak.intellijdbc.psi

import com.github.adrianjagielak.intellijdbc.DbcLanguage
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType

class DbcElementType(debugName: String) : IElementType(debugName, DbcLanguage.INSTANCE) {
    override fun toString(): String = "DbcElementType.${super.toString()}"
}

object DbcElementTypes {
    @JvmField val FILE = IFileElementType(DbcLanguage.INSTANCE)

    // Top-level definitions
    @JvmField val VERSION_DEF = DbcElementType("VERSION_DEF")
    @JvmField val NEW_SYMBOLS_DEF = DbcElementType("NEW_SYMBOLS_DEF")
    @JvmField val BUS_SPEED_DEF = DbcElementType("BUS_SPEED_DEF")
    @JvmField val NODES_DEF = DbcElementType("NODES_DEF")
    @JvmField val VALUE_TABLE_DEF = DbcElementType("VALUE_TABLE_DEF")
    @JvmField val MESSAGE_DEF = DbcElementType("MESSAGE_DEF")
    @JvmField val SIGNAL_DEF = DbcElementType("SIGNAL_DEF")
    @JvmField val COMMENT_DEF = DbcElementType("COMMENT_DEF")
    @JvmField val ATTRIBUTE_DEFINITION = DbcElementType("ATTRIBUTE_DEFINITION")
    @JvmField val ATTRIBUTE_DEFAULT = DbcElementType("ATTRIBUTE_DEFAULT")
    @JvmField val ATTRIBUTE_VALUE = DbcElementType("ATTRIBUTE_VALUE")
    @JvmField val VALUE_DESCRIPTION = DbcElementType("VALUE_DESCRIPTION")
    @JvmField val SIGNAL_GROUP_DEF = DbcElementType("SIGNAL_GROUP_DEF")
    @JvmField val MESSAGE_TRANSMITTERS_DEF = DbcElementType("MESSAGE_TRANSMITTERS_DEF")
    @JvmField val ENVIRONMENT_VARIABLE_DEF = DbcElementType("ENVIRONMENT_VARIABLE_DEF")
    @JvmField val ENVIRONMENT_VARIABLE_DATA_DEF = DbcElementType("ENVIRONMENT_VARIABLE_DATA_DEF")
    @JvmField val MUX_SIGNAL_DEF = DbcElementType("MUX_SIGNAL_DEF")
    @JvmField val SIGNAL_TYPE_DEF = DbcElementType("SIGNAL_TYPE_DEF")
    @JvmField val SIGNAL_TYPE_REF_DEF = DbcElementType("SIGNAL_TYPE_REF_DEF")
    @JvmField val ATTRIBUTE_REL_VALUE = DbcElementType("ATTRIBUTE_REL_VALUE")
    @JvmField val ATTRIBUTE_REL_DEFINITION = DbcElementType("ATTRIBUTE_REL_DEFINITION")
    @JvmField val SIGNAL_VALTYPE_DEF = DbcElementType("SIGNAL_VALTYPE_DEF")
    @JvmField val SIGNAL_GROUP_SIGNAL_TYPE_DEF = DbcElementType("SIGNAL_GROUP_SIGNAL_TYPE_DEF")

    // Sub-elements
    @JvmField val NODE_NAME = DbcElementType("NODE_NAME")
    @JvmField val MESSAGE_ID = DbcElementType("MESSAGE_ID")
    @JvmField val MESSAGE_NAME = DbcElementType("MESSAGE_NAME")
    @JvmField val SIGNAL_NAME = DbcElementType("SIGNAL_NAME")
    @JvmField val TRANSMITTER_NAME = DbcElementType("TRANSMITTER_NAME")
    @JvmField val RECEIVER_NAME = DbcElementType("RECEIVER_NAME")
    @JvmField val VALUE_TABLE_NAME = DbcElementType("VALUE_TABLE_NAME")
    @JvmField val ENV_VAR_NAME = DbcElementType("ENV_VAR_NAME")
    @JvmField val ATTRIBUTE_NAME = DbcElementType("ATTRIBUTE_NAME")
    @JvmField val VALUE_ENCODING = DbcElementType("VALUE_ENCODING")
    @JvmField val SIGNAL_EXTENDED_VALUE_TYPE = DbcElementType("SIGNAL_EXTENDED_VALUE_TYPE")
}
