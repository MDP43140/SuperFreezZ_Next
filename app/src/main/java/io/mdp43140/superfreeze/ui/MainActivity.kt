/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.ui
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.elevation.SurfaceColors
import io.mdp43140.superfreeze.databinding.ActivityMainBinding
import io.mdp43140.superfreeze.R
class MainActivity: BaseActivity(){
	private lateinit var binding: ActivityMainBinding
	private lateinit var menu: Menu
	override fun onCreate(savedInstanceState: Bundle?){
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		binding.apply {
			setContentView(root)
			setSupportActionBar(toolbar)
		}
		binding.toolbar.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
	}
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.main,menu)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
			menu.setGroupDividerEnabled(true);
		}
		this.menu = menu!!
		return true
	}
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId){
			R.id.settings -> startActivity(Intent(this,SettingsActivity::class.java))
			else -> return super.onOptionsItemSelected(item)
		}
		return true
	}
}
