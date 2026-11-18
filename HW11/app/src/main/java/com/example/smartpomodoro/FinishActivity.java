package com.example.smartpomodoro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FinishActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER); root.setPadding(30, 30, 30, 30); root.setBackgroundColor(Color.WHITE);
        TextView title = new TextView(this); title.setText("🎉\n焦點會議完成！"); title.setTextSize(28); title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.CENTER);
        TextView time = new TextView(this); time.setText("00:00"); time.setTextSize(42); time.setTextColor(Color.rgb(230, 90, 65)); time.setGravity(Gravity.CENTER);
        TextView sub = new TextView(this); sub.setText("做得好，保持專注！"); sub.setTextSize(16); sub.setGravity(Gravity.CENTER);
        Button back = new Button(this); back.setText("返回主頁"); back.setOnClickListener(v -> { startActivity(new Intent(this, MainActivity.class)); finish(); });
        root.addView(title, new LinearLayout.LayoutParams(-1, 150)); root.addView(time, new LinearLayout.LayoutParams(-1, 90)); root.addView(sub, new LinearLayout.LayoutParams(-1, 60)); root.addView(back, new LinearLayout.LayoutParams(-1, 60));
        setContentView(root);
    }
}
