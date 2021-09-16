package com.amine.bmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class CategorizedDetails extends AppCompatActivity implements View.OnClickListener {

    private String personName;
    private ArrayList<SoldProduct> soldProducts;
    private LinearLayout catDet_detailsLayout1, catDet_detailsLayout2;
    private double totalDue = 0.0, payableAmount = 0.0, totalPaid = 0.0, basicPayable = 0.0;
    private ArrayList<Integer> index;
    private final DecimalFormat df =  new DecimalFormat("0.#");
    private Button catDet_btnAcceptPayment;
    private CheckBox chek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorized_details);
        initialize();
        setDetails();
    }

    private void initialize(){
        index = new ArrayList<>();
        soldProducts = new ArrayList<>();
        personName = getIntent().getStringExtra("PERSON_NAME");
        soldProducts = SoldProductActivity.getSoldProducts();
        catDet_detailsLayout1 = findViewById(R.id.catDet_detailsLayout1);
        catDet_detailsLayout2 = findViewById(R.id.catDet_detailsLayout2);
        catDet_btnAcceptPayment = findViewById(R.id.catDet_btnAcceptPayment);
        catDet_btnAcceptPayment.setOnClickListener(this);
        chek = findViewById(R.id.categorized_checkComplete);
        chek.setOnClickListener(this);
        findViewById(R.id.catDet_imgCalc).setOnClickListener(this);


    }


    private void setDetails(){
        catDet_detailsLayout1.removeAllViews();
        catDet_detailsLayout2.removeAllViews();
        payableAmount = 0.0;
        totalPaid = 0.0;
        totalDue = 0.0;
        basicPayable = 0.0;
        for(int i = 0; i < soldProducts.size(); i++){
            if(soldProducts.get(i).getBuyerName().equals(personName)){

                index.add(i);

                LinearLayout ll1 = new LinearLayout(CategorizedDetails.this),
                        ll2 = new LinearLayout(CategorizedDetails.this),
                        fake1 = new LinearLayout(CategorizedDetails.this),
                        fake2 = new LinearLayout(CategorizedDetails.this);

                ll1.setOrientation(LinearLayout.VERTICAL);
                ll2.setOrientation(LinearLayout.VERTICAL);

                TextView txtPName = new TextView(CategorizedDetails.this),
                        txtCode = new TextView(CategorizedDetails.this),
                        txtDate = new TextView(CategorizedDetails.this),
                        txtSellAmount = new TextView(CategorizedDetails.this),
                        txtSPPUnit = new TextView(CategorizedDetails.this),
                        txtPayable = new TextView(CategorizedDetails.this),
                        txtPaid = new TextView(CategorizedDetails.this),
                        txtDue = new TextView(CategorizedDetails.this);

                txtPName.setGravity(Gravity.CENTER);
                txtCode.setGravity(Gravity.CENTER);
                txtDate.setGravity(Gravity.CENTER);
                txtDate.setGravity(Gravity.CENTER);
                txtSellAmount.setGravity(Gravity.CENTER);
                txtSPPUnit.setGravity(Gravity.CENTER);
                txtPayable.setGravity(Gravity.CENTER);
                txtPaid.setGravity(Gravity.CENTER);
                txtDue.setGravity(Gravity.CENTER);


                catDet_detailsLayout1.addView(ll1);
                catDet_detailsLayout1.addView(fake1);
                catDet_detailsLayout2.addView(ll2);
                catDet_detailsLayout2.addView(fake2);

                ll1.addView(txtPName); ll2.addView(txtCode);
                ll1.addView(txtDate); ll2.addView(txtSellAmount);
                ll1.addView(txtSPPUnit); ll2.addView(txtPayable);
                ll1.addView(txtPaid); ll2.addView(txtDue);


                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT),
                        paramsF = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);


                ll1.setLayoutParams(params); ll2.setLayoutParams(params);
                txtPName.setLayoutParams(params); txtCode.setLayoutParams(params);
                txtDate.setLayoutParams(params); txtSellAmount.setLayoutParams(params);
                txtSPPUnit.setLayoutParams(params); txtPayable.setLayoutParams(params);
                txtPaid.setLayoutParams(params); txtDue.setLayoutParams(params);

                fake1.setLayoutParams(paramsF); fake2.setLayoutParams(paramsF);
                fake1.setBackgroundColor(Color.WHITE); fake2.setBackgroundColor(Color.WHITE);

                double payable = (Double.parseDouble(soldProducts.get(i).getSellAmount()) *
                        Double.parseDouble(soldProducts.get(i).getSellingPricePerUnit())),
                        basic = (Double.parseDouble(soldProducts.get(i).getPurchasePricePerUnit())*
                                Double.parseDouble(soldProducts.get(i).getSellAmount()));

                String s = "পণ্যের নামঃ " + soldProducts.get(i).getProductName();
                txtPName.setText(s);
                s = "কোডঃ " + soldProducts.get(i).getProductCode();
                txtCode.setText(s);
                s = "বিক্রয়ের তারিখঃ " + soldProducts.get(i).getSellingDate();
                txtDate.setText(s);
                s = "বিক্রয়ের পরিমানঃ " + df.format(Double.parseDouble(soldProducts.get(i).getSellAmount()));
                txtSellAmount.setText(s);
                s = "ইউনিট প্রতি বিক্রয় মূল্যঃ " +
                        df.format(Double.parseDouble(soldProducts.get(i).getSellingPricePerUnit()));
                txtSPPUnit.setText(s);
                s = "পরিশোধযোগ্য টাকাঃ " + df.format(payable);
                txtPayable.setText(s);
                s = "পরিশোধঃ " + df.format(Double.parseDouble(soldProducts.get(i).getPaid()));
                txtPaid.setText(s);
                s = "বাকীঃ " + df.format(Double.parseDouble(soldProducts.get(i).getDue()));
                txtDue.setText(s);
                payableAmount += payable;
                basicPayable += basic;
                totalPaid += Double.parseDouble(soldProducts.get(i).getPaid());
                totalDue += Double.parseDouble(soldProducts.get(i).getDue());


            }
        }

        TextView txtTotalPayable = findViewById(R.id.catDet_totalPayable),
                txtTotalPaid = findViewById(R.id.catDet_totalPaid),
                txtTotalDue = findViewById(R.id.catDet_totalDue),
                txtIntro = findViewById(R.id.catDet_txtIntro);

        String s = "মোট পরিশোধযোগ্যঃ " + df.format(payableAmount);
        txtTotalPayable.setText(s);
        s = "মোট পরিশোধঃ " + df.format(totalPaid);
        txtTotalPaid.setText(s);
        s = "মোট বাকীঃ " + df.format(totalDue);
        txtTotalDue.setText(s);
        s = "Details of: " + personName;
        txtIntro.setText(s);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.catDet_btnAcceptPayment){
            EditText ed = findViewById(R.id.catDet_edtPayment);
            String pay = ed.getText().toString().trim(),
                    isComplete = catDet_btnAcceptPayment.getText().toString();

            if(pay.isEmpty()){
                ed.setError("Enter payment");
                ed.requestFocus();
                return;
            }
            double payment = Double.parseDouble(pay);
            if(payment > totalDue){
                ed.setError("Payment can not be bigger than payable amount");
                ed.requestFocus();
                return;
            }

            if(isComplete.equals("Complete Payment") || totalDue - payment == 0){
                findViewById(R.id.categorized_progress).setVisibility(View.VISIBLE);
                totalPaid += payment;
                writeProfitOrLoss(totalPaid, new CartActivity.nCallback() {
                    @Override
                    public void onCallback() {
                        findViewById(R.id.categorized_progress).setVisibility(View.GONE);
                        Intent intent =
                                new Intent(CategorizedDetails.this, SoldProductActivity.class);
                        startActivity(intent);
                    }
                });
            }
            else{
                balanceSoldProduct(payment);
            }

        }
        else if(id == R.id.categorized_checkComplete){
            String s;
            if(chek.isChecked()){
                s = "Complete Payment";
            }
            else{
                s = "Accept Payment";
            }
            catDet_btnAcceptPayment.setText(s);
        }
        else if(id == R.id.catDet_imgCalc){
            CalculatorInterface calc = new CalculatorInterface(this, R.id.catDet_edtPayment,
                    (s, ID) -> {
                        EditText ed = findViewById(ID);
                        ed.setText(s);
                    });

            calc.show();
            WindowManager.LayoutParams p = getDisplaySize(calc);
            calc.getWindow().setAttributes(p);
        }
    }

    private void deleteNamesFromDatabase(){
        for(int i = 0; i < index.size(); i++){
            int ind = index.get(i);
            String date = soldProducts.get(ind).getSellingDate(),
                    name = soldProducts.get(ind).getBuyerName();

            DatabaseReference r = MainActivity.getSold().child(date).child(name);
            r.removeValue();

        }
    }

    private void writeProfitOrLoss(double pd, CartActivity.nCallback callback){
        DatabaseReference rTotalSell = MainActivity.getRootPath().child("totalSell"),
                rExpectedProfit = MainActivity.getRootPath().child("expectedProfit"),
                rNetLoss = MainActivity.getRootPath().child("netLoss"),
                rMinorLoss = MainActivity.getRootPath().child("minorLoss"),
                rNProfit = MainActivity.getRootPath().child("netProfit");

        MainActivity.getRootPath().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double dExpProfit = payableAmount - basicPayable,
                        dSell = pd,
                        dNProfit = pd - basicPayable,
                        dNLoss = -dNProfit, dMLoss = payableAmount - pd;


                if(snapshot.child("totalSell").exists())
                    dSell += snapshot.child("totalSell").getValue(Double.class);
                rTotalSell.setValue(dSell);


                if(snapshot.child("expectedProfit").exists()){

                    if(dExpProfit > 0){
                        dExpProfit += snapshot.child("expectedProfit").getValue(Double.class);
                        rExpectedProfit.setValue(dExpProfit);
                    }
                }
                else{
                    if(dExpProfit > 0) rExpectedProfit.setValue(dExpProfit);

                }


                if(snapshot.child("netLoss").exists()){

                    if(dNLoss > 0){
                        dNLoss += snapshot.child("netLoss").getValue(Double.class);
                        rNetLoss.setValue(dNLoss);
                    }
                }
                else{
                    if(dNLoss > 0) rNetLoss.setValue(dNLoss);
                }

                if(snapshot.child("minorLoss").exists()){
                    if(dMLoss > 0){
                        dMLoss += snapshot.child("minorLoss").getValue(Double.class);
                        rMinorLoss.setValue(dMLoss);
                    }
                }
                else{
                    if(dMLoss > 0) rMinorLoss.setValue(dMLoss);
                }

                if(snapshot.child("netProfit").exists()){
                    if(dNProfit > 0){
                        dNProfit += snapshot.child("netProfit").getValue(Double.class);
                        rNProfit.setValue(dNProfit);
                    }
                }
                else{
                    if(dNProfit > 0) rNProfit.setValue(dNProfit);
                }

                deleteNamesFromDatabase();

                callback.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void balanceSoldProduct(double payment){
        for(int i = 0; i < index.size(); i++){
            int ind = index.get(i);
            SoldProduct p = soldProducts.get(ind);

            double paid = Double.parseDouble(p.getPaid()), due = Double.parseDouble(p.getDue());

            if(due < payment){
                paid += due;
                payment -= due;
                due = 0;
            }
            else{
                paid += payment;
                due -= payment;
                payment = 0;
            }
            p.setPaid(paid + "");
            p.setDue(due + "");

            overrideSoldProduct(p);
        }
        setDetails();
    }

    private void overrideSoldProduct(SoldProduct p){

        DatabaseReference r = MainActivity.getSold().child(p.getSellingDate()).child(p.getBuyerName())
                .child(p.getProductCode()).child(p.getProductName());

        r.setValue(p);
    }

    public interface CCallback{
        void onCallback(String s, int ID);
    }
    public class CalculatorInterface extends Dialog implements View.OnClickListener{
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
                    Toast.makeText(CategorizedDetails.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(CategorizedDetails.this, "Can't include negative numbers\n" +
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
                Toast.makeText(CategorizedDetails.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String s = txtDisplayResult.getText().toString() + num;
            txtDisplayResult.setText(s);
        }
        private void operation(String op){

            if(equalClicked){
                Toast.makeText(CategorizedDetails.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
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
    public int getColor(String hex){
        int color = Integer.parseInt(hex, 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;

        return Color.rgb(r, g, b);
    }
}