package dev.audio.timeruler.bean

import kotlin.reflect.KProperty

class Ref<T>(val getter: () -> T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getter()
    }
}