/*
 * SPDX-FileCopyrightText: 2024 MDP43140
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
import io.mdp43140.superfreeze.App
import io.mdp43140.superfreeze.AppListItems
import io.mdp43140.superfreeze.AppListItems.AbstractItem
import io.mdp43140.superfreeze.AppListItems.AppItem
import io.mdp43140.superfreeze.CommonFunctions
import io.mdp43140.superfreeze.Constants
import io.mdp43140.superfreeze.R
class AppListAdapter(
	private val onAppClick: ((AppItem) -> Unit)?
): RecyclerView.Adapter<ViewHolder>(){
	lateinit var appListItems: AppListItems
	lateinit var appListItems2: List<AbstractItem>
	private var ctx: Activity? = null
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
			else                      -> throw IllegalArgumentException("Invalid view type")
		}
	}
	override fun onBindViewHolder(holder: ViewHolder, index: Int) {
		(holder as AppViewHolder).bind(appListItems2[index],this)
	}
	override fun getItemCount(): Int = appListItems2.size
	override fun getItemId(index: Int): Long {
		return appListItems2[index].pkg.hashCode().toLong()
	}
	override fun getItemViewType(index: Int): Int {
		return when (appListItems2[index]){
			is AppItem   -> Constants.VIEW_TYPE_APP
			else         -> throw IllegalArgumentException("Invalid view type")
		}
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
	fun sort() = Thread {
		// Sort by A-Z
		appListItems2 = appListItems.appList.sortedBy { it.label.lowercase() }
		// update list
		ctx!!.runOnUiThread {
			notifyDataSetChanged()
		}
	}.start()
	class AppViewHolder(binding: AppListItemBinding): ViewHolder(binding.root) {
		private val icon: ImageView = binding.icon
		private val label: TextView = binding.label
		fun bind(app: AbstractItem, adapter: AppListAdapter){
			Thread {
				// TODO: app.icon eats 400MB+ memory!
				if (app.icon == null) app.loadIcon()
				// TODO: crashed randomly and according to stacktrace its from here?
				icon.post { icon.setImageDrawable(app.icon) }
			}.start()
			label.text = app.label
		}
	}
}
