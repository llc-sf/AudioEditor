package dev.android.player.framework.rx;


import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RxUtils {

    public static Disposable completableOnSingle(Action action) {
        return Completable.fromAction(action)
                .subscribeOn(Schedulers.single())
                .subscribe(() -> {
                }, Throwable::printStackTrace);
    }
}
