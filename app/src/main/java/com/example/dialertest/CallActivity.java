package com.example.dialertest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.view.WindowManager;

import com.example.dialertest.databinding.ActivityCallBinding;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import kotlin.collections.CollectionsKt;

/**
 * Отображается при входящем звонке. Содержит логику для управления пользовательским интерфейсом
 * и взаимодействия с вызовами через объект OngoingCall.
 */
public class CallActivity extends AppCompatActivity {

    private ActivityCallBinding binding;

    /**
     * Контейнер для хранения нескольких Disposable объектов и управления ими. Disposable используется
     * для привязки и отмены подписок на Observable, чтобы избежать утечек памяти. CompositeDisposable
     * позволяет управлять несколькими Disposable объектами одновременно. В данном случае - объектами,
     * связанными с подписками на состояние вызова (state) в объекте OngoingCall.
     */
    private CompositeDisposable disposables;
    private String number;
    private OngoingCall ongoingCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ongoingCall = new OngoingCall();
        disposables = new CompositeDisposable();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        number = Objects.requireNonNull(getIntent().getData()).getSchemeSpecificPart();

        binding.answerButton.setOnClickListener(view -> ongoingCall.answer());
        binding.declineButton.setOnClickListener(view -> ongoingCall.hangup());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Добавляем две подписки на BehaviorSubject OngoingCall.state. BehaviorSubject в RxJava является
        // типом Observable, который хранит последнее отправленное значение и передает его новым подписчикам.

        // В данной подписке каждый раз, когда происходит изменение состояния вызова, вызывается метод
        // updateUi для обновления пользовательского интерфейса (UI) в соответствии с новым состоянием.
        disposables.add(
                OngoingCall.state
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer integer) {
                                updateUi(integer);
                            }
                        }));

        // В данной подписке используется метод filter для фильтрации состояния вызова и ожидается
        // состояние Call.STATE_DISCONNECTED. После этого вызов метода finish() для завершения активности
        // через 1 секунду после окончания вызова.
        disposables.add(
                OngoingCall.state
                        .filter(new Predicate<Integer>() {
                            @Override
                            public boolean test(Integer integer) {
                                return integer == Call.STATE_DISCONNECTED;
                            }
                        })
                        .delay(1, TimeUnit.SECONDS)
                        .firstElement()
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer integer) {
                                finish();
                            }
                        }));
    }

    @SuppressLint("SetTextI18n")
    private void updateUi(Integer state) {
        binding.phoneNumberTextView.setText(number);

        if (state != Call.STATE_RINGING) {
            binding.answerButton.setVisibility(View.GONE);
            binding.declineButton.setText(getString(R.string.end));
        } else {
            binding.answerButton.setVisibility(View.VISIBLE);
            binding.declineButton.setText(getString(R.string.decline));
        }

        if (CollectionsKt.listOf(new Integer[]{
                Call.STATE_DIALING,
                Call.STATE_RINGING,
                Call.STATE_ACTIVE}).contains(state)) {
            binding.declineButton.setVisibility(View.VISIBLE);
        } else
            binding.declineButton.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Отписывает все подписки и очищает объект CompositeDisposable disposables. Это важно для
        // предотвращения утечек памяти и корректного управления ресурсами в цикле жизни активности.
        disposables.clear();
    }

    /**
     * Используется для запуска активности CallActivity из других частей приложения
     * и передачи объекта Call для обработки входящего звонка.
     */
    public static void start(Context context, Call call) {
        Intent intent = new Intent(context, CallActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // Поскольку метод getHandle возаращает номер телефона в виде URI,
                // передаём его в интент с помощью метода setData().
                .setData(call.getDetails().getHandle());
        context.startActivity(intent);
    }
}