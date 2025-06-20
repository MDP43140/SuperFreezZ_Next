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
class AppListAdapter(): RecyclerView.Adapter<ViewHolder>(), PopupTextProvider {
	var onItemClick: ((AppItem) -> Unit)? = null
	var onItemLongClick: ((AppItem) -> Unit)? = null
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
			is AppViewHolder   -> holder.bind(appListItems2[pos])
			is LabelViewHolder -> holder.bind(appListItems2[pos])
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
		appListItems2 = appListItems.sort(
			searchPattern,
			sortOrder,
			categorizeItem,
			sortReverse,
			showUserApp,
			showSystemApp
		)
		// update state string cache
		appLabelCache.clear()
		appListItems2.forEach {
			// Current status (Playing media/Foreground/Unused/Inactive/Stopped)
			// Stop mode (nothing/Inactive stop/Not stopping)
			// Ignored bg free
			// Ignored running
			appLabelCache.put(it.pkg,StringBuilder().apply {
				append(
					when {
						CommonFunctions.isFlagSet(it.flags, ApplicationInfo.FLAG_STOPPED) ->
							ctx!!.getString(R.string.stopped)
						NotificationService.mediaPlaybackApps.contains(it.pkg) ->
							ctx!!.getString(R.string.playingMedia)
						appListItems.activeAccessibilityAppList.contains(it.pkg) ->
							ctx!!.getString(R.string.accessibility_svc_active)
						appListItems.enabledIMEAppList.contains(it.pkg) ->
							ctx!!.getString(R.string.ime_active)
						NotificationService.persistNotificationApps.contains(it.pkg) ||
						!appListItems.isPkgRecentlyUnused(it.pkg) ->
							ctx!!.getString(R.string.foreground)
						appListItems.isPkgInactive(it.pkg) ->
							ctx!!.getString(R.string.inactiveState)
						appListItems.isPkgRecentlyUnused(it.pkg) ->
							ctx!!.getString(R.string.state_unused)
						else -> ""
					}
				)
				append("\n")
				append(
					when (it.stopMode){
						0 ->
							ctx!!.getString(R.string.state_notStopped)
						1 -> if (appListItems.isAppPendingStop(it as AppItem))
							ctx!!.getString(R.string.state_pendingStop) else
							ctx!!.getString(R.string.state_normalStop)
						2 -> if (appListItems.isIgnoringBatteryOptimizations(it.pkg))
							ctx!!.getString(R.string.state_inactiveStopBattOptIgn) else
							ctx!!.getString(R.string.state_inactiveStop)
						else -> ""
					}
				)
				if (it.ignoreRunning)
					append("\n").append(ctx!!.getString(R.string.ignore_running))
				if (it.ignoreBgFree)
					append("\n").append(ctx!!.getString(R.string.ignore_bgFree))
				if (App.prefs!!.getBoolean("detailedAppStateInfo", false)){
					if (it.isInstalledByFDroid)
						append("\n").append(ctx!!.getString(R.string.installed_from_fdroid))
					if (it.isSignedByFDroid)
						append("\n").append(ctx!!.getString(R.string.signed_by_fdroid))
				}
			}.toString())
		}
		// update list
		ctx!!.runOnUiThread {
			notifyDataSetChanged()
		}
	}.start()
	inner class AppViewHolder(binding: AppListItemBinding): ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {
		private val icon: ImageView = binding.icon
		private val label: TextView = binding.label
		private val state: TextView = binding.state
		init {
			itemView.setOnClickListener(this)
			itemView.setOnLongClickListener(this)
		}
		fun bind(app: AbstractItem){
			Thread {
				// TODO: app.icon eats 400MB+ memory!
				if (app.icon == null) app.loadIcon()
				// TODO: crashed randomly and according to stacktrace its from here?
				icon.post { icon.setImageDrawable(app.icon) }
			}.start()
			label.text = CommonFunctions.highlightText(if (sortOrder == 1) app.pkg else app.label,searchPattern,Constants.COLOR_FG_TEXT_HIGHLIGHT)
			state.text = appLabelCache[app.pkg]
			itemView.setBackgroundColor(if (app.isItemSelected) bgHighlight else 0)
		}
		override fun onClick(view: View) {
			val app = appListItems2[getAbsoluteAdapterPosition()]
			onItemClick?.invoke(app as AppItem)
			itemView.setBackgroundColor(if (app.isItemSelected) bgHighlight else 0)
		}
		override fun onLongClick(view: View): Boolean {
			val app = appListItems2[getAbsoluteAdapterPosition()]
			onItemLongClick?.invoke(app as AppItem)
			itemView.setBackgroundColor(if (app.isItemSelected) bgHighlight else 0)
			return true
		}
	}
	class LabelViewHolder(binding: AppListSectionHeaderBinding): ViewHolder(binding.root) {
		private val text: TextView = binding.text
		fun bind(app: AbstractItem){
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
