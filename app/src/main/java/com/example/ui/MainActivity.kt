/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.example.ui
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.databinding.ActivityMainBinding
import com.example.R
class MainActivity: BaseActivity(){
	private lateinit var binding: ActivityMainBinding
	override fun onCreate(savedInstanceState: Bundle?){
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
	}
}