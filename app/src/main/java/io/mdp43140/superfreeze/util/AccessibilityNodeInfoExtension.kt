/*
 * SPDX-FileCopyrightText: 2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.util
import android.view.accessibility.AccessibilityNodeInfo

fun AccessibilityNodeInfo.compareText(text: CharSequence): Boolean {
	return this.compareText(text.toString())
}

fun AccessibilityNodeInfo.compareText(text: String): Boolean {
	return this.text?.toString()?.lowercase() == text.lowercase()
}

fun AccessibilityNodeInfo.matchNode(matcher: (AccessibilityNodeInfo?) -> Boolean): AccessibilityNodeInfo? {
//someNode.matchNode { it.compareText("OK") }
	this.getAllChild().forEach {
		it?.matchNode(matcher)?.let { return it }
	}
	return this.takeIf(matcher)
}

fun AccessibilityNodeInfo.findClickable(): AccessibilityNodeInfo? {
	return when {
		this.isClickable && this.isEnabled -> this
		else -> this.parent?.findClickable()
	}
}

fun AccessibilityNodeInfo.performClick(): Boolean? {
	return this.findClickable()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
}

fun AccessibilityNodeInfo.getAllChild(): Iterator<AccessibilityNodeInfo?> {
	return object: Iterator<AccessibilityNodeInfo?> {
		val childCount = this@getAllChild.childCount
		var currentIdx = 0
		override fun hasNext(): Boolean {
			return childCount > 0 && currentIdx < childCount
		}
		override fun next(): AccessibilityNodeInfo? {
			return this@getAllChild.getChild(currentIdx++)
		}
	}
}

fun AccessibilityNodeInfo.showTree(level: Int) {
	this.getAllChild().forEach { it?.showTree(level + 1) }
}
