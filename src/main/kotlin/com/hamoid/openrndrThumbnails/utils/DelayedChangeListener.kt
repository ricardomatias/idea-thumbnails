package com.hamoid.openrndrThumbnails.utils

import javax.swing.Timer
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


open class DelayedChangeListener : DocumentListener {
    private val timer = Timer(250) { fireStateChanged() }.also {
        it.isRepeats = false
    }
    private val listeners = mutableListOf<ChangeListener>()

    fun addChangeListener(listener: ChangeListener) {
        listeners.add(listener)
    }

    fun removeChangeListener(listener: ChangeListener) {
        listeners.remove(listener)
    }

    private fun fireStateChanged() {
        val ev = ChangeEvent(this)
        listeners.forEach { it.stateChanged(ev) }
    }

    override fun insertUpdate(e: DocumentEvent?) = timer.restart()
    override fun removeUpdate(e: DocumentEvent?) = timer.restart()
    override fun changedUpdate(e: DocumentEvent?) = timer.restart()
}