package com.github.adrianjagielak.intellijdbc.highlighting

import com.github.adrianjagielak.intellijdbc.DbcIcons
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class DbcColorSettingsPage : ColorSettingsPage {

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", DbcSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Section keyword", DbcSyntaxHighlighter.SECTION_KEYWORD),
            AttributesDescriptor("String", DbcSyntaxHighlighter.STRING),
            AttributesDescriptor("Number", DbcSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Line comment", DbcSyntaxHighlighter.LINE_COMMENT),
            AttributesDescriptor("Identifier", DbcSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("Semicolon", DbcSyntaxHighlighter.SEMICOLON),
            AttributesDescriptor("Comma", DbcSyntaxHighlighter.COMMA),
            AttributesDescriptor("Colon", DbcSyntaxHighlighter.COLON),
            AttributesDescriptor("Parentheses", DbcSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Brackets", DbcSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Operator", DbcSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Multiplex indicator", DbcSyntaxHighlighter.MUX_INDICATOR),
            AttributesDescriptor("Bad character", DbcSyntaxHighlighter.BAD_CHARACTER),
            // Annotator-based highlighting
            AttributesDescriptor("Message name", DbcAnnotator.MESSAGE_NAME_ATTR),
            AttributesDescriptor("Signal name", DbcAnnotator.SIGNAL_NAME_ATTR),
            AttributesDescriptor("Node name", DbcAnnotator.NODE_NAME_ATTR),
            AttributesDescriptor("Message ID", DbcAnnotator.MESSAGE_ID_ATTR),
            AttributesDescriptor("Unit string", DbcAnnotator.UNIT_ATTR),
        )
    }

    override fun getIcon(): Icon = DbcIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = DbcSyntaxHighlighter()

    override fun getDemoText(): String = """
VERSION "1.0"

NS_ :

BS_:

BU_: ECU_Engine ECU_Transmission ECU_Dashboard

// Engine messages
BO_ 100 EngineData: 8 ECU_Engine
 SG_ EngineSpeed : 0|16@1+ (0.1,0) [0|8000] "rpm" ECU_Dashboard
 SG_ EngineTemp : 16|8@1+ (1,-40) [-40|215] "degC" ECU_Dashboard
 SG_ GearRequest M : 24|3@1+ (1,0) [0|6] "" ECU_Transmission
 SG_ GearCurrent m0 : 27|3@1+ (1,0) [0|6] "" ECU_Dashboard

BO_ 200 TransmissionData: 4 ECU_Transmission
 SG_ CurrentGear : 0|4@1+ (1,0) [0|8] "" ECU_Dashboard,ECU_Engine

CM_ "CAN database for engine control";
CM_ BU_ ECU_Engine "Main engine control unit";
CM_ BO_ 100 "Engine sensor data message";
CM_ SG_ 100 EngineSpeed "Engine speed in RPM";

BA_DEF_ BO_ "GenMsgCycleTime" INT 0 10000;
BA_DEF_DEF_ "GenMsgCycleTime" 100;
BA_ "GenMsgCycleTime" BO_ 100 50;

VAL_ 100 GearRequest 0 "Park" 1 "Reverse" 2 "Neutral" 3 "Drive" ;
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "DBC (CAN Database)"
}
