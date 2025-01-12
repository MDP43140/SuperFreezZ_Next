/*
 * SPDX-FileCopyrightText: 2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.ui
import android.app.Activity
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.mdp43140.superfreeze.databinding.AppListItemBinding
import io.mdp43140.superfreeze.databinding.AppListSectionHeaderBinding
import io.mdp43140.superfreeze.App
import io.mdp43140.superfreeze.AppListItems
import io.mdp43140.superfreeze.AppListItems.AbstractItem
import io.mdp43140.superfreeze.AppListItems.AppItem
import io.mdp43140.superfreeze.AppListItems.LabelItem
import io.mdp43140.superfreeze.CommonFunctions
import io.mdp43140.superfreeze.Constants
import io.mdp43140.superfreeze.NotificationService
import io.mdp43140.superfreeze.R
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.util.Locale
class AppListAdapter(
	private val onAppClick: ((AppItem) -> Unit)?
): RecyclerView.Adapter<ViewHolder>(), PopupTextProvider {
	lateinit var appListItems: AppListItems
	lateinit var appListItems2: List<AbstractItem>
	private var ctx: Activity? = null
	val appLabelCache = HashMap<String,String>()
	var bgHighlight = 0
	var searchPattern: String = ""
		set(value){
			field = value.lowercase()
			sort()
		}
	init {
		setHasStableIds(true)
	}
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return when (viewType){
			Constants.VIEW_TYPE_APP   -> AppViewHolder(AppListItemBinding.inflate(ctx!!.layoutInflater))
			Constants.VIEW_TYPE_LABEL -> LabelViewHolder(AppListSectionHeaderBinding.inflate(ctx!!.layoutInflater))
			else                      -> throw IllegalArgumentException("Invalid view type")
		}
	}
	override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
		when (holder){
			is AppViewHolder   -> holder.bind(appListItems2[pos],this)
			is LabelViewHolder -> holder.bind(appListItems2[pos],this)
		}
	}
	override fun getItemCount(): Int = appListItems2.size
	override fun getItemId(pos: Int): Long {
		return appListItems2[pos].pkg.hashCode().toLong()
	}
	override fun getItemViewType(pos: Int): Int {
		return when (appListItems2[pos]){
			is AppItem   -> Constants.VIEW_TYPE_APP
			is LabelItem -> Constants.VIEW_TYPE_LABEL
			else         -> throw IllegalArgumentException("Invalid view type")
		}
	}
	override fun getPopupText(view: View, pos: Int): CharSequence {
		val item = appListItems2[pos]
		return (if (sortOrder == 1) item.pkg else item.label).take(1).uppercase(Locale.getDefault())
	}
	fun setCtx(ctx2: Activity?){
		ctx = ctx2
		if (ctx != null){
			bgHighlight = ctx!!.getColor(R.color.bg_highlight)
		}
	}
	fun setAppList(appListItems3: AppListItems){
		appListItems = appListItems3
		appListItems2 = appListItems.appList
	}
	fun trimMemory(){
		appLabelCache.clear()
		appListItems2.forEach {
			it.icon = null
		}
	};
	fun sort() = Thread {
		// Filtering (User/System/Runnning/Stopped)
		appListItems2 = appListItems.appList.filter {
			var isSystemApp = CommonFunctions.isFlagSet(it.flags,ApplicationInfo.FLAG_SYSTEM)
			(showUserApp && !isSystemApp) ||
			(showSystemApp && isSystemApp)
		}
		// Sort by A-Z
		appListItems2 = when (sortOrder){
			1    -> appListItems2.sortedBy { it.pkg.lowercase() }
			else -> appListItems2.sortedBy { it.label.lowercase() }
		}
		// Reverse
		if (sortReverse) appListItems2 = appListItems2.reversed()
		// Filtering (Search. placed after sorting & reverse because this filtering also separates the startsWith and contains word)
		if (!searchPattern.isEmpty()){
			// Show the more relevant apps (that starts with the search pattern) at the top
			val (importantApps, otherApps) = appListItems2
				.asSequence()
				.filter {
					// headers, matching name, matching package
					it is LabelItem ||
					it.label.lowercase().contains(searchPattern) ||
					it.pkg.lowercase().contains(searchPattern)
				}
				.partition {
					(if (sortOrder == 1) it.pkg else it.label).lowercase().startsWith(searchPattern)
				}
			appListItems2 = importantApps + otherApps
		}
		// More complex sorting
		when (categorizeItem){
			1 -> {
				val (pendingApps,otherApps) =
					appListItems2.partition {
						(it.stopMode == 1 || it.stopMode == 2) && !CommonFunctions.isFlagSet(it.flags,ApplicationInfo.FLAG_STOPPED)
					}
				appListItems2 =
					listOf<AbstractItem>(LabelItem(ctx!!.getString(R.string.headerSection_pendingStop))) +
					pendingApps +
					listOf<AbstractItem>(LabelItem(ctx!!.getString(R.string.headerSection_otherApps))) +
					otherApps
			}
			2 -> {
				appListItems.getAggregatedUsageStats(356 * 2)
				if (appListItems.usageStatsMap != null){
					appListItems2 = appListItems2.sortedBy {
						appListItems.usageStatsMap?.get(it.pkg)?.lastTimeUsed ?: -1L
					}
				}
			}
			3 -> {
				val (sysApps,userApps) =
					appListItems2.partition {
						CommonFunctions.isFlagSet(it.flags,ApplicationInfo.FLAG_SYSTEM)
					}
				appListItems2 =
					listOf<AbstractItem>(LabelItem(ctx!!.getString(R.string.headerSection_userApps))) +
					userApps +
					listOf<AbstractItem>(LabelItem(ctx!!.getString(R.string.headerSection_sysApps))) +
					sysApps
			}
		}
		// update state string cache
		appLabelCache.clear()
		appListItems2.forEach {
			appLabelCache.put(it.pkg,StringBuilder().apply {
				// Current status (Playing media/Foreground/Unused/Inactive/Stopped)
				// Stop mode (nothing/Inactive stop/Not stopping)
				// Ignored bg free
				// Ignored running
				append(
				if (CommonFunctions.isFlagSet(it.flags,ApplicationInfo.FLAG_STOPPED)){
					ctx!!.getString(R.string.stopped)
				}
				else if (NotificationService.mediaPlaybackApps.contains(it.pkg)){
					ctx!!.getString(R.string.playingMedia)
				}
				else if (appListItems.activeAccessibilityAppList.contains(it.pkg)){
					ctx!!.getString(R.string.accessibility_svc_active);
				}
				else if (appListItems.enabledIMEAppList.contains(it.pkg)){
					ctx!!.getString(R.string.ime_active);
				}
				else if (NotificationService.persistNotificationApps.contains(it.pkg) || appListItems.isPkgRecentlyUnused(it.pkg) == false){
					ctx!!.getString(R.string.foreground);
				}
				else if (appListItems.isPkgInactive(it.pkg)){
					ctx!!.getString(R.string.inactiveState);
				}
				else if (appListItems.isPkgRecentlyUnused(it.pkg)){
					ctx!!.getString(R.string.state_unused)
				}
				else "")
				append("\n")
				append(if (false){
					ctx!!.getString(R.string.state_bgFree)
				}
				else if (it.stopMode == 0){
					ctx!!.getString(R.string.state_notStopped)
				}
				else if (it.stopMode == 1){
					if (appListItems.isAppPendingStop(it as AppItem))
						ctx!!.getString(R.string.state_pendingStop)
					else
						ctx!!.getString(R.string.state_normalStop)
				}
				else if (it.stopMode == 2){
					if (appListItems.isIgnoringBatteryOptimizations(it.pkg))
						ctx!!.getString(R.string.state_inactiveStopBattOptIgn)
					else
						ctx!!.getString(R.string.state_inactiveStop)
				}
				else "")
				if (it.ignoreRunning) append("\n").append(ctx!!.getString(R.string.ignore_running));
				if (it.ignoreBgFree)  append("\n").append(ctx!!.getString(R.string.ignore_bgFree));
				if (App.prefs!!.getBoolean("detailedAppStateInfo",false)){
					if (it.isInstalledByFDroid) append("\n").append(ctx!!.getString(R.string.installed_from_fdroid));
					if (it.isSignedByFDroid)    append("\n").append(ctx!!.getString(R.string.signed_by_fdroid));
				}
			}.toString());
		}
		// update list
		ctx!!.runOnUiThread {
			notifyDataSetChanged()
		}
	}.start()
	class AppViewHolder(binding: AppListItemBinding): ViewHolder(binding.root) {
		private val icon: ImageView = binding.icon
		private val label: TextView = binding.label
		private val state: TextView = binding.state
		fun bind(app: AbstractItem, adapter: AppListAdapter){
			Thread {
				// TODO: app.icon eats 400MB+ memory!
				if (app.icon == null) app.loadIcon()
				// TODO: crashed randomly and according to stacktrace its from here?
				icon.post { icon.setImageDrawable(app.icon) }
			}.start()
			label.text = CommonFunctions.highlightText(if (sortOrder == 1) app.pkg else app.label,adapter.searchPattern,Constants.COLOR_FG_TEXT_HIGHLIGHT)
			state.text = adapter.appLabelCache[app.pkg]
			if (app.isItemSelected){
				itemView.setBackgroundColor(adapter.bgHighlight)
			} else {
				itemView.setBackgroundColor(0) // transparent/null
			}
			itemView.setOnClickListener {
				adapter.onAppClick?.invoke(app as AppItem)
				if (app.isItemSelected){
					itemView.setBackgroundColor(adapter.bgHighlight)
				} else {
					itemView.setBackgroundColor(0) // transparent/null
				}
			}
			itemView.setOnLongClickListener {
				adapter.onAppClick?.invoke(app as AppItem)
				if (app.isItemSelected){
					itemView.setBackgroundColor(adapter.bgHighlight)
				} else {
					itemView.setBackgroundColor(0) // transparent/null
				}
				true
			}
		}
	}
	class LabelViewHolder(binding: AppListSectionHeaderBinding): ViewHolder(binding.root) {
		private val text: TextView = binding.text
		fun bind(app: AbstractItem, adapter: AppListAdapter){
			text.text = app.label
		}
	}
	companion object {
		var sortOrder: Int = App.prefs!!.getInt("sortBy",0)
		var categorizeItem: Int = App.prefs!!.getInt("categorizeItem",1)
		var sortReverse: Boolean = App.prefs!!.getBoolean("sortReverse",false)
		var showUserApp: Boolean = App.prefs!!.getBoolean("showUserApp",true)
		var showSystemApp: Boolean = App.prefs!!.getBoolean("showSystemApp",false)
	}
}
