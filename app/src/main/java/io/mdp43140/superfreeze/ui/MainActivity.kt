/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.ui
import android.app.SearchManager
import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
	var currSelectedApp: MutableList<AppListItems.AppItem> = mutableListOf()
	override fun onCreate(savedInstanceState: Bundle?){
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		binding.apply {
			setContentView(root)
			swiperefresh.setOnRefreshListener {
				loadApps()
			}
			fab.setOnClickListener {
				stopApps()
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
		App.appListItems?.getDataFromPrefs()
		App.appListItems?.getActiveAccessibilityServices()
		App.appListItems?.getEnabledIMEs()
		appListAdapter.sort()
		binding.swiperefresh.post {
			binding.swiperefresh.isRefreshing = false
		}
	}.start()
	fun onAppClicked(app: AppListItems.AppItem){
		app.isItemSelected = !app.isItemSelected
		// process stuff
		if (app.isItemSelected){
			currSelectedApp.add(app)
		} else {
			currSelectedApp.remove(app)
		}
		if (currSelectedApp.isEmpty()){
			// clear
			binding.toolbar.subtitle = ""
			menu.findItem(R.id.ignoreBgFree).setVisible(false)
			menu.findItem(R.id.ignoreRunning).setVisible(false)
			menu.findItem(R.id.stop_off).setEnabled(false)
			menu.findItem(R.id.stop_normal).setEnabled(false)
			menu.findItem(R.id.stop_inactive).setEnabled(false)
		} else {
			menu.findItem(R.id.ignoreBgFree).setVisible(true)
			menu.findItem(R.id.ignoreRunning).setVisible(true)
			menu.findItem(R.id.stop_off).setEnabled(true)
			menu.findItem(R.id.stop_normal).setEnabled(true)
			menu.findItem(R.id.stop_inactive).setEnabled(true)
			binding.toolbar.subtitle = getString(R.string.apps_is_selected,currSelectedApp.size)
			if (currSelectedApp.size == 1){
				setRadioMenuSelected(menu.findItem(when (app.stopMode){
					0 -> R.id.stop_off
					2 -> R.id.stop_inactive
					else -> R.id.stop_normal
				}),R.id.stop_method)
				menu.findItem(R.id.ignoreBgFree).isChecked = app.ignoreBgFree
				menu.findItem(R.id.ignoreRunning).isChecked = app.ignoreRunning
			} else {
				menu.findItem(R.id.stop_off).isChecked = false
				menu.findItem(R.id.stop_normal).isChecked = false
				menu.findItem(R.id.stop_inactive).isChecked = false
				menu.findItem(R.id.ignoreBgFree).isChecked = false
				menu.findItem(R.id.ignoreRunning).isChecked = false
			}
		}
	}
	fun stopApps(){
		binding.swiperefresh.post {
			binding.swiperefresh.isRefreshing = true
		}
		if (currSelectedApp.isEmpty()){
			// stop all pending stop apps
			FreezeShortcutActivity.freezeApp(this)
		}
		else {
			var prevState: MutableMap<String,tempAppItem> = mutableMapOf()
			for (item in currSelectedApp){
				prevState[item.pkg] = tempAppItem(item.stopMode,item.ignoreBgFree,item.ignoreRunning)
				item.stopMode       = 1
				item.ignoreBgFree   = true
				item.ignoreRunning  = true
			}
			FreezeShortcutActivity.freezeApp(this,currSelectedApp)
			for (item in currSelectedApp){
				// TODO: there is bug which had 2 same app selected,
				// and when stopping, it tries to revert state, but for later reverts,
				// because the 1st one already removes the reference, now its null :(
				// and crashed.
				item.stopMode      = prevState[item.pkg]!!.stopMode
				item.ignoreBgFree  = prevState[item.pkg]!!.ignoreBgFree
				item.ignoreRunning = prevState[item.pkg]!!.ignoreRunning
				prevState.remove(item.pkg)
			}
		}
		appListAdapter!!.sort()
		binding.swiperefresh.post {
			binding.swiperefresh.isRefreshing = false
		}
	};
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.main,menu)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
			menu.setGroupDividerEnabled(true);
		}
		this.menu = menu!!
		menu.findItem(R.id.sort_reverse).isChecked = AppListAdapter.sortReverse
		menu.findItem(R.id.sort_user).isChecked = AppListAdapter.showUserApp
		menu.findItem(R.id.sort_system).isChecked = AppListAdapter.showSystemApp
		menu.findItem(R.id.ignoreBgFree).setVisible(false)
		menu.findItem(R.id.ignoreRunning).setVisible(false)
		menu.findItem(R.id.stop_off).setEnabled(false)
		menu.findItem(R.id.stop_normal).setEnabled(false)
		menu.findItem(R.id.stop_inactive).setEnabled(false)
		setRadioMenuSelected(menu.findItem(when (AppListAdapter.sortOrder){
			1 -> R.id.sort_pkg
			else -> R.id.sort_label
		}),R.id.sortByOrder)
		setRadioMenuSelected(menu.findItem(when (AppListAdapter.categorizeItem){
			0 -> R.id.categorize_none
			2 -> R.id.categorize_unusedFirst
			3 -> R.id.categorize_userSystem
			else -> R.id.categorize_appState
		}),R.id.categorize)
		val searchView = menu.findItem(R.id.search).actionView as SearchView
		searchView.setSearchableInfo((getSystemService(SearchManager::class.java)).getSearchableInfo(componentName))
		searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(s: String) = false
			override fun onQueryTextChange(s: String): Boolean {
				binding.appsList.stopScroll()
				appListAdapter.searchPattern = s
				return true
			}
		})
		return true
	}
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId){
			R.id.settings -> startActivity(Intent(this,SettingsActivity::class.java))
			R.id.stop_off -> {
				setRadioMenuSelected(item,R.id.stop_method)
				appListAdapter!!.sort()
				for (app in currSelectedApp){
					app.stopMode = 0
					App.appListItems?.storeDataToPrefs(app)
				}
			}
			R.id.stop_normal -> {
				setRadioMenuSelected(item,R.id.stop_method)
				appListAdapter!!.sort()
				for (app in currSelectedApp){
					app.stopMode = 1
					App.appListItems?.storeDataToPrefs(app)
				}
			}
			R.id.stop_inactive -> {
				setRadioMenuSelected(item,R.id.stop_method)
				appListAdapter!!.sort()
				for (app in currSelectedApp){
					app.stopMode = 2
					App.appListItems?.storeDataToPrefs(app)
				}
			}
			R.id.ignoreRunning -> {
				menu.findItem(R.id.ignoreRunning).isChecked =
				!menu.findItem(R.id.ignoreRunning).isChecked
				appListAdapter!!.sort()
				for (app in currSelectedApp){
					app.ignoreRunning = !app.ignoreRunning
					App.appListItems?.storeDataToPrefs(app)
				}
			}
			R.id.ignoreBgFree -> {
				menu.findItem(R.id.ignoreBgFree).isChecked =
				!menu.findItem(R.id.ignoreBgFree).isChecked
				appListAdapter!!.sort()
				for (app in currSelectedApp){
					app.ignoreBgFree = !app.ignoreBgFree
					App.appListItems?.storeDataToPrefs(app)
				}
			}
			R.id.sort_reverse -> AppListAdapter.let {
				it.sortReverse = !it.sortReverse
				item.isChecked = it.sortReverse
				App.prefs!!.edit().putBoolean("sortReverse",item.isChecked).apply()
				appListAdapter!!.sort()
			}
			R.id.sort_user -> AppListAdapter.let {
				it.showUserApp = !it.showUserApp
				item.isChecked = it.showUserApp
				App.prefs!!.edit().putBoolean("showUserApp",item.isChecked).apply()
				appListAdapter!!.sort()
			}
			R.id.sort_system -> AppListAdapter.let {
				it.showSystemApp = !it.showSystemApp
				item.isChecked = it.showSystemApp
				App.prefs!!.edit().putBoolean("showSystemApp",item.isChecked).apply()
				appListAdapter!!.sort()
			}
			R.id.sort_label -> {
				setRadioMenuSelected(item, R.id.sortByOrder)
				AppListAdapter.sortOrder = 0
				App.prefs!!.edit().putInt("sortBy",0).apply()
				appListAdapter!!.sort()
			}
			R.id.sort_pkg -> {
				setRadioMenuSelected(item, R.id.sortByOrder)
				AppListAdapter.sortOrder = 1
				App.prefs!!.edit().putInt("sortBy",1).apply()
				appListAdapter!!.sort()
			}
			R.id.categorize_none -> {
				setRadioMenuSelected(item, R.id.categorize)
				AppListAdapter.categorizeItem = 0
				App.prefs!!.edit().putInt("categorizeItem",0).apply()
				appListAdapter!!.sort()
			}
			R.id.categorize_appState -> {
				setRadioMenuSelected(item, R.id.categorize)
				AppListAdapter.categorizeItem = 1
				App.prefs!!.edit().putInt("categorizeItem",1).apply()
				appListAdapter!!.sort()
			}
			R.id.categorize_unusedFirst -> {
				setRadioMenuSelected(item, R.id.categorize)
				AppListAdapter.categorizeItem = 2
				App.prefs!!.edit().putInt("categorizeItem",2).apply()
				appListAdapter!!.sort()
			}
			R.id.categorize_userSystem -> {
				setRadioMenuSelected(item, R.id.categorize)
				AppListAdapter.categorizeItem = 3
				App.prefs!!.edit().putInt("categorizeItem",3).apply()
				appListAdapter!!.sort()
			}
			else -> return super.onOptionsItemSelected(item)
		}
		return true
	}
	override fun onConfigurationChanged(newCfg: Configuration) {
		super.onConfigurationChanged(newCfg)
		//This is necessary so that the list items change their look when the screen is rotated:
		binding.appsList.let {
			val pos = (it.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//		it.adapter = null
//		it.layoutManager = null
//		it.recycledViewPool.clear()
//		it.adapter = appListAdapter
//		it.layoutManager = LinearLayoutManager(this)
//		appListAdapter.notifyDataSetChanged()
			(it.layoutManager as LinearLayoutManager).scrollToPosition(pos)
		}
	}
	override fun onResume(){
		super.onResume()
		loadApps()
	}
	override fun onDestroy(){
		super.onDestroy()
		appListAdapter.trimMemory()
		appListAdapter.setCtx(null)
	}
	override fun onBackPressed(){
		// suggested fix by LeakCanary
		finishAfterTransition()
	}
	override fun onTrimMemory(level: Int){
		// See https://developer.android.com/topic/performance/memory#release
		// on API 34+, apps only notified with UI_HIDDEN and BACKGROUND level
		when (level){
			ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> appListAdapter.trimMemory()
			ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {}
			ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
			ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
			ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
			ComponentCallbacks2.TRIM_MEMORY_MODERATE,
			ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
				appListAdapter.trimMemory()
			}
			else -> {}
		}
		Toast.makeText(this,"Memory cleared with level ${level}",Toast.LENGTH_SHORT).show()
	}
	private fun setRadioMenuSelected(selectedItem: MenuItem?, groupId: Int) {
		for (i in 0 until menu.size()){
			val item = menu.getItem(i)
			if (item.groupId == groupId) item.isChecked = false
		}
		selectedItem?.isChecked = true
	}
	private data class tempAppItem(val stopMode: Int,val ignoreBgFree: Boolean,val ignoreRunning: Boolean)
}
