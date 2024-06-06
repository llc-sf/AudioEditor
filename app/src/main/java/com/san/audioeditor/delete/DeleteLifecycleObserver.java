package com.san.audioeditor.delete;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import io.reactivex.rxjava3.disposables.Disposable;

class DeleteLifecycleObserver implements DefaultLifecycleObserver {
    private final Disposable disposables;

    public DeleteLifecycleObserver(Disposable disposables) {
        this.disposables = disposables;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        disposables.dispose();
        owner.getLifecycle().removeObserver(this);
    }
}
