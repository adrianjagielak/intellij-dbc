package com.github.adrianjagielak.intellijdbc

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object DbcIcons {
    @JvmField val FILE: Icon = IconLoader.getIcon("/icons/dbc.svg", DbcIcons::class.java)
    @JvmField val MESSAGE: Icon = IconLoader.getIcon("/icons/message.svg", DbcIcons::class.java)
    @JvmField val SIGNAL: Icon = IconLoader.getIcon("/icons/signal.svg", DbcIcons::class.java)
    @JvmField val NODE: Icon = IconLoader.getIcon("/icons/node.svg", DbcIcons::class.java)
    @JvmField val ENV_VAR: Icon = IconLoader.getIcon("/icons/envvar.svg", DbcIcons::class.java)
    @JvmField val VALUE_TABLE: Icon = IconLoader.getIcon("/icons/valuetable.svg", DbcIcons::class.java)
    @JvmField val ATTRIBUTE: Icon = IconLoader.getIcon("/icons/attribute.svg", DbcIcons::class.java)
}
