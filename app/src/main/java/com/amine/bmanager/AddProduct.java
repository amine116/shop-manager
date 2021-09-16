package com.amine.bmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AddProduct extends AppCompatActivity implements View.OnClickListener {

    private final DecimalFormat df =  new DecimalFormat("0.#");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        initialize();
    }

    private void initialize(){
        findViewById(R.id.addProductBtnAdd).setOnClickListener(this);
        findViewById(R.id.add_imgCalcAmount).setOnClickListener(this);
        findViewById(R.id.add_imgCalcPPrice).setOnClickListener(this);
        findViewById(R.id.add_imgCalcSPrice).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.addProductBtnAdd){
            EditText editText = findViewById(R.id.addProductEdtProductName),
                    editText1 = findViewById(R.id.addProductEdtProductCode),
                    edtAmount = findViewById(R.id.addProductEdtProductAmount),
                    edtPurch = findViewById(R.id.addProductEdtPurchasePrice),
                    edtSell = findViewById(R.id.addProductEdtSellingPrice),
                    add_edtCompanyName = findViewById(R.id.add_edtCompanyName);


            String productName = editText.getText().toString().trim(),
                    productCode = editText1.getText().toString().trim(),
                    reservedAmount = edtAmount.getText().toString().trim(),
                    purchasePrice = edtPurch.getText().toString().trim(),
                    sellingPricePerUnit = edtSell.getText().toString().trim(),
                    companyName = add_edtCompanyName.getText().toString().trim();

            if(productName.isEmpty()){
                editText.setError("Enter product name");
                editText.requestFocus();
                return;
            }

            if(productCode.isEmpty()){
                editText1.setError("Enter product code");
                editText1.requestFocus();
                return;
            }

            if(companyName.isEmpty()){
                add_edtCompanyName.setError("Enter Company Name");
                add_edtCompanyName.requestFocus();
                return;
            }

            if(reservedAmount.isEmpty()){
                edtAmount.setError("Enter Amount");
                edtAmount.requestFocus();
                return;
            }

            if(purchasePrice.isEmpty()){
                edtPurch.setError("Enter purchase price");
                edtPurch.requestFocus();
                return;
            }

            if(sellingPricePerUnit.isEmpty()){
                edtSell.setError("Enter selling price");
                edtSell.requestFocus();
                return;
            }
            double amount = Double.parseDouble(reservedAmount);
            if(amount < 1.0){
                Toast.makeText(AddProduct.this, "Amount can't be less than 1", Toast.LENGTH_LONG).show();
                return;
            }

            String purchasePricePerUnit = (Double.parseDouble(purchasePrice)/amount) + "";

            Product product = new Product(productCode, productName, reservedAmount,
                    getTodayDate(), purchasePricePerUnit, sellingPricePerUnit, companyName);

            findViewById(R.id.add_scrollLayout).setVisibility(View.GONE);
            findViewById(R.id.add_progress).setVisibility(View.VISIBLE);

            writeProduct(product, () -> MainActivity.readProductsFromStorage(() -> {
                findViewById(R.id.add_scrollLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.add_progress).setVisibility(View.GONE);
            }));

        }
        if(id == R.id.add_imgCalcAmount){
            CalculatorInterface calc = new CalculatorInterface(this, R.id.addProductEdtProductAmount,
                    (s, ID) -> {
                        EditText ed = findViewById(ID);
                        ed.setText(s);
                    });
            calc.show();
            WindowManager.LayoutParams p = getDisplaySize(calc);
            calc.getWindow().setAttributes(p);
        }
        if(id == R.id.add_imgCalcPPrice){
            CalculatorInterface calc = new CalculatorInterface(this, R.id.addProductEdtPurchasePrice,
                    (s, ID) -> {
                        EditText ed = findViewById(ID);
                        ed.setText(s);
                    });
            calc.show();
            WindowManager.LayoutParams p = getDisplaySize(calc);
            calc.getWindow().setAttributes(p);
        }
        if(id == R.id.add_imgCalcSPrice){
            CalculatorInterface calc = new CalculatorInterface(this, R.id.addProductEdtSellingPrice,
                    (s, ID) -> {
                        EditText ed = findViewById(ID);
                        ed.setText(s);
                    });

            calc.show();
            WindowManager.LayoutParams p = getDisplaySize(calc);
            calc.getWindow().setAttributes(p);
        }
    }

    private WindowManager.LayoutParams getDisplaySize(CalculatorInterface calc){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(calc.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.9f);
        int dialogWindowHeight = (int) (displayHeight * 0.8f);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;

        return layoutParams;
    }


    public static void writeProduct(Product newP, CartActivity.nCallback nCallback){

        DatabaseReference r = MainActivity.getReserves().child(newP.getProductCode())
                .child(newP.getProductName());

        r.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Product prevP = snapshot.getValue(Product.class);
                    if(prevP != null){
                        String newReserve = (Double.parseDouble(prevP.getReservedAmount()) +
                                Double.parseDouble(newP.getReservedAmount())) + "";
                        newP.setReservedAmount(newReserve);
                        newP.setPurchaseDate(prevP.getPurchaseDate());
                    }

                }

                r.setValue(newP);

                nCallback.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void saveCartProductToStorage(CartProduct p){

        DatabaseReference r = MainActivity.getRootPath().child("cart").child(p.getProductCode())
                .child(p.getProductName());
        r.setValue(p);

    }

    private String getTodayDate(){
        String date = Calendar.getInstance().getTime().toString(),
                day = date.substring(8, 10),
                month = getMonth(date.substring(4, 7)),
                year = date.substring(date.length() - 4);

        return (day + "_" + month + "_" + year);
    }

    private String getMonth(String month){
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

    private interface CCallback{
        void onCallback(String s, int ID);
    }
    private class CalculatorInterface extends Dialog implements View.OnClickListener{
        private TextView txtDisplayInputs, txtDisplayResult;
        private final int ID;
        private double result = 0;
        private String  prevOp = "", lastOp = "";
        private boolean equalClicked = false;
        private final CCallback callback;

        public CalculatorInterface(@NonNull Context context, int ID, CCallback callback) {
            super(context);
            this.callback = callback;
            this.ID = ID;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.calculator_interface);
            initialize();
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == R.id.btn0){
                if(equalClicked){
                    Toast.makeText(AddProduct.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                    return;
                }
                String s = txtDisplayResult.getText().toString();
                if(!s.equals("")) s = s + "0";
                txtDisplayResult.setText(s);
            }
            if(v.getId() == R.id.btn1){
                addDigit("1");
            }
            if(v.getId() == R.id.btn2){
                addDigit("2");
            }
            if(v.getId() == R.id.btn3){
                addDigit("3");
            }
            if(v.getId() == R.id.btn4){
                addDigit("4");
            }
            if(v.getId() == R.id.btn5){
                addDigit("5");
            }
            if(v.getId() == R.id.btn6){
                addDigit("6");
            }
            if(v.getId() == R.id.btn7){
                addDigit("7");
            }
            if(v.getId() == R.id.btn8){
                addDigit("8");
            }
            if(v.getId() == R.id.btn9){
                addDigit("9");
            }
            if(v.getId() == R.id.btnAc){

                result = 0;
                txtDisplayResult.setText("");
                txtDisplayInputs.setText("");
                equalClicked = false;
                lastOp = "";
                prevOp = "";
            }
            if(v.getId() == R.id.btnX){

                String s = txtDisplayResult.getText().toString();
                if(s.length() > 1) s = s.substring(0, s.length() - 1);
                else s = "";
                txtDisplayResult.setText(s);
            }
            if(v.getId() == R.id.btnDiv){

                operation("÷");
            }
            if(v.getId() == R.id.btnMul){

                operation("X");
            }
            if(v.getId() == R.id.btnMin){
                operation("-");
            }
            if(v.getId() == R.id.btnPlus){

                operation("+");
            }
            if(v.getId() == R.id.btnEqual){

                String prev = txtDisplayInputs.getText().toString(),
                        s = txtDisplayResult.getText().toString();
                if(s.equals("")) return;

                if(equalClicked) return;
                equalClicked = true;

                if(!prev.equals("")){
                    prev = "( " + prev + " " + s + " ) )";
                }
                else{
                    prev = "( " + s + " )";
                }
                txtDisplayInputs.setText(prev);


                switch (lastOp) {
                    case "÷":
                        result = result / Double.parseDouble(s);
                        break;
                    case "X":
                        result = result * Double.parseDouble(s);
                        break;
                    case "+":
                        result = result + Double.parseDouble(s);
                        break;
                    case "-":
                        result = result - Double.parseDouble(s);
                        break;
                    case "":
                        result = Double.parseDouble(s);
                        break;
                }

                String val = result + "";
                txtDisplayResult.setText(val);
                Log.i("test", result + "");
            }
            if(v.getId() == R.id.btnPoint){
                String s = txtDisplayResult.getText().toString();
                if(!pointExistInNumber(s)) s = s + ".";
                txtDisplayResult.setText(s);
            }

            if(v.getId() == R.id.btnCalcInterfaceOk){

                String s = txtDisplayResult.getText().toString();
                if(s.equals("")) s = "0";
                int res = (int)Double.parseDouble(s);
                if(res < 0){
                    Toast.makeText(AddProduct.this, "Can't include negative numbers\n" +
                                    "Press cancel",
                            Toast.LENGTH_LONG).show();
                }else {
                    callback.onCallback(res + "", ID);
                    dismiss();
                }
            }
            if(v.getId() == R.id.btnCalcInterfaceCancel){
                dismiss();
            }

        }

        private void addDigit(String num){
            if(equalClicked){
                Toast.makeText(AddProduct.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String s = txtDisplayResult.getText().toString() + num;
            txtDisplayResult.setText(s);
        }
        private void operation(String op){

            if(equalClicked){
                Toast.makeText(AddProduct.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String curNum = txtDisplayResult.getText().toString();

            if(curNum.equals("") || curNum.equals("0")) return;
            lastOp = op;
            Log.i("test2", result + "");
            operate(prevOp, curNum);
            Log.i("test2", result + "");
        }
        private void operate(String op, String num){

            String prev = txtDisplayInputs.getText().toString();
            if(!prev.equals("")){
                prev = "( " + prev + " " + num + " ) " + lastOp;
            }
            else{
                prev = "( " + num + " " + lastOp + " ";
            }
            txtDisplayInputs.setText(prev);
            txtDisplayResult.setText("");

            switch (op) {
                case "÷":
                    result = result / Double.parseDouble(num);
                    break;
                case "X":
                    result = result * Double.parseDouble(num);
                    break;
                case "+":
                    result = result + Double.parseDouble(num);
                    break;
                case "-":
                    result = result - Double.parseDouble(num);
                    break;
                case "":
                    result = Double.parseDouble(num);
                    break;
            }
            prevOp = lastOp;

        }
        private boolean pointExistInNumber(String number){
            for(int i = 0; i < number.length(); i++){
                if(number.charAt(i) == '.'){
                    return true;
                }
            }
            return false;
        }
        private void initialize(){

            txtDisplayInputs = findViewById(R.id.txtDisplayInputs);
            txtDisplayResult = findViewById(R.id.txtDisplayResult);

            findViewById(R.id.btn0).setOnClickListener(this);
            findViewById(R.id.btn0).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btn1).setOnClickListener(this);
            findViewById(R.id.btn1).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btn2).setOnClickListener(this);
            findViewById(R.id.btn2).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btn3).setOnClickListener(this);
            findViewById(R.id.btn3).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btn4).setOnClickListener(this);
            findViewById(R.id.btn4).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btn5).setOnClickListener(this);
            findViewById(R.id.btn5).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btn6).setOnClickListener(this);
            findViewById(R.id.btn6).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btn7).setOnClickListener(this);
            findViewById(R.id.btn7).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btn8).setOnClickListener(this);
            findViewById(R.id.btn8).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btn9).setOnClickListener(this);
            findViewById(R.id.btn9).setBackgroundColor(getColor("323438"));
            findViewById(R.id.btnAc).setOnClickListener(this);
            findViewById(R.id.btnAc).setBackgroundColor(getColor("9fb4e3"));
            findViewById(R.id.btnPlus).setOnClickListener(this);
            findViewById(R.id.btnPlus).setBackgroundColor(getColor("9fb4e3"));
            findViewById(R.id.btnMin).setOnClickListener(this);
            findViewById(R.id.btnMin).setBackgroundColor(getColor("9fb4e3"));
            findViewById(R.id.btnMul).setOnClickListener(this);
            findViewById(R.id.btnMul).setBackgroundColor(getColor("9fb4e3"));
            findViewById(R.id.btnDiv).setOnClickListener(this);
            findViewById(R.id.btnDiv).setBackgroundColor(getColor("9fb4e3"));
            findViewById(R.id.btnEqual).setOnClickListener(this);
            findViewById(R.id.btnEqual).setBackgroundColor(getColor("9fb4e3"));
            findViewById(R.id.btnPoint).setOnClickListener(this);
            findViewById(R.id.btnPoint).setBackgroundColor(getColor("9fb4e3"));
            findViewById(R.id.btnX).setOnClickListener(this);
            findViewById(R.id.btnX).setBackgroundColor(getColor("9fb4e3"));
            findViewById(R.id.btnCalcInterfaceOk).setOnClickListener(this);
            findViewById(R.id.btnCalcInterfaceCancel).setOnClickListener(this);

        }

    }
    private int getColor(String hex){
        int color = Integer.parseInt(hex, 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;

        return Color.rgb(r, g, b);
    }
}