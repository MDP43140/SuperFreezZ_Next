/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze.ui
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import io.mdp43140.superfreeze.App
import io.mdp43140.superfreeze.AppListItems
import io.mdp43140.superfreeze.CommonFunctions
import io.mdp43140.superfreeze.databinding.ActivitySettingsBinding
import io.mdp43140.superfreeze.util.putData // SharedPreferencesExtension.kt
import io.mdp43140.superfreeze.R
class SettingsActivity: BaseActivity(){
	private lateinit var binding: ActivitySettingsBinding
	private val importFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		if (uri != null){
			val jsonObject = JSONObject(
				contentResolver.openInputStream(uri)
					?.use { it.readBytes() }
					!!.toString(Charsets.UTF_8)
			)
			val appsObject = jsonObject.getJSONObject("apps")
			val buffer = StringBuilder();

			val prefsEditor1 = AppListItems.prefs?.edit()
			val prefsEditor2 = App.prefs?.edit()

			for (key in appsObject.keys()){
				val value = appsObject.get(key)
				if (prefsEditor1?.putData(key,value) == false)
					buffer.append("\n- $key: $value")
			}
			for (key in jsonObject.keys()){
				val value = jsonObject.get(key)
				if (key != "apps" && prefsEditor2?.putData(key,value) == false)
					buffer.append("\n- $key: $value")
			}
			prefsEditor1?.apply()
			prefsEditor2?.apply()
			val text = buffer.toString()
			Snackbar.make(
				binding.root,
				if (text == "") "Settings imported!" else "Unsupported data type:${text}",
				Snackbar.LENGTH_SHORT
			).let {
				it.setAction(getString(android.R.string.ok)){ _: View ->
					it.dismiss()
				}
				it.show()
			}
			loadFragment()
		}
	}
	private val exportFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
		if (uri != null){
			// TODO - warning - Type mismatch: inferred type is (Mutable)Map<String!, *>? but (MutableMap<Any?, Any?>..Map<*, *>) was expected
			val jsonData = JSONObject(App.prefs?.all)
			jsonData.put("apps",JSONObject(AppListItems.prefs?.all))
			CommonFunctions.writeContentToUri(this,uri,jsonData.toString())
			Snackbar.make(
				binding.root,
				"Settings exported!",
				Snackbar.LENGTH_SHORT
			).let {
				it.setAction(getString(android.R.string.ok)){ _: View ->
					it.dismiss()
				}
				it.show()
			}
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySettingsBinding.inflate(layoutInflater)
		setContentView(binding.root)
		loadFragment()
	}
	override fun onBackPressed(){
		// suggested fix by LeakCanary
		super.onBackPressed()
		finishAfterTransition()
	}
	fun loadFragment(){
		val preferenceFragment = MyPreferenceFragment()
		/* TODO: is there a better way to do this? */
		preferenceFragment.importFileLauncher = importFileLauncher
		preferenceFragment.exportFileLauncher = exportFileLauncher
		supportFragmentManager.beginTransaction()
			.replace(R.id.layout, preferenceFragment)
			.commit()
	}
	class MyPreferenceFragment: PreferenceFragmentCompat() {
		lateinit var importFileLauncher: ActivityResultLauncher<Array<String>>
		lateinit var exportFileLauncher: ActivityResultLauncher<String>
		override fun onCreate(savedInstanceState: Bundle?) {
			super.onCreate(savedInstanceState)
			findPreference<Preference>("dataImport")?.onPreferenceClickListener =
				Preference.OnPreferenceClickListener {
					importFileLauncher.launch(arrayOf("application/json"))
					false
				}
			findPreference<Preference>("dataExport")?.onPreferenceClickListener =
				Preference.OnPreferenceClickListener {
					exportFileLauncher.launch("SuperFreezZ_Next_settings.json")
					false
				}
			findPreference<Preference>("theme")?.onPreferenceChangeListener =
				Preference.OnPreferenceChangeListener { _:Preference, value:Any? ->
					val themeStr = resources.getStringArray(R.array.themes_key)
					AppCompatDelegate.setDefaultNightMode(when (value){
						themeStr[1] -> AppCompatDelegate.MODE_NIGHT_NO
						themeStr[2] -> AppCompatDelegate.MODE_NIGHT_YES
						else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
					})
					true
				}
		}
		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.prefs,rootKey)
		}
	}
}
