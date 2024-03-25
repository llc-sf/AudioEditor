package dev.android.player.framework.rx

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap

object RxBus {

    private val bus = PublishSubject.create<Any>()

    private val stickyEvents = ConcurrentHashMap<Int, RxBusEvent>()

    @JvmOverloads
    @JvmStatic
    fun send(action: Int, params: Any? = null) {
        send(object : RxBusEvent {
            override fun getAction(): Int {
                return action
            }

            override fun getParams(): Any? {
                return params
            }
        })
    }

    @JvmOverloads
    @JvmStatic
    fun sendSticky(action: Int, params: Any? = null) {
        sendSticky(object : RxBusEvent {
            override fun getAction(): Int {
                return action
            }

            override fun getParams(): Any? {
                return params
            }
        })
    }

    /**
     * 发送任意类型的一个事件
     */
    @JvmStatic
    fun sendAny(any: Any) {
        bus.onNext(any)
    }

    /**
     * 发送一个事件
     */
    @JvmStatic
    fun send(ev: RxBusEvent) {
        bus.onNext(ev)
    }

    /**
     * 发送粘性事件
     */
    @JvmStatic
    fun sendSticky(ev: RxBusEvent) {
        if (!stickyEvents.containsKey(ev.getAction()))
            stickyEvents[ev.getAction()] = ev
        send(ev)
    }

    /**
     * 删除粘性事件
     */
    @JvmStatic
    fun removeStickEvent(action: Int) {
        if (stickyEvents.containsKey(action))
            stickyEvents.remove(action)
    }

    /**
     * 清除所有的粘性事件
     */
    @JvmStatic
    fun removeAllStickyEvents() {
        stickyEvents.clear()
    }

    /**
     * 普通的订阅事件
     */
    @JvmStatic
    fun toObservable(): Observable<RxBusEvent> {
        return toObservable(RxBusEvent::class.java)
    }

    /**
     * 接受指定类型的事件
     */
    @JvmStatic
    fun <T : Any> toObservable(clz: Class<T>): Observable<T> {
        return bus.ofType(clz).observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 包含粘性订阅事件
     */
    @JvmStatic
    fun toStickyObservable(): Observable<RxBusEvent> {
        val event = stickyEvents.map {
            it.value
        }.toTypedArray()
        return bus.mergeWith(Observable.fromIterable(event.asIterable()))
            .ofType(RxBusEvent::class.java).observeOn(AndroidSchedulers.mainThread())
    }
}

interface RxBusEvent {

    fun getAction(): Int

    fun getParams(): Any?
}
