package com.uploadity.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _linkedinCode = MutableLiveData<String>("")
    val linkedinCode: LiveData<String> = _linkedinCode

    private val _test = MutableLiveData<Int>(0)
    val test: LiveData<Int> = _test

    fun setLinkedinCode(linkedinCode: String) {
        _linkedinCode.value = linkedinCode
    }

    fun setTest(test: Int) {
        _test.value = test
    }
}