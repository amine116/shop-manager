package com.amine.bmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.contentcapture.DataRemovalRequest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Scanner;

public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout productListLayout, sellLayout;
    private ArrayList<Product> reservedProducts;
    private ArrayList<CartProduct> inCart;
    private ArrayList<SoldProduct> soldProducts;
    private TextView txtPayableAmount;
    private double payableAmount = 0, basicPayable = 0;
    private final DecimalFormat df =  new DecimalFormat("0.#");
    private CheckBox cart_checkCompleteSell;
    private Button cart_btnCompleteSell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        initialize();

    }

    private void initialize(){

        findViewById(R.id.cart_progress).setVisibility(View.VISIBLE);
        soldProducts = new ArrayList<>();
        productListLayout = findViewById(R.id.productListLayout);
        sellLayout = findViewById(R.id.sellLayout);
        reservedProducts = MainActivity.getReservedProducts();
        inCart = ReservedProductList.getInCart();
        txtPayableAmount = findViewById(R.id.cart_txtPayableAmount);
        cart_checkCompleteSell = findViewById(R.id.cart_checkComplete);
        cart_btnCompleteSell = findViewById(R.id.cart_btnCompleteSell);

        cart_checkCompleteSell.setOnClickListener(this);
        findViewById(R.id.cart_btnCompleteSell).setOnClickListener(this);
        findViewById(R.id.cart_btnCancel).setOnClickListener(this);

        readSellHistory(() -> {
            findViewById(R.id.cart_progress).setVisibility(View.GONE);
            findViewById(R.id.cart_buttonLayout).setVisibility(View.VISIBLE);
            setProductList();
        });
    }

    private void setProductList(){
        productListLayout.removeAllViews();
        sellLayout.removeAllViews();
        Button[] btnSell = new Button[inCart.size() + 1];
        double payable = 0.0, basic = 0.0;
        for(int i = 0; i < inCart.size(); i++){
            LinearLayout ll = new LinearLayout(CartActivity.this),
                    sellBtnLayout = new LinearLayout(CartActivity.this),
                    fake = new LinearLayout(CartActivity.this),
                    fake1 = new LinearLayout(CartActivity.this);

            ll.setOrientation(LinearLayout.VERTICAL);
            sellBtnLayout.setOrientation(LinearLayout.VERTICAL);
            productListLayout.addView(ll);
            sellLayout.addView(sellBtnLayout);



            TextView name = new TextView(CartActivity.this),
                    code = new TextView(CartActivity.this),
                    amount = new TextView(CartActivity.this),
                    purPrice = new TextView(CartActivity.this),
                    sellPrice = new TextView(CartActivity.this),
                    sellAmount = new TextView(CartActivity.this);
            btnSell[i] = new Button(CartActivity.this);

            ll.addView(name);
            ll.addView(code);
            ll.addView(amount);
            ll.addView(sellAmount);
            ll.addView(purPrice);
            ll.addView(sellPrice);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            ll.setLayoutParams(params);

            name.setLayoutParams(params);
            name.setGravity(Gravity.CENTER);
            name.setTextColor(Color.CYAN);
            name.setTextSize(20);

            code.setLayoutParams(params);
            code.setGravity(Gravity.CENTER);

            amount.setLayoutParams(params);
            amount.setGravity(Gravity.CENTER);

            sellAmount.setLayoutParams(params);
            sellAmount.setGravity(Gravity.CENTER);

            purPrice.setLayoutParams(params);
            purPrice.setGravity(Gravity.CENTER);

            sellPrice.setLayoutParams(params);
            sellPrice.setGravity(Gravity.CENTER);

            payable += (Double.parseDouble(inCart.get(i).getSellAmount()) *
                    Double.parseDouble(inCart.get(i).getSellingPricePerUnit()));
            basic += (Double.parseDouble(inCart.get(i).getPurchasePricePerUnit()) *
                    Double.parseDouble(inCart.get(i).getSellAmount()));


            String s;

            s = "পণ্যের নাম: " + inCart.get(i).getProductName();

            name.setText(s);
            s = "পণ্যের কোড: " + inCart.get(i).getProductCode();
            code.setText(s);

            s = "Reserved: " + df.format(Double.parseDouble(inCart.get(i).getReservedAmount())) + "  টি";
            amount.setText(s);

            s = "বিক্রয়ের পরিমান: " + df.format(Double.parseDouble(inCart.get(i).getSellAmount())) + "  টি";

            sellAmount.setText(s);

            s = "ইউনিট প্রতি ক্রয়মুল্য: " + df.format(Double.parseDouble(inCart.get(i).getPurchasePricePerUnit()));
            purPrice.setText(s);
            s = "ইউনিট প্রতি বিক্রয়মুল্য: " + df.format(Double.parseDouble(inCart.get(i).getSellingPricePerUnit()));
            sellPrice.setText(s);


            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    283);
            sellBtnLayout.setLayoutParams(params1);
            sellBtnLayout.setGravity(Gravity.CENTER);

            sellBtnLayout.addView(btnSell[i]);

            btnSell[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));


            s = "Edit";

            btnSell[i].setText(s);


            sellLayout.addView(fake1);
            fake1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 7));
            productListLayout.addView(fake);
            fake.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 7));

            fake.setBackgroundColor(Color.GREEN);
            fake1.setBackgroundColor(Color.GREEN);

            int finalI = i;
            btnSell[i].setOnClickListener(v -> {

                CartProduct product = inCart.get(finalI);

                class SellDialog extends Dialog implements View.OnClickListener{
                    TextView name;
                    EditText selPric, sellAmount;
                    public SellDialog(@NonNull Context context) {
                        super(context);
                    }

                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        setContentView(R.layout.layout);
                        setCancelable(false);
                        initialize();
                    }

                    private void initialize(){
                        name = findViewById(R.id.layout_txtName);
                        selPric = findViewById(R.id.layout_edtSellingPrice);
                        sellAmount = findViewById(R.id.layout_edtSellingAmount);
                        sellAmount.setText(product.getSellAmount());
                        Button button = findViewById(R.id.layout_btnSell);
                        String ss = "Save";
                        button.setText(ss);

                        selPric.setText(product.getSellingPricePerUnit());

                        ss = "Product name: " + inCart.get(finalI).getProductName();
                        name.setText(ss);
                        name.setTextSize(20);
                        name.setTextColor(Color.CYAN);

                        findViewById(R.id.layout_btnSell).setOnClickListener(this);
                        findViewById(R.id.layout_btnCancel).setOnClickListener(this);
                    }

                    @Override
                    public void onClick(View v) {
                        int id = v.getId();
                        if(id == R.id.layout_btnSell){
                            String strSelPrice = selPric.getText().toString().trim(),
                                    selAmount = sellAmount.getText().toString().trim();
                            if(strSelPrice.isEmpty()){
                                selPric.setError("Enter selling price");
                                selPric.requestFocus();
                                return;
                            }
                            if(selAmount.isEmpty()){
                                sellAmount.setError("Enter sell amount");
                                sellAmount.requestFocus();
                                return;
                            }
                            double reserv = Double.parseDouble(product.getReservedAmount()),
                                    slamnt = Double.parseDouble(selAmount);
                            if(reserv < slamnt){
                                sellAmount.setError("Sell amount is bigger than your reserve");
                                sellAmount.requestFocus();
                                return;
                            }


                            // TODO
                            product.setSellingPricePerUnit(strSelPrice);
                            product.setSellAmount(selAmount);
                            inCart.set(finalI, product);
                            AddProduct.saveCartProductToStorage(product);
                            setProductList();
                            dismiss();
                        }
                        if(id == R.id.layout_btnCancel){
                            dismiss();
                        }
                    }
                }

                SellDialog sellDialog = new SellDialog(CartActivity.this);
                sellDialog.show();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(sellDialog.getWindow().getAttributes());
                int dialogWindowWidth = (int) (displayWidth * 0.8f);
                int dialogWindowHeight = (int) (displayHeight * 0.5f);
                layoutParams.width = dialogWindowWidth;
                layoutParams.height = dialogWindowHeight;
                sellDialog.getWindow().setAttributes(layoutParams);
            });


        }

        payableAmount = payable;
        basicPayable = basic;
        String s = df.format(payable) + "  টাকা";
        txtPayableAmount.setText(s);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.cart_btnCompleteSell){
            EditText edtPayment = findViewById(R.id.cart_edtPayment),
                    edtCustomerName = findViewById(R.id.cart_edtCustomerName),
                    edtPhone = findViewById(R.id.cart_edtCustomerPhone);
            String payment = edtPayment.getText().toString(),
                    customerName = edtCustomerName.getText().toString().trim(),
                    phone = edtPhone.getText().toString().trim(),
                    isComplete = cart_btnCompleteSell.getText().toString();

            if(payment.isEmpty()){
                edtPayment.setError("Enter payment");
                edtPayment.requestFocus();
                return;
            }
            if(customerName.isEmpty()){
                edtCustomerName.setError("Enter customer name");
                edtCustomerName.requestFocus();
                return;
            }
            if(phone.isEmpty()) phone = "N/A";

            double paymentD = Double.parseDouble(payment), due, paid = 0.0;
            if(paymentD > payableAmount){
                edtPayment.setError("Payment can't be more than payable amount!");
                edtPayment.requestFocus();
                return;
            }

            makePDF(inCart, customerName, payableAmount, paymentD, payableAmount - paymentD, phone,
                    isComplete.equals("Complete Sell"));

            if(isComplete.equals("Complete Sell") ||
                    Math.abs(payableAmount - paymentD) == 0){

                for(int i = 0; i < inCart.size(); i++){
                    CartProduct p = inCart.get(i);
                    String date = getTodayDate();
                    double amnt = Double.parseDouble(p.getSellAmount());
                    if(amnt > 0.0){
                        double pPayable = Double.parseDouble(p.getSellAmount()) *
                                Double.parseDouble(p.getSellingPricePerUnit());

                        if(pPayable < paymentD){
                            paid = pPayable;
                            due = 0;
                            paymentD -= pPayable;
                        }
                        else{
                            paid = paymentD;
                            due = pPayable - paymentD;
                            paymentD = 0;
                        }

                        SoldProduct product = new SoldProduct(p.getProductCode(), p.getProductName(),
                                p.getReservedAmount(), p.getPurchaseDate(), p.getPurchasePricePerUnit(),
                                p.getSellingPricePerUnit(), p.getCompanyName(), p.getSellAmount(),
                                pPayable + "", paid + "", due + "",
                                date, customerName, phone);

                        storeSoldProductsInToday(product);

                        balanceReserve(p.getProductCode(), p.getProductName(), p.getSellAmount());
                    }

                }

                findViewById(R.id.cart_progress).setVisibility(View.VISIBLE);
                findViewById(R.id.cart_buttonLayout).setVisibility(View.GONE);
                writeProfitOrLoss(payment, () -> {
                    MainActivity.getRootPath().child("cart").removeValue();
                    findViewById(R.id.cart_progress).setVisibility(View.GONE);
                    findViewById(R.id.cart_buttonLayout).setVisibility(View.VISIBLE);

                    Intent intent = new Intent(CartActivity.this, ReservedProductList.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }
            else{
                for(int i = 0; i < inCart.size(); i++){
                    CartProduct p = inCart.get(i);
                    String date = getTodayDate();
                    double amnt = Double.parseDouble(p.getSellAmount());
                    if(amnt > 0.0){
                        double pPayable = Double.parseDouble(p.getSellAmount()) *
                                Double.parseDouble(p.getSellingPricePerUnit());

                        if(pPayable < paymentD){
                            paid = pPayable;
                            due = 0;
                            paymentD -= pPayable;
                        }
                        else{
                            paid = paymentD;
                            due = pPayable - paymentD;
                            paymentD = 0;
                        }

                        SoldProduct product = new SoldProduct(p.getProductCode(), p.getProductName(),
                                p.getReservedAmount(), p.getPurchaseDate(), p.getPurchasePricePerUnit(),
                                p.getSellingPricePerUnit(), p.getCompanyName(), p.getSellAmount(),
                                pPayable + "", paid + "", due + "",
                                date, customerName, phone);

                        storeSoldProducts(product);
                        storeSoldProductsInToday(product);

                        balanceReserve(p.getProductCode(), p.getProductName(), p.getSellAmount());
                    }

                }

                MainActivity.getRootPath().child("cart").removeValue();
                Intent intent = new Intent(CartActivity.this, ReservedProductList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }



        }
        else if(id == R.id.cart_btnCancel){
            Intent intent = new Intent(CartActivity.this, ReservedProductList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else if(id == R.id.cart_checkComplete){
            String s;
            if(cart_checkCompleteSell.isChecked()){
                s = "Complete Sell";
            }else{
                s = "Sell";
            }
            cart_btnCompleteSell.setText(s);
        }
    }

    private void makePDF(ArrayList<CartProduct> list, String cName, double payable,
                         double paid, double due, String phone, boolean isComplete){

        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(350,
                200 + (list.size()*150), 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();
        int x = 50, y = 25;

        String s = "";

        myPaint.setColor(Color.GREEN);
        s = MainActivity.shopName;
        myPage.getCanvas().drawText(s, x + 50, y, myPaint);
        y+=myPaint.descent()-myPaint.ascent() + 20;


        myPaint.reset();
        myPaint.setColor(Color.RED);
        s = "Sell Document";
        myPage.getCanvas().drawText(s, x + 70, y, myPaint);
        y+=myPaint.descent()-myPaint.ascent() + 50;

        myPaint.reset();
        s = "Mr/Mrs: " + cName;
        myPage.getCanvas().drawText(s, x + 70, y, myPaint);
        y+=myPaint.descent()-myPaint.ascent();

        myPaint.reset();
        s = "Phone: " + phone;
        myPage.getCanvas().drawText(s, x + 70, y, myPaint);
        y+=myPaint.descent()-myPaint.ascent() + 20;


        for(int i = 0; i < list.size(); i++){

            s = (i + 1) + ". Product Name: " + list.get(i).getProductName();
            myPage.getCanvas().drawText(s, x, y, myPaint);
            y+=myPaint.descent()-myPaint.ascent();

            s = "Product Code: " + list.get(i).getProductCode();
            myPage.getCanvas().drawText(s, x + 20, y, myPaint);
            y+=myPaint.descent()-myPaint.ascent();

            s = "Company: " + list.get(i).getCompanyName();
            myPage.getCanvas().drawText(s, x + 20, y, myPaint);
            y+=myPaint.descent()-myPaint.ascent();

            s = "Amount: " + df.format(Double.parseDouble(list.get(i).getSellAmount()));
            myPage.getCanvas().drawText(s, x + 20, y, myPaint);
            y+=myPaint.descent()-myPaint.ascent();

            s = "Price per product: " + df.format(Double.parseDouble(list.get(i).getSellingPricePerUnit()));
            myPage.getCanvas().drawText(s, x + 20, y, myPaint);
            y+=myPaint.descent()-myPaint.ascent() + 20;

        }


        myPaint.setColor(Color.MAGENTA);
        s = "Selling Date: " + getTodayDate();
        myPage.getCanvas().drawText(s, x, y, myPaint);
        y+=myPaint.descent()-myPaint.ascent();


        s = "Total cost: " + df.format(payable);
        myPage.getCanvas().drawText(s, x, y, myPaint);
        y+=myPaint.descent()-myPaint.ascent();

        s = "Total Paid: " + df.format(paid);
        myPage.getCanvas().drawText(s, x, y, myPaint);
        y+=myPaint.descent()-myPaint.ascent();

        myPaint.reset();
        myPaint.setColor(Color.RED);
        if(!isComplete) s = "Due: " + df.format(due);
        else s = "Due: 0";
        myPage.getCanvas().drawText(s, x, y, myPaint);


        myPdfDocument.finishPage(myPage);

        File myFile = new File(getExternalFilesDir(MainActivity.appName), "Sell Document.pdf");

        try {
            myPdfDocument.writeTo(new FileOutputStream(myFile));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        myPdfDocument.close();
    }


    public interface nCallback{
        void onCallback();
    }

    private void storeSoldProductsInToday(SoldProduct p){
        for(int i = 0; i < soldProducts.size(); i++){
            SoldProduct prev = soldProducts.get(i);
            String n = prev.getBuyerName(),
                    c = prev.getProductCode(),
                    pn = prev.getProductName();
            if(p.getBuyerName().equals(n) && p.getProductCode().equals(c)
                    && p.getProductName().equals(pn)) {

                String sa = prev.getSellAmount(),
                        pa = prev.getPayableAmount(),
                        pd = prev.getPaid(),
                        du = prev.getDue();
                double newSelAm = Double.parseDouble(sa) + Double.parseDouble(p.getSellAmount()),
                        newPayable =
                                Double.parseDouble(pa) + Double.parseDouble(p.getPayableAmount()),
                        newPaid = Double.parseDouble(pd) + Double.parseDouble(p.getPaid()),
                        newDu = Double.parseDouble(du) + Double.parseDouble(p.getDue());

                p.setSellAmount(newSelAm + "");
                p.setPayableAmount(newPayable + "");
                p.setPaid(newPaid + "");
                p.setDue(newDu + "");
                break;

            }
        }

        DatabaseReference r = MainActivity.getRootPath().child("justTodaySell")
                .child(p.getSellingDate()).child(p.getBuyerName())
                .child(p.getProductCode()).child(p.getProductName());


        r.setValue(p);
    }

    private void writeProfitOrLoss(String paid, nCallback callback){
        DatabaseReference rTotalSell = MainActivity.getRootPath().child("totalSell"),
                rExpectedProfit = MainActivity.getRootPath().child("expectedProfit"),
                rNetLoss = MainActivity.getRootPath().child("netLoss"),
                rMinorLoss = MainActivity.getRootPath().child("minorLoss"),
                rNProfit = MainActivity.getRootPath().child("netProfit");

        MainActivity.getRootPath().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double dExpProfit = payableAmount - basicPayable, pd = Double.parseDouble(paid),
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

                callback.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface ReadSellHistory{
        void onCallback();
    }

    private void readSellHistory(ReadSellHistory read){

        DatabaseReference r = MainActivity.getSold();

        r.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot date : snapshot.getChildren()){
                        for(DataSnapshot name : date.getChildren()){
                            for(DataSnapshot code : name.getChildren()){
                                for(DataSnapshot product : code.getChildren()){
                                    SoldProduct p = product.getValue(SoldProduct.class);
                                    if(p != null){
                                        soldProducts.add(p);
                                    }
                                }
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

    private void balanceReserve(String code, String name, String sellAmount){
        for(int i = 0; i < reservedProducts.size(); i++){

            String rCode = reservedProducts.get(i).getProductCode(),
                    rName = reservedProducts.get(i).getProductName();
            if(code.equals(rCode) && name.equals(rName)){
                reservedProducts.get(i).setReservedAmount((-Double.parseDouble(sellAmount)) + "");

                findViewById(R.id.cart_progress).setVisibility(View.VISIBLE);
                findViewById(R.id.cart_buttonLayout).setVisibility(View.GONE);

                AddProduct.writeProduct(reservedProducts.get(i), () -> {

                    findViewById(R.id.cart_progress).setVisibility(View.GONE);
                    findViewById(R.id.cart_buttonLayout).setVisibility(View.VISIBLE);

                });
            }
        }
    }

    private void storeSoldProducts(SoldProduct p){

        for(int i = 0; i < soldProducts.size(); i++){
            SoldProduct prev = soldProducts.get(i);
            String n = prev.getBuyerName(),
                    c = prev.getProductCode(),
                    pn = prev.getProductName();
            if(p.getBuyerName().equals(n) && p.getProductCode().equals(c)
                    && p.getProductName().equals(pn)) {

                String sa = prev.getSellAmount(),
                        pa = prev.getPayableAmount(),
                        pd = prev.getPaid(),
                        du = prev.getDue();
                double newSelAm = Double.parseDouble(sa) + Double.parseDouble(p.getSellAmount()),
                        newPayable =
                                Double.parseDouble(pa) + Double.parseDouble(p.getPayableAmount()),
                        newPaid = Double.parseDouble(pd) + Double.parseDouble(p.getPaid()),
                        newDu = Double.parseDouble(du) + Double.parseDouble(p.getDue());

                p.setSellAmount(newSelAm + "");
                p.setPayableAmount(newPayable + "");
                p.setPaid(newPaid + "");
                p.setDue(newDu + "");
                break;

            }
        }

        DatabaseReference r = MainActivity.getSold().child(p.getSellingDate()).child(p.getBuyerName())
                .child(p.getProductCode()).child(p.getProductName());


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
}