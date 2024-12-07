/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.example.ui
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.App
import com.example.CommonFunctions
// open allows it to be used as a base class for other classes
// like MainActivity based on the class below :)
open class BaseActivity: AppCompatActivity(){
	override fun onCreate(savedInstanceState:Bundle?){
		super.onCreate(savedInstanceState)
		if (CommonFunctions.isDarkThemeActive(this) && App.prefs!!.getBoolean("pitchBlack",false))
			window.decorView.setBackgroundColor(Color.BLACK)
	}
}