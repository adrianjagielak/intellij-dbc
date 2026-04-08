package com.github.adrianjagielak.intellijdbc.psi

import com.intellij.psi.PsiElement

interface DbcVersionDef : PsiElement {
    val versionString: String?
}

interface DbcNodesDef : PsiElement {
    val nodeNames: List<DbcNodeName>
}

interface DbcNodeName : DbcNamedElement

interface DbcMessageDef : DbcNamedElement {
    val messageId: Long?
    val messageName: String?
    val dlc: Int?
    val transmitterNode: String?
    val signals: List<DbcSignalDef>
}

interface DbcSignalDef : DbcNamedElement {
    val signalName: String?
    val startBit: Int?
    val bitLength: Int?
    val byteOrder: Int?       // 0=Motorola, 1=Intel
    val valueType: Int?       // 0=unsigned (+), 1=signed (-)
    val factor: Double?
    val offset: Double?
    val minimum: Double?
    val maximum: Double?
    val unit: String?
    val receiverNodes: List<String>
    val muxIndicator: String?
}

interface DbcCommentDef : PsiElement {
    val commentType: String?   // null=global, "BU_", "BO_", "SG_", "EV_"
    val targetId: Long?        // message ID for BO_ and SG_ comments
    val targetName: String?    // node/signal/env var name
    val commentText: String?
}

interface DbcValueTableDef : DbcNamedElement {
    val tableName: String?
    val entries: List<Pair<Long, String>>
}

interface DbcAttributeDefinitionDef : PsiElement {
    val objectType: String?   // null=global, "BU_", "BO_", "SG_", "EV_"
    val attributeName: String?
    val valueType: String?    // "INT", "FLOAT", "STRING", "HEX", "ENUM"
}

interface DbcAttributeDefaultDef : PsiElement {
    val attributeName: String?
}

interface DbcAttributeValueDef : PsiElement {
    val attributeName: String?
    val objectType: String?
    val targetId: Long?
    val targetName: String?
}

interface DbcValueDescriptionDef : PsiElement {
    val messageId: Long?
    val signalName: String?
    val entries: List<Pair<Long, String>>
}

interface DbcSignalGroupDef : PsiElement {
    val messageId: Long?
    val groupName: String?
    val signalNames: List<String>
}

interface DbcMessageTransmittersDef : PsiElement {
    val messageId: Long?
    val transmitterNodes: List<String>
}

interface DbcEnvironmentVariableDef : DbcNamedElement {
    val envVarName: String?
    val envVarType: Int?
}

interface DbcMuxSignalDef : PsiElement

interface DbcSignalValtypeDef : PsiElement {
    val messageId: Long?
    val signalName: String?
    val extendedValueType: Int?
}
