package com.example.dialertest;

import android.content.Context;
import android.graphics.PixelFormat;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * Используется для возможности не допустить входящий звонок до пользователя
 */
public class MyCallScreeningService extends CallScreeningService {

    /**
     * WindowManager для отображения и скрытия диалогового окна с информацией о входящем звонке
     */
    private WindowManager windowManager;

    /**
     * Диалоговое окно с информацией о входящем звонке
     */
    private View dialogView;

    /**
     * Используется для скрытия диалогового окна, когда звонок принят или отклонён
     */
    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    // Звонок завершен
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // Звонок принят или на удержании
                    hideSpamDialog();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // Входящий звонок
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Регистрация PhoneStateListener для отслеживания состояния вызова
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * Вызывается при входящем звонке
     */
    @Override
    public void onScreenCall(@NonNull Call.Details details) {
        CallResponse.Builder callResponseBuilder = new CallResponse.Builder();

        // Удаляем префикс "tel" у URI и извлекаем телефонный номер
        String phoneNumber = details.getHandle().getSchemeSpecificPart();

        // Проверяем, является ли номер нежелательным
        boolean isSpam = checkIfNumberIsSpam(phoneNumber);

        if (isSpam) {
            // Показываем диалоговое окно с информацией о нежелательном звонке
            showSpamDialog("Звонок от номера " + phoneNumber + " является нежелательным.");

            callResponseBuilder.setRejectCall(true);
            callResponseBuilder.setDisallowCall(true);
            callResponseBuilder.setSkipCallLog(false);
        } else {
            showSpamDialog("Отзывы: полезный звонок");
        }

        respondToCall(details, new CallResponse.Builder().build());
    }

    /**
     * Проверяет, является ли телефонный номер нежелательным
     * @param phoneNumber Номер для проверки
     * @return Истина, если номер является нежелательным, иначе - ложь
     */
    private boolean checkIfNumberIsSpam(String phoneNumber) {
        // Здесь должно быть определение, является ли номер нежелательным, например, проверка в базе данных нежелательных номеров
        return true;
    }

    /**
     * Отображает диалоговое окно с информацией о номере
     * @param message Сообщение, содержащее информацию о номере: является он нежелательным или полезным
     */
    private void showSpamDialog(String message) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialogView = inflater.inflate(R.layout.dialog_spam, null);

        TextView textView = dialogView.findViewById(R.id.text_view);
        textView.setText(message);

        windowManager.addView(dialogView, params);
    }

    /**
     * Закрывает диалоговое окно с информацией о номере
     */
    private void hideSpamDialog() {
        // Закрыть диалоговое окно при ответе на звонок
        if (windowManager != null && dialogView != null) {
            windowManager.removeViewImmediate(dialogView);
        }
    }
}