package com.example.dialertest;

import android.telecom.Call;
import android.telecom.VideoProfile;
import io.reactivex.subjects.BehaviorSubject;
import androidx.annotation.Nullable;

/**
 * Класс для управления текущим вызовом (call) в приложении
 */
public class OngoingCall {

    /**
     * Представляет собой Observable, который может выдавать последнее значение, которое было передано
     * в него или значение по умолчанию, если ничего не было передано. В данном случае используется
     * для отслеживания состояния текущего вызова (call).
     */
    public static BehaviorSubject<Integer> state = BehaviorSubject.create();

    /**
     * Представляет собой вызов в системе телефонии Android. Он содержит методы для управления вызовом,
     * такие как ответ на вызов, завершение вызова, установка состояния и т. д.
     */
    private static Call call;

    /**
     * Предоставляет колбэк-методы для отслеживания изменений состояния вызова
     */
    private final Object callback = new Call.Callback() {
        /**
         * Вызывается при изменении состояния вызова и обновляет состояние в BehaviorSubject.
         */
        @Override
        public void onStateChanged(Call call, int newState) {
            super.onStateChanged(call, newState);
            state.onNext(newState);
        }
    };

    /**
     * Метод для установки текущего вызова
     */
    public final void setCall(@Nullable Call value) {
        // Если вызов уже установлен, снимаем у него регистрацию колбэка
        if (call != null) {
            call.unregisterCallback((Call.Callback)callback);
        }

        // Если передан новый вызов (параметр value), регистрируем для него колюэк и передаём в BehaviorSubject новое состояние
        if (value != null) {
            value.registerCallback((Call.Callback)callback);
            state.onNext(value.getState());
        }

        call = value;
    }

    /**
     * Метод ответа на вызов
     */
    public void answer() {
        assert call != null;
        call.answer(VideoProfile.STATE_AUDIO_ONLY);
    }

    /**
     * Метод завершения вызова
     */
    public void hangup() {
        assert call != null;
        call.disconnect();
    }
}
