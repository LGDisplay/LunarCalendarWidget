package vn.amlich.widget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Màn hình hướng dẫn thêm widget và kiểm tra việc tải ngày nghỉ. */
public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Calendar now = Calendar.getInstance();
        int[] l = LunarCalendar.solar2lunar(
                now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR));

        float d = getResources().getDisplayMetrics().density;
        int pad = (int) (24 * d);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad * 2, pad, pad);

        TextView info = new TextView(this);
        info.setTextSize(16);
        info.setText("Hôm nay: " + LunarWidgetProvider.solarText(now) + "\n"
                + "Âm lịch: " + l[0] + "/" + l[1] + (l[3] == 1 ? " nhuận" : "")
                + " năm " + LunarCalendar.canChi(l[2]) + "\n\n"
                + "Cách thêm widget:\n"
                + "1. Nhấn giữ vào chỗ trống trên màn hình chính\n"
                + "2. Chọn Widget (Tiện ích)\n"
                + "3. Tìm \"Âm lịch Việt Nam\" và kéo ra màn hình\n\n"
                + "Âm lịch tính ngay trên máy, không cần mạng. "
                + "Ngày nghỉ lễ được cập nhật từ mạng mỗi ngày.");
        root.addView(info);

        status = new TextView(this);
        status.setTextSize(14);
        status.setPadding(0, pad, 0, (int) (8 * d));
        root.addView(status);
        showStatus();

        Button refresh = new Button(this);
        refresh.setText("Cập nhật ngày nghỉ ngay");
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.setText("Đang tải...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean ok = HolidayStore.refreshNow(getApplicationContext());
                        if (ok) {
                            LunarWidgetProvider.updateAll(getApplicationContext());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showStatus();
                                if (!ok) {
                                    status.setText(status.getText()
                                            + "\n(Lần tải vừa rồi không thành công, kiểm tra mạng"
                                            + " và chắc repo GitHub để chế độ Public.)");
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        root.addView(refresh);

        setContentView(root);
    }

    private void showStatus() {
        long t = HolidayStore.lastFetched(getApplicationContext());
        if (t == 0) {
            status.setText("Ngày nghỉ: đang dùng dữ liệu đi kèm trong app, chưa tải từ mạng.");
        } else {
            String s = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(t));
            status.setText("Ngày nghỉ: đã cập nhật từ mạng lúc " + s);
        }
    }
}
