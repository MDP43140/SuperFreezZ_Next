/*
 * SPDX-FileCopyrightText: 2025 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze
//
// Bad apps includes:
// - Privacy invasive apps (including apps with invasive ads/tracker SDKs)
// - Games (Stopping it wont affect startup speed, since it uses so much RAM that quitting it will immediately get cleared by system)
// - Social media (some important communication platforms should not be included, eg. whatsapp)
// - Annoying apps (sends spam notification, even if you disable notification its actually running in background waiting for new spam promotions)
// - Apps that runs in background and waste battery
// - Apps that has same startup speed before/after stopped
//
// Will stop these apps by default
//
// Most of these apps can be found on play store,
// open https://play.google.com/store/apps/details?id=PutYourPackageNameHere
//
val badPackages = hashSetOf<String>(

// Categories of apps:
// - Simple Mobile Tools apps (owned by Zipo Apps)
// - Androidacy apps
// - Games
// - Social Media
// - Entertainment apps
// - Financial apps
// - E-Commerce or similiar apps
// - SIM Companion apps
// - Miscellaneous

// Simple Mobile Tools is bought by Israeli company
// Zipo Apps that tries to milk profit from it
// Now its considered like most bad apps on play store
// For existing users, i strongly recommend to migrate
// to Fossify Apps instead
"com.simplemobiletools.applauncher",
"com.simplemobiletools.calculator",
"com.simplemobiletools.calendar",
"com.simplemobiletools.calendar.pro",
"com.simplemobiletools.camera",
"com.simplemobiletools.clock",
"com.simplemobiletools.contacts",
"com.simplemobiletools.contacts.pro",
"com.simplemobiletools.dialer",
"com.simplemobiletools.draw",
"com.simplemobiletools.draw.pro",
"com.simplemobiletools.filemanager",
"com.simplemobiletools.filemanager.pro",
"com.simplemobiletools.flashlight",
"com.simplemobiletools.gallery",
"com.simplemobiletools.gallery.pro",
"com.simplemobiletools.keyboard",
"com.simplemobiletools.launcher",
"com.simplemobiletools.musicplayer",
"com.simplemobiletools.notes",
"com.simplemobiletools.notes.pro",
"com.simplemobiletools.smsmessenger",
"com.simplemobiletools.thankyou",
"com.simplemobiletools.voicerecorder",

// Androidacy is a scum company,
// Their apps cannot be trusted anymore
"com.androidacy.mmm",
"com.fox2code.mmm",
"com.fox2code.mmm.fdroid",

// Games (they dont need to run in background
// at most they will try to send spam notification
// for you to play their games)
"com.AXgamesoft.TurbopropFS",
"com.aldagames.thewalkdead",
"com.aldagames.zombieshooter",
"com.dts.freefiremax",
"com.dts.freefireth",
"com.ea.game.pvz2_row",
"com.ea.game.pvzfree_row",
"com.ea.gp.fifamobile",
"com.feelingtouch.zf3d",
"com.gof.global",
"com.jundroo.SimpleRockets2",
"com.kiloo.subwaysurf",
"com.king.candycrushsaga",
"com.maxgames.stickwarlegacy",
"com.mobile.legends",
"com.mojang.minecraftpe",
"com.neptune.domino",
"com.nianticlabs.pokemongo",
"com.playdead.limbo.full",
"com.PoxelStudios.DudeTheftAuto",
"com.RimaStudio.JurnalMalamPaid",
"com.roblox.client",
"com.rockstargames.gtasa",
"com.supercell.clashofclans",
"com.tencent.ig",
"com.wildspike.wormszone",
"com.yg.mini.games",
"it.rortos.extremelandings",
"it.rortos.realflightsimulator",
"jp.garud.ssimulator",
"net.apex_designs.payback2",

// Social media
"com.facebook.katana",
"com.facebook.lite",
"com.facebook.mlite",
"com.facebook.orca",
"com.instagram.android",
"com.instagram.lite",
"com.kwai.bulldog",
"com.lemon.lvoverseas",
"com.pinterest",
"com.ss.android.ugc.tiktok.livewallpaper",
"com.ss.android.ugc.trill",
"com.ss.android.ugc.aweme.mobile", // is this Douyin? (chinese tiktok)
"com.tiktokshop.seller",
"com.twitter",
"com.twitter.lite",
"omegle.tv",
"sg.bigo.live",

// Entertainment apps ("film/movie")
// they send spam notification too
"com.berkahomah.lk21.nontonfilmsubindo.gratis.dunia21.indoxx1.nontonfilmgratis",
"com.bimlind.bioskop_21",
"com.cianjurartstudio.tv",
"com.freemovies2019.watchhdmovieonline",
"com.storymatrix.drama",
"live.shorttv.apps",
"premium.gotube.adblock.utube",
"sg.omi",
"tv.hooq.android",
"xyz.gl.moviein",

// Financial apps (they're privacy
// nightmare and sends spam notifications
// every hour)
"com.bca.mybca.omni.android",
"com.bcadigital.blu",
"com.finaccel.android",
"com.gojek.app",
"com.gojek.gopay",
"com.grabtaxi.passenger",
"id.bmri.livin",
"id.bni.wondr",
"id.co.bni.tapcashgo",
"id.co.bri.brimo",
"id.dana",
"io.silvrr.installment",
"ovo.id",

// E-commerce apps (or similiar)
// Same as financial apps, but worse
"com.dafturn.mypertamina",
"com.lazada.android",
"com.shopee.id",
"com.shopee.lite.id",
"com.shopeepay.id",
"com.tokopedia.tkpd",

// SIM Companion app (dont need to run in background,
// they mostly send spam notification)
"com.apps.MyXL",
"com.axis.net",
"com.byu.id",
"com.linkit.bimatri",
"com.telkomsel.telkomselcm",
"com.pure.indosat.care",

// Others
"app.source.getcontact",
"cn.wps.moffice_eng",
"com.bergel.tauros",
"com.bigwinepot.nwdn.international",
"com.dubox.drive",
"com.foxdebug.acode.free", // play store version has too much bad stuff (ads, proprietary codes, Play Store only DRM) compared to the F-Droid version
"com.indosk.sensid",
"com.intsig.camscanner",
"com.lenovo.anyshare.gps",
"com.opera.browser",
"com.simejikeyboard",
"com.speedy.vpn",
"com.taxsee.taxsee",
"com.wejoy.weplay",
"com.whatsapp.w4b", // i hope no avg user uses this
"e.books.reading.apps",
"free.vpn.unblock.proxy.turbovpn",
"free.vpn.unblock.proxy.vpn.master.pro",
"com.ipopcorn.xnxbrowser",
"com.michatapp.im",
"com.techlastudio.bfbrowser",
"com.uc.browser.en",
"com.UCMobile.intl",
"com.vpntechstudioinc.mobiledevca.vpnbrowserantiblokir",
"com.zhiliaoapp.musically.go",
"org.zwanoo.android.speedtest",
// "com.spotify.music", // if this plays music, its dead now...
"com.asus.themeapp",
"com.baviux.voicechanger",
"com.bitsmedia.android.muslimpro",
"com.emoji.coolkeyboard",
"com.google.android.music",
"com.google.android.videos",
"com.duokan.phone.remotecontroller",
"com.openai.chatgpt",
)
