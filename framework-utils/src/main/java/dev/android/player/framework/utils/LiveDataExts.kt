package dev.android.player.framework.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.*

fun <T> LiveData<T>.debounce(duration: Long, coroutineScope: CoroutineScope): LiveData<T> {
    val result = MediatorLiveData<T>()
    val source = this
    var debounceJob: Job? = null

    result.addSource(source) { value ->
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(duration)
            result.postValue(value)
        }
    }

    return result
}