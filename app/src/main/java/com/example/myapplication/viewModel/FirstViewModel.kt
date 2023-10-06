package com.example.myapplication.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FirstViewModel : ViewModel() {
    val counter : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    init{
        counter.value = 0
    }
}