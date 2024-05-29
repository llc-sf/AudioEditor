package dev.android.player.framework.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewBindingDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                binding = null
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return it }
        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Cannot access view bindings. View lifecycle is ${lifecycle.currentState}!")
        }
        return bindingInflater(thisRef.layoutInflater, thisRef.view?.parent as? ViewGroup, false)
            .also { binding = it }
    }
}

class ViewBindingDelegateForView<T : ViewBinding>(
    val view: View,
    val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T
) : ReadOnlyProperty<ViewGroup, T> {

    private var binding: T? = null

    init {
        view.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                binding = null
            }
        })
    }

    override fun getValue(thisRef: ViewGroup, property: KProperty<*>): T {
        return binding ?: createBinding(thisRef).also { binding = it }
    }

    private fun createBinding(view: ViewGroup): T {
        val inflater = LayoutInflater.from(view.context)
        return bindingInflater(inflater, view, false)
    }
}

fun <T : ViewBinding> Fragment.viewBinding(bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T) =
    ViewBindingDelegate(this, bindingInflater)

inline fun <reified T : ViewBinding> View.viewBinding(noinline bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T) =
    ViewBindingDelegateForView(this, bindingInflater)

fun View.findViewTreeLifecycleOwner(): LifecycleOwner? {
    var current: View? = this
    while (current != null) {
        val lifecycleOwner = current.getTag(R.id.view_tree_lifecycle_owner) as? LifecycleOwner
        if (lifecycleOwner != null) {
            return lifecycleOwner
        }
        val parent = current.parent
        current = if (parent is View) parent else null
    }
    return null
}