package com.voiceapp.livedata

import androidx.lifecycle.MutableLiveData

class DistinctLiveData<T> : MutableLiveData<T>() {

    override fun setValue(value: T) {
        if (value != this.value) {
            super.setValue(value)
        }
    }
}