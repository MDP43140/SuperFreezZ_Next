/*
 * SPDX-FileCopyrightText: 2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.ui
import android.app.Activity
import android.os.Bundle
import io.mdp43140.superfreeze.App
import io.mdp43140.superfreeze.databinding.ActivityEmptyBinding
class FreezeShortcutActivity: Activity(){
	override fun onCreate(savedInstanceState:Bundle?){
		super.onCreate(savedInstanceState)
		setContentView(ActivityEmptyBinding
			.inflate(layoutInflater)
			.root)
		App.freezeUtil.freezeApp(this)
	}
	override fun onDestroy(){
		super.onDestroy()
		App.freezeUtil.stopFreezing()
		finish()
	}
	override fun onResume(){
		super.onResume()
		if (!App.freezeUtil.freezeNextApp(this)) finish()
	}
}
