package com.sinc.mobile.app.features.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    // This ViewModel will manage the state of the MainScreen,
    // including the navigation drawer and top bar.
}
