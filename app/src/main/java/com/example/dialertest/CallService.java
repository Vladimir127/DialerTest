package com.example.dialertest;

import android.telecom.Call;
import android.telecom.InCallService;

/**
 * Класс InCallService предоставляет функциональность для управления вызовами в приложении
 */
public class CallService extends InCallService {
    /**
     * Вызывается, когда новый вызов добавлен в систему
     */
    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);

        // Создаём новый объект OngoingCall, устанавливаем ему вызов и запускаем CallActivity
        new OngoingCall().setCall(call);
        CallActivity.start(this, call);
    }

    /**
     * Вызывается, когда вызов удаляется из системы (например, когда вызов завершается)
     */
    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);

        // Устанавливаем в качестве вызова значение null.
        new OngoingCall().setCall(null);
    }
}
