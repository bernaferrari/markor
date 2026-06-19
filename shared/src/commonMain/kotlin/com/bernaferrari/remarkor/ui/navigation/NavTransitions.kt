package com.bernaferrari.remarkor.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

private fun isSettingsScreen(key: Any): Boolean = key is Screen.Settings

fun markorTransitionSpec(initialStateKey: Any, targetStateKey: Any): ContentTransform {
    return if (isSettingsScreen(targetStateKey) && !isSettingsScreen(initialStateKey)) {
        settingsEnterTogetherWithFade()
    } else {
        defaultFadeTransition()
    }
}

fun markorPopTransitionSpec(initialStateKey: Any, targetStateKey: Any): ContentTransform {
    return if (isSettingsScreen(initialStateKey) && !isSettingsScreen(targetStateKey)) {
        settingsPopTogetherWithSlide()
    } else {
        defaultFadeTransition()
    }
}

private fun settingsEnterTogetherWithFade(): ContentTransform =
    (slideInHorizontally(
        animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing),
        initialOffsetX = { width -> width },
    ) + fadeIn(
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
    )) togetherWith (
        fadeOut(
            animationSpec = tween(durationMillis = 120, easing = FastOutLinearInEasing),
        )
        )

private fun settingsPopTogetherWithSlide(): ContentTransform =
    (slideInHorizontally(
        animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing),
        initialOffsetX = { width -> -width / 10 },
    ) + fadeIn(
        animationSpec = tween(durationMillis = 160, easing = LinearOutSlowInEasing),
    )) togetherWith (
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing),
            targetOffsetX = { width -> width },
        ) + fadeOut(
            animationSpec = tween(durationMillis = 140, easing = FastOutLinearInEasing),
        )
        )

private fun defaultFadeTransition(): ContentTransform =
    fadeIn(
        animationSpec = tween(durationMillis = 160, easing = LinearOutSlowInEasing),
    ) togetherWith (
        fadeOut(
            animationSpec = tween(durationMillis = 120, easing = FastOutLinearInEasing),
        )
        )