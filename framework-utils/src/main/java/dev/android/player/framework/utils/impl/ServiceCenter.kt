package dev.android.player.framework.utils.impl

object ServiceCenter {

    private val services = mutableMapOf<Class<*>, Any>()

    fun <T : Any> registerService(serviceClass: Class<T>, implementation: T) {
        services[serviceClass] = implementation
    }

    fun <T : Any> getService(serviceClass: Class<T>): T {
        return services[serviceClass] as T
    }

}