package com.game.awesa.ui.dashboard.extension

import androidx.fragment.app.Fragment
import com.game.awesa.R
import com.game.awesa.ui.dashboard.fragments.FragmentHistory
import com.game.awesa.ui.dashboard.fragments.FragmentHome
import com.game.awesa.ui.dashboard.fragments.FragmentProfile
import com.game.awesa.ui.dashboard.fragments.FragmentSettings


enum class JBNavigationPosition(val position: Int, val id: Int) {
    HOME(0, R.id.navHome),
    PROFILE(1, R.id.navProfile),
    HISTORY(2, R.id.navHistory),
    MORE(3, R.id.navMore),
}

fun findNavigationPositionById(id: Int): JBNavigationPosition = when (id) {
    JBNavigationPosition.HOME.id -> JBNavigationPosition.HOME
    JBNavigationPosition.PROFILE.id -> JBNavigationPosition.PROFILE
    JBNavigationPosition.HISTORY.id -> JBNavigationPosition.HISTORY
    JBNavigationPosition.MORE.id -> JBNavigationPosition.MORE
     else -> JBNavigationPosition.HOME
}

fun findTabByPosition(position: Int): Int = when (position) {
    0 -> R.id.navHome
    1 -> R.id.navProfile
    2 -> R.id.navHistory
    3 -> R.id.navMore
    else -> error("Invalid Tab position")
}

fun findNavigationByPosition(position: Int): JBNavigationPosition = when (position) {
    0 -> JBNavigationPosition.HOME
    1 -> JBNavigationPosition.PROFILE
    2 -> JBNavigationPosition.HISTORY
    3 -> JBNavigationPosition.MORE
    else -> JBNavigationPosition.HOME
}

fun JBNavigationPosition.createFragment(): Fragment = when (this) {
    JBNavigationPosition.HOME -> FragmentHome.newInstance()
    JBNavigationPosition.PROFILE -> FragmentProfile.newInstance()
    JBNavigationPosition.HISTORY -> FragmentHistory.newInstance()
    JBNavigationPosition.MORE -> FragmentSettings.newInstance()
 }

fun JBNavigationPosition.getTag(): String = when (this) {
    JBNavigationPosition.HOME -> FragmentHome.TAG
    JBNavigationPosition.PROFILE -> FragmentProfile.TAG
    JBNavigationPosition.HISTORY -> FragmentHistory.TAG
    JBNavigationPosition.MORE -> FragmentSettings.TAG
 }

