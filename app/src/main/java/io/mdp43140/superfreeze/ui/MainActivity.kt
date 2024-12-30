/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.ui
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.elevation.SurfaceColors
import io.mdp43140.superfreeze.App
import io.mdp43140.superfreeze.AppListItems
import io.mdp43140.superfreeze.databinding.ActivityMainBinding
import io.mdp43140.superfreeze.R
class MainActivity: BaseActivity(){
	private lateinit var appListAdapter: AppListAdapter
	private lateinit var binding: ActivityMainBinding
	private lateinit var menu: Menu
	override fun onCreate(savedInstanceState: Bundle?){
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		binding.apply {
			setContentView(root)
			swiperefresh.setOnRefreshListener {
				loadApps()
			}
			setSupportActionBar(toolbar)
		}
		binding.toolbar.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
		App.appListItems = AppListItems(this)
		App.appListItems?.loadPrefs()

		appListAdapter = AppListAdapter(::onAppClicked)
		appListAdapter.setCtx(this)
		appListAdapter.setAppList(App.appListItems!!)

		binding.appsList.adapter = appListAdapter
		binding.appsList.setItemViewCacheSize(15)
		binding.appsList.addItemDecoration(DividerItemDecoration(this,1))
		binding.appsList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
	}
	fun loadApps() = Thread {
		binding.swiperefresh.post {
			binding.swiperefresh.isRefreshing = true
		}
		App.appListItems?.loadApps()
		appListAdapter.sort()
		binding.swiperefresh.post {
			binding.swiperefresh.isRefreshing = false
		}
	}.start()
	fun onAppClicked(app: AppListItems.AppItem){
		// Work In Progress
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
	override fun onConfigurationChanged(newCfg: Configuration) {
		super.onConfigurationChanged(newCfg)
		//This is necessary so that the list items change their look when the screen is rotated:
		binding.appsList.let {
			val pos = (it.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
			it.adapter = null
			it.layoutManager = null
			it.recycledViewPool.clear()
			it.adapter = appListAdapter
			it.layoutManager = LinearLayoutManager(this)
			appListAdapter.notifyDataSetChanged()
			(it.layoutManager as LinearLayoutManager).scrollToPosition(pos)
		}
	}
	override fun onResume(){
		super.onResume()
		loadApps()
	}
	override fun onBackPressed(){
		// suggested fix by LeakCanary
		finishAfterTransition()
	}
}
