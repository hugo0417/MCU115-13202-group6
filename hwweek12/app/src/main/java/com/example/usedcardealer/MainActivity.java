package com.example.usedcardealer;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private CarDatabaseHelper db;
    private EditText brandInput, modelInput, yearInput, priceInput, searchInput;
    private LinearLayout resultList;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        db = new CarDatabaseHelper(this);
        buildScreen();
        refreshList(null);
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(24));
        root.setBackgroundColor(0xFFF5F7FA);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("中古車商車輛資料庫"); title.setTextSize(24); title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF17365D); title.setPadding(0, 0, 0, dp(12)); root.addView(title);
        TextView hint = new TextView(this);
        hint.setText("輸入車輛資料後，可新增、修改、刪除及查詢。點選下方資料可載入編輯。");
        hint.setTextSize(14); hint.setTextColor(0xFF555555); root.addView(hint);

        brandInput = addField(root, "車子廠牌（主鍵，不可重複）");
        modelInput = addField(root, "車型");
        yearInput = addField(root, "年份（西元，例如 2020）");
        yearInput.setInputType(2);
        priceInput = addField(root, "價格（元）");
        priceInput.setInputType(8194);

        LinearLayout row1 = new LinearLayout(this);
        addButton(row1, "新增", v -> addCar()); addButton(row1, "修改", v -> updateCar());
        root.addView(row1);
        LinearLayout row2 = new LinearLayout(this);
        addButton(row2, "刪除", v -> deleteCar()); addButton(row2, "清空欄位", v -> clearFields());
        root.addView(row2);

        TextView searchTitle = new TextView(this);
        searchTitle.setText("查詢車輛（依廠牌或車型）"); searchTitle.setTextSize(17);
        searchTitle.setTypeface(null, Typeface.BOLD); searchTitle.setTextColor(0xFF17365D);
        searchTitle.setPadding(0, dp(18), 0, dp(5)); root.addView(searchTitle);
        searchInput = new EditText(this); searchInput.setSingleLine(true); searchInput.setHint("輸入關鍵字");
        root.addView(searchInput, matchWrap());
        LinearLayout searchRow = new LinearLayout(this);
        addButton(searchRow, "查詢", v -> refreshList(searchInput.getText().toString().trim()));
        addButton(searchRow, "顯示全部", v -> { searchInput.setText(""); refreshList(null); });
        root.addView(searchRow);

        TextView listTitle = new TextView(this);
        listTitle.setText("車輛資料列表（點選項目可編輯）"); listTitle.setTextSize(17);
        listTitle.setTypeface(null, Typeface.BOLD); listTitle.setTextColor(0xFF17365D);
        listTitle.setPadding(0, dp(18), 0, dp(8)); root.addView(listTitle);
        resultList = new LinearLayout(this); resultList.setOrientation(LinearLayout.VERTICAL); root.addView(resultList);
        setContentView(scroll);
    }

    private EditText addField(LinearLayout root, String hint) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setTextSize(16); e.setHint(hint);
        root.addView(e, matchWrap()); return e;
    }
    private void addButton(LinearLayout row, String label, View.OnClickListener action) {
        Button b = new Button(this); b.setText(label); b.setOnClickListener(action);
        row.addView(b, new LinearLayout.LayoutParams(0, dp(50), 1));
    }
    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(-1, dp(52)); }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private String[] readFields() {
        String brand = brandInput.getText().toString().trim();
        String model = modelInput.getText().toString().trim();
        String yearText = yearInput.getText().toString().trim();
        String priceText = priceInput.getText().toString().trim();
        if (brand.isEmpty() || model.isEmpty() || yearText.isEmpty() || priceText.isEmpty()) {
            toast("請完整輸入廠牌、車型、年份及價格"); return null;
        }
        try {
            int year = Integer.parseInt(yearText); double price = Double.parseDouble(priceText);
            if (year < 1886 || year > 2100 || price < 0) throw new NumberFormatException();
            return new String[]{brand, model, String.valueOf(year), String.valueOf(price)};
        } catch (NumberFormatException ex) { toast("年份或價格格式錯誤，請重新輸入"); return null; }
    }
    private void addCar() {
        String[] x = readFields(); if (x == null) return;
        boolean ok = db.insertCar(x[0], x[1], Integer.parseInt(x[2]), Double.parseDouble(x[3]));
        toast(ok ? "新增成功" : "新增失敗：廠牌可能已存在（主鍵不可重複）"); refreshList(null);
    }
    private void updateCar() {
        String[] x = readFields(); if (x == null) return;
        boolean ok = db.updateCar(x[0], x[1], Integer.parseInt(x[2]), Double.parseDouble(x[3]));
        toast(ok ? "修改成功" : "修改失敗：找不到此廠牌"); refreshList(null);
    }
    private void deleteCar() {
        String brand = brandInput.getText().toString().trim();
        if (brand.isEmpty()) { toast("請先輸入要刪除的車子廠牌"); return; }
        boolean ok = db.deleteCar(brand); toast(ok ? "刪除成功" : "刪除失敗：找不到此廠牌");
        clearFields(); refreshList(null);
    }
    private void clearFields() {
        brandInput.setText(""); modelInput.setText(""); yearInput.setText(""); priceInput.setText("");
    }
    private void refreshList(String keyword) {
        resultList.removeAllViews();
        Cursor c = (keyword == null || keyword.isEmpty()) ? db.getAllCars() : db.searchCar(keyword);
        int count = 0;
        try {
            while (c.moveToNext()) {
                count++;
                final String brand = c.getString(c.getColumnIndexOrThrow(CarDatabaseHelper.BRAND));
                final String model = c.getString(c.getColumnIndexOrThrow(CarDatabaseHelper.MODEL));
                final String year = c.getString(c.getColumnIndexOrThrow(CarDatabaseHelper.YEAR));
                final String price = c.getString(c.getColumnIndexOrThrow(CarDatabaseHelper.PRICE));
                TextView item = new TextView(this);
                item.setText(String.format(Locale.getDefault(), "廠牌：%s\n車型：%s　年份：%s\n價格：NT$ %,.0f\n（點選載入編輯）", brand, model, year, Double.parseDouble(price)));
                item.setTextSize(15); item.setTextColor(0xFF222222); item.setPadding(dp(12), dp(12), dp(12), dp(12));
                item.setBackgroundColor(0xFFFFFFFF);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = dp(8);
                resultList.addView(item, lp);
                item.setOnClickListener(v -> { brandInput.setText(brand); modelInput.setText(model); yearInput.setText(year); priceInput.setText(price); });
            }
        } finally { c.close(); }
        if (count == 0) {
            TextView empty = new TextView(this); empty.setText("目前沒有符合條件的車輛資料");
            empty.setTextColor(0xFF666666); empty.setPadding(dp(8), dp(12), dp(8), dp(12)); resultList.addView(empty);
        }
    }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    @Override protected void onDestroy() { super.onDestroy(); if (db != null) db.close(); }
}
