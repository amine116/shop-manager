package com.amine.bmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        ValueEventListener{

    private static DatabaseReference reserveRef, soldRef, rootRef;
    private static File selectedShop;
    private static ArrayList<Product> reservedProducts;
    private static ArrayList<SoldProduct> todaySell;
    public static final String reserves = "reserves", sold = "sold", appName = "bManager";
    private ListView lstTodayCalc;
    private BaseAdapter adapter;
    private TextView txtTotalSell, txtTotalDue;
    private final DecimalFormat df =  new DecimalFormat("0.#");
    private static double totalSell = 0.0, totalDue = 0.0;
    private FirebaseAuth auth;
    public static String shopName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent = new Intent(MainActivity.this, ActivityLogIn.class);
            startActivity(intent);
            finish();
            return;
        }

        init();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.menu_selectShop){

            Intent intent = new Intent(MainActivity.this, ShopSelection.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.menu_statistics){
            Intent intent = new Intent(MainActivity.this, ActivityStatistics.class);
            startActivity(intent);
        }

        if(id == R.id.menu_login){

            if(auth.getCurrentUser() != null){
                Toast.makeText(this, "You are already Logged In", Toast.LENGTH_LONG).show();
            }
            else{
                Intent intent = new Intent(MainActivity.this, ActivityLogIn.class);
                startActivity(intent);
            }

            return true;
        }

        if(id == R.id.menu_changePass){

            WantPermission perm = new WantPermission(this, 1);
            perm.show();

            return true;
        }

        if (id == R.id.menu_logout){

            WantPermission perm = new WantPermission(this, 2);
            perm.show();

            return true;
        }

        if (id == R.id.menu_about) {

            Intent i = new Intent(MainActivity.this, ActivityAbout.class);
            startActivity(i);

            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    private void init(){

        findViewById(R.id.main_todayCalcLayout).setVisibility(View.INVISIBLE);
        findViewById(R.id.main_progress).setVisibility(View.VISIBLE);
        initialize();
        readProductsFromStorage(() -> {
            findViewById(R.id.main_todayCalcLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.main_progress).setVisibility(View.GONE);
            findViewById(R.id.main_buttonLayout).setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            String s = "টোটাল বিক্রিত মূল্যঃ " + df.format(totalSell);
            txtTotalSell.setText(s);
            s = "টোটাল বাকীঃ " + df.format(totalDue);
            txtTotalDue.setText(s);
            TextView tv = findViewById(R.id.main_txtNoSellToday);
            if(todaySell.size() == 0){
                tv.setVisibility(View.VISIBLE);
                s = "No Sell Today!";
                tv.setText(s);
            }else{
                tv.setVisibility(View.GONE);
            }


        });
        deleteInToday();

    }

    private void deleteInToday(){
        DatabaseReference r = rootRef.child("justTodaySell");
        r.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String date = getTodayDate();
                for(DataSnapshot dates : snapshot.getChildren()){
                    String temp = dates.getKey();
                    if(temp != null){
                        if(!temp.equals(date)) r.child(temp).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface ReadFromStorage{
        void onCallback();
    }

    private void initializeAdapter(){
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return todaySell.size();
            }

            @Override
            public Object getItem(int position) {
                return todaySell.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                LayoutInflater inflater =
                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                view = inflater.inflate(R.layout.main_today_sell_list, null);

                TextView pName = view.findViewById(R.id.main_todaySell_productName),
                        pCode = view.findViewById(R.id.main_todaySell_productCode),
                        cName = view.findViewById(R.id.main_todaySell_customerName),
                        sellAmount = view.findViewById(R.id.main_todaySell_sellAmount),
                        payable = view.findViewById(R.id.main_todaySell_payable),
                        paid = view.findViewById(R.id.main_todaySell_paid),
                        due = view.findViewById(R.id.main_todaySell_due),
                        phone = view.findViewById(R.id.main_todaySell_phone);

                String s = "পণ্যের নামঃ " + todaySell.get(position).getProductName();
                pName.setText(s);
                s = "কোডঃ " + todaySell.get(position).getProductCode();
                pCode.setText(s);
                s = "ক্রেতাঃ " + todaySell.get(position).getBuyerName();
                cName.setText(s);
                s = "বিক্রয়ের পরিমানঃ " + df.format(Double.parseDouble(todaySell.get(position).getSellAmount()));
                sellAmount.setText(s);
                s = "মোট মূল্যঃ " + df.format(Double.parseDouble(todaySell.get(position).getPayableAmount()));
                payable.setText(s);
                s = "পরিষোধঃ " + df.format(Double.parseDouble(todaySell.get(position).getPaid()));
                paid.setText(s);
                s = "বাকীঃ " + df.format(Double.parseDouble(todaySell.get(position).getDue()));
                due.setText(s);
                s = "মোবাইলঃ " + todaySell.get(position).getPhone();
                phone.setText(s);

                double du = Double.parseDouble(todaySell.get(position).getDue());
                if(du > 0.0){
                    due.setTextSize(20);
                    due.setBackgroundColor(Color.CYAN);
                    due.setTextColor(Color.RED);
                }

                return view;
            }
        };
        lstTodayCalc.setAdapter(adapter);
    }


    private void initializeRef(){

        String selectedShopName = getShopName();
        shopName = selectedShopName;
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.setTitle(selectedShopName);
        if(selectedShopName.equals("")){
            Intent intent = new Intent(MainActivity.this, ShopSelection.class);
            startActivity(intent);
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        rootRef = db.getReference().child(appName).child(selectedShopName);
        reserveRef = rootRef.child(reserves);
        soldRef = rootRef.child(sold);

    }

    private String getShopName(){

        File shopPath = getExternalFilesDir("bManager");
        if(shopPath.mkdir()){
            Log.i("shopPath", "created");
        }else{
            Log.i("shopPath", "Exists");
        }
        selectedShop = new File(shopPath, "Selected Shop.txt");
        try {
            if(selectedShop.createNewFile()){
                Log.i("selectedShop", "Created");
            }else{
                Log.i("selectedShop", "Exists");
            }
        }catch (Exception e){
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }


        String name = "";
        try {
            Scanner scanner = new Scanner(selectedShop);

            if(scanner.hasNextLine()) name = scanner.nextLine();
            else name = "";
            scanner.close();
        }catch (Exception e){
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return name;
    }

    private void initialize(){
        initializeRef();

        reservedProducts = new ArrayList<>();
        todaySell = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        txtTotalDue = findViewById(R.id.main_TotalDue);
        txtTotalSell = findViewById(R.id.main_TotalSell);
        lstTodayCalc = findViewById(R.id.main_lstTodayCalc);
        initializeAdapter();
        findViewById(R.id.mainBtnAddProduct).setOnClickListener(this);
        findViewById(R.id.mainBtnReserved).setOnClickListener(this);
        findViewById(R.id.mainBtnSoldActivity).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.mainBtnAddProduct){
            Intent intent = new Intent(MainActivity.this, AddProduct.class);
            startActivity(intent);
        }
        if(id == R.id.mainBtnReserved){
            Intent intent = new Intent(MainActivity.this, ReservedProductList.class);
            startActivity(intent);
        }
        if(id == R.id.mainBtnSoldActivity){
            Intent intent = new Intent(MainActivity.this, SoldProductActivity.class);
            startActivity(intent);
        }
    }

    public static void readProductsFromStorage(ReadFromStorage read){

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reservedProducts.clear();
                todaySell.clear();
                totalDue = 0.0;
                totalSell = 0.0;
                DataSnapshot sn = snapshot.child(reserves);
                if(sn.exists()){
                    for(DataSnapshot pCode : sn.getChildren()){

                        for(DataSnapshot pName : pCode.getChildren()){
                            Product p = pName.getValue(Product.class);
                            reservedProducts.add(p);
                        }

                    }

                }
                sn = snapshot.child("justTodaySell").child(getTodayDate());
                if(sn.exists()){
                    for(DataSnapshot cName : sn.getChildren()){
                        for(DataSnapshot pCode : cName.getChildren()){
                            for(DataSnapshot pName : pCode.getChildren()){
                                SoldProduct p = pName.getValue(SoldProduct.class);

                                todaySell.add(p);

                                totalSell += (Double.parseDouble(p.getSellAmount()) *
                                        Double.parseDouble(p.getSellingPricePerUnit()));

                                totalDue += Double.parseDouble(p.getDue());
                            }
                        }
                    }
                }
                read.onCallback();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private class WantPermission extends Dialog implements View.OnClickListener{

        private final int decide;
        public WantPermission(@NonNull Context context, int decide) {
            super(context);
            this.decide = decide;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.take_permission);
            initialize();
        }

        private void initialize(){
            findViewById(R.id.permission_btnYes).setOnClickListener(this);
            findViewById(R.id.permission_btnNo).setOnClickListener(this);
            TextView tv = findViewById(R.id.layout_txtText);
            String s = "";
            if(decide == 2) s = "Do you want to Log out?";
            else if(decide == 1) s = "Do you want to Change/reset password?";
            tv.setText(s);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.permission_btnYes){

                if(decide == 1){
                    auth.sendPasswordResetEmail("azharyes2@gmail.com")
                            .addOnCompleteListener(task -> {
                                if(!task.isSuccessful()){

                                    if(task.getException() != null){
                                        Toast.makeText(MainActivity.this, "Verification Failed",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                                else{
                                    Intent intent = new Intent(MainActivity.this,
                                            ActivityChangePassword.class);
                                    startActivity(intent);
                                }

                            });
                }
                else if(decide == 2){
                    auth.signOut();
                    Intent intent = new Intent(MainActivity.this, ActivityLogIn.class);
                    startActivity(intent);
                }

                dismiss();
            }
            else if(id == R.id.permission_btnNo){
                dismiss();
            }
        }
    }

    public static DatabaseReference getReserves() {
        return reserveRef;
    }

    public static DatabaseReference getSold() {
        return soldRef;
    }

    public static ArrayList<Product> getReservedProducts() {
        return reservedProducts;
    }


    public static DatabaseReference getRootPath() {
        return rootRef;
    }

    private static String getTodayDate(){
        String date = Calendar.getInstance().getTime().toString(),
                day = date.substring(8, 10),
                month = getMonth(date.substring(4, 7)),
                year = date.substring(date.length() - 4);

        return (day + "_" + month + "_" + year);
    }

    private static String getMonth(String month){
        switch (month) {
            case "Jan":
                return "01";
            case "Feb":
                return "02";
            case "Mar":
                return "03";
            case "Apr":
                return "04";
            case "May":
                return "05";
            case "Jun":
                return "06";
            case "Jul":
                return "07";
            case "Aug":
                return "08";
            case "Sep":
                return "09";
            case "Oct":
                return "10";
            case "Nov":
                return "11";
            default:
                return "12";
        }

    }

    public static File getSelectedShop(){
        return selectedShop;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}