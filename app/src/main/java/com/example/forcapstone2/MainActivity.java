package com.example.forcapstone2;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ImageButton informationButton; // 정보 팝업 버튼 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyApp myApp = (MyApp) getApplication(); // 앱 전체에서 사용되는 변수에 접근하기 위해 MyApp 인스턴스 생성


        createNotificationChannel(); // 알림 채널 생성 메소드 호출

        ImageView drainIcon = findViewById(R.id.drainIcon); // 물 버림 버튼
        ImageView drinkIcon = findViewById(R.id.drinkIcon); // 물 채움 버튼
        ImageView buttonSetting = findViewById(R.id.buttonSetting); // 설정창 액티비티로 이동하는 버튼
        ImageView statisticsIcon = findViewById(R.id.statisticsIcon); // 통계 화면으로 이동하는 아이콘
        ImageView bluetoothIcon = findViewById(R.id.bluetoothIcon); // 블루투스 설정 액티비티로 이동하는 아이콘
        ImageView reloadIcon = findViewById(R.id.reloadIcon); // 새로고침 아이콘
        Button format = findViewById(R.id.format);

        // activity_main.xml의 TextView에 목표량 연결
        TextView purposewaterAmountText = findViewById(R.id.PurposewaterAmountText);
        String goalAmountText = "목표량 " + String.valueOf((float) myApp.getGoalAmount()/1000 + "L");
        purposewaterAmountText.setText(goalAmountText);

        setMainTextVeiw(myApp.getTodayAmount(), myApp.getGoalAmount(), myApp.getBeforeAmount());

       resetAlarm(MainActivity.this);

        final WaterCupView waterCupView = findViewById(R.id.waterCupView); // 물컵 뷰

        // 오늘 마신양 초기화하는 버튼
        format.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myApp.formatTodayAmount();
                setMainTextVeiw(myApp.getTodayAmount(), myApp.getGoalAmount(), myApp.getBeforeAmount());
                waterCupView.setVisibility(View.VISIBLE);
            }
        });

        // 물버림 버튼 클릭 이벤트
        drainIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myApp.drainAmount();

                TextView waterAmountText = findViewById(R.id.waterAmountText);
                String todayAmountText = String.valueOf(myApp.getTodayAmount()) + "mL";
                waterAmountText.setText(todayAmountText);

                setMainTextVeiw(myApp.getTodayAmount(), myApp.getGoalAmount(), myApp.getBeforeAmount());

                Toast.makeText(getApplicationContext(), "물을 버렸어요!", Toast.LENGTH_SHORT).show();
            }
        });

        // 물 채움 버튼 클릭 이벤트
        drinkIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myApp.drinkAmount();

                TextView waterAmountText = findViewById(R.id.waterAmountText);
                String todayAmountText = String.valueOf(myApp.getTodayAmount()) + "mL";
                waterAmountText.setText(todayAmountText);

                int percentage = (int) ((float) myApp.getTodayAmount() / myApp.getGoalAmount() * 100);
                TextView amountPercent = findViewById(R.id.amountPercent);
                String percent = String.valueOf(percentage) + " %";
                amountPercent.setText(percent);

                // 완성되면 지울 것. 테스트용
                TextView nowAmount = findViewById(R.id.nowAmount);
                String now = "현재 텀블러 측정값 : " + String.valueOf(myApp.getBeforeAmount());
                nowAmount.setText(now);

                Toast.makeText(getApplicationContext(), "물을 추가했어요!", Toast.LENGTH_SHORT).show();
            }
        });

        // 새로고침 아이콘 클릭 이벤트
        reloadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myApp.reloadAmount();

                TextView waterAmountText = findViewById(R.id.waterAmountText);
                String todayAmountText = String.valueOf(myApp.getTodayAmount()) + "mL";
                waterAmountText.setText(todayAmountText);

                if (myApp.getTodayAmount() > myApp.getGoalAmount()) {
                    waterCupView.setVisibility(View.GONE);
                }

                setMainTextVeiw(myApp.getTodayAmount(), myApp.getGoalAmount(), myApp.getBeforeAmount());

                Toast.makeText(getApplicationContext(), "새로고침 성공!", Toast.LENGTH_SHORT).show(); // 새로고침 성공 토스트 메시지
            }
        });


        // 설정창 버튼 클릭 이벤트
        buttonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent); // 설정 액티비티 시작
            }
        });

        // 통계 아이콘 클릭 이벤트
        statisticsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Stat.class);
                startActivity(intent); // 통계 액티비티 시작
            }
        });

        // 블루투스 아이콘 클릭 이벤트
        bluetoothIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothConnect.class);
                startActivity(intent); // 블루투스 연결 액티비티 시작
            }
        });

        informationButton = findViewById(R.id.informationButton); // 정보 팝업 버튼

        // 정보 팝업 버튼 클릭 이벤트
        informationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInformationPopup(); // 정보 팝업창 표시 메소드 호출
            }
        });
    }

    private void setMainTextVeiw(int todayAmount, int goalAmount, int beforeAmount) {
        // activity_main.xml의 TextView에 연결해서 퍼센트 계산
        int percentage = (int) ((float) todayAmount / goalAmount * 100);
        TextView amountPercent = findViewById(R.id.amountPercent);
        String percent = String.valueOf(percentage) + " %";
        amountPercent.setText(percent);

        // activity_main.xml의 TextView에 오늘 마신양 연결
        TextView waterAmountText = findViewById(R.id.waterAmountText);
        String todayAmountText = String.valueOf(todayAmount) + "mL";
        waterAmountText.setText(todayAmountText);

        // 완성되면 지울 것. 테스트용
        TextView nowAmount = findViewById(R.id.nowAmount);
        String now = "현재 물 측정값 : " + String.valueOf(beforeAmount);
        nowAmount.setText(now);
    }

    /**
     * 정보 팝업창을 표시하는 메소드
     */
    private void showInformationPopup() {
        Dialog dialog = new Dialog(MainActivity.this, R.style.RoundedDialog);
        dialog.setContentView(R.layout.popup_activity);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        params.width = (int) (width * 0.85);
        params.height = (int) (height * 0.7);
        params.gravity = Gravity.CENTER;

        window.setAttributes(params);
        dialog.show();
    }

    /**
     * 알림 채널을 생성하는 메소드 (Android 8.0 이상에서 필요)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name); // 채널 이름
            String description = getString(R.string.channel_description); // 채널 설명
            int importance = NotificationManager.IMPORTANCE_DEFAULT; // 중요도 설정
            NotificationChannel channel = new NotificationChannel("GOALATTAINMENT_CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel); // 시스템에 채널 등록
        }
    }

     /* 목표 달성 시 알림을 생성하는 메소드
    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "GOALATTAINMENT_CHANNEL_ID")
                .setSmallIcon(R.drawable.hirue) // 알림에 보여질 아이콘 설정
                .setContentTitle("목표 달성 알림") // 알림 제목 설정
                .setContentText("축하합니다~!! 일일 목표 달성했습니다!") // 알림 내용 설정
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // 알림의 우선순위 설정

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 리턴
            return;
        }
        notificationManager.notify(1, builder.build()); // 알림 발생
    }*/

    public static void resetAlarm(Context context) {
        AlarmManager resetAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent resetIntent = new Intent(context, Initialize.class);
        PendingIntent resetSender = PendingIntent.getBroadcast(context, 0, resetIntent, PendingIntent.FLAG_IMMUTABLE);

        // 자정 시간
        Calendar resetCal = Calendar.getInstance();
        resetCal.setTimeInMillis(System.currentTimeMillis());
        resetCal.set(Calendar.HOUR_OF_DAY, 0);
        resetCal.set(Calendar.MINUTE, 0);
        resetCal.set(Calendar.SECOND, 0);

        //다음날 0시에 맞추기 위해 24시간을 뜻하는 상수인 AlarmManager.INTERVAL_DAY를 더해줌.
        resetAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis()
                + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, resetSender);
    }
}