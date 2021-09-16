package com.amine.bmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

public class ReservedProductList extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener {

    private LinearLayout productListLayout, sellLayout;
    private static ArrayList<Product> reservedProducts, onSearchItems;
    private static ArrayList<CartProduct> inCart;
    private TextView cart;
    private final DecimalFormat df =  new DecimalFormat("0.#");
    private final String[] spinItems =
            {"Product Name", "Product Code", "Company Name"};
    private String selectedCategory;
    private Spinner spinner;
    private Button btnMail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserved_product_list);
        initialize();
    }

    private void setSpinner(){
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinItems);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

    }

    private void initialize(){

        findViewById(R.id.reserved_progress).setVisibility(View.VISIBLE);
        productListLayout = findViewById(R.id.productListLayout);
        sellLayout = findViewById(R.id.sellLayout);
        spinner = findViewById(R.id.reserved_spinner);
        SearchView search = findViewById(R.id.reserved_search);
        btnMail = findViewById(R.id.reserved_btnMail);
        reservedProducts = MainActivity.getReservedProducts();
        inCart = new ArrayList<>();
        onSearchItems = new ArrayList<>();
        cart = findViewById(R.id.layout_cart);
        cart.setOnClickListener(this);
        spinner.setOnItemSelectedListener(this);
        btnMail.setOnClickListener(this);
        search.setOnQueryTextListener(this);
        readProductsInCart(() -> {
            findViewById(R.id.reserved_progress).setVisibility(View.GONE);
            setProductList();
            setSpinner();
            TextView tv = findViewById(R.id.reserved_txtNoReserved);
            setVisibilityOfMailButton();
            if(reservedProducts.size() == 0){
                tv.setVisibility(View.VISIBLE);
                String s = "Reserved is empty!";
                tv.setText(s);
            }else{
                tv.setVisibility(View.GONE);
            }
        });
    }

    private void setVisibilityOfMailButton(){
        File f = new File(getExternalFilesDir(MainActivity.appName), "Sell Document.pdf");
        if(f.exists()) btnMail.setVisibility(View.VISIBLE);
        else btnMail.setVisibility(View.GONE);
    }

    private boolean isMatched(String key, String value){
        key = key.toLowerCase();
        value = value.toLowerCase();
        int lenK = key.length();
        while (lenK >= 3){

            String subKey = key.substring(0, lenK);
            if(value.contains(subKey)){
                return true;
            }
            lenK--;

        }

        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedCategory = spinItems[position];
        onSearchItems.clear();
        setProductList();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        onSearchItems.clear();

        if(query.length() < 3){
            setProductList();
            return false;
        }

        switch (selectedCategory) {

            case "Product Name":

                for (int i = 0; i < reservedProducts.size(); i++) {
                    String value = reservedProducts.get(i).getProductName();
                    if(isMatched(query, value)){
                        onSearchItems.add(reservedProducts.get(i));
                    }
                }
                setOnSearchItems();

                break;


            case "Product Code":
                for (int i = 0; i < reservedProducts.size(); i++) {
                    String value = reservedProducts.get(i).getProductCode();
                    if(isMatched(query, value)) onSearchItems.add(reservedProducts.get(i));
                }

                setOnSearchItems();

                break;

            case "Company Name":

                for (int i = 0; i < reservedProducts.size(); i++) {
                    String value = reservedProducts.get(i).getCompanyName();
                    if(isMatched(query, value)) onSearchItems.add(reservedProducts.get(i));
                }

                setOnSearchItems();

                break;
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        onSearchItems.clear();

        if(newText.length() < 3){
            setProductList();
            return false;
        }

        switch (selectedCategory) {

            case "Product Name":

                for (int i = 0; i < reservedProducts.size(); i++) {
                    String value = reservedProducts.get(i).getProductName();
                    if(isMatched(newText, value)){
                        onSearchItems.add(reservedProducts.get(i));
                    }
                }
                setOnSearchItems();

                break;


            case "Product Code":
                for (int i = 0; i < reservedProducts.size(); i++) {
                    String value = reservedProducts.get(i).getProductCode();
                    if(isMatched(newText, value)) onSearchItems.add(reservedProducts.get(i));
                }

                setOnSearchItems();

                break;

            case "Company Name":

                for (int i = 0; i < reservedProducts.size(); i++) {
                    String value = reservedProducts.get(i).getCompanyName();
                    if(isMatched(newText, value)) onSearchItems.add(reservedProducts.get(i));
                }

                setOnSearchItems();

                break;
        }

        return false;
    }

    private interface ReadPInCart{
        void onCallback();
    }

    private void readProductsInCart(ReadPInCart read){

        MainActivity.getRootPath().child("cart").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                inCart.clear();
                if(snapshot.exists()){
                    for(DataSnapshot code : snapshot.getChildren()){
                        for(DataSnapshot product : code.getChildren()){
                            CartProduct p = product.getValue(CartProduct.class);
                            inCart.add(p);
                        }
                    }
                }
                String ss = "Cart: " + inCart.size();
                cart.setText(ss);
                if(inCart.size() > 0){
                    cart.setTextSize(20);
                    cart.setTextColor(Color.RED);
                }

                read.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setProductList(){
        productListLayout.removeAllViews();
        sellLayout.removeAllViews();
        Button[] btnSell = new Button[reservedProducts.size() + 1],
                btnAdd = new Button[reservedProducts.size() + 1],
                btnEdit = new Button[reservedProducts.size() + 1];
        for(int i = 0; i < reservedProducts.size(); i++){
            LinearLayout ll = new LinearLayout(ReservedProductList.this),
                    sellBtnLayout = new LinearLayout(ReservedProductList.this),
                    fake = new LinearLayout(ReservedProductList.this),
                    fake1 = new LinearLayout(ReservedProductList.this);

            ll.setOrientation(LinearLayout.VERTICAL);
            sellBtnLayout.setOrientation(LinearLayout.VERTICAL);
            productListLayout.addView(ll);
            sellLayout.addView(sellBtnLayout);



            TextView name = new TextView(ReservedProductList.this),
                    code = new TextView(ReservedProductList.this),
                    amount = new TextView(ReservedProductList.this),
                    purPrice = new TextView(ReservedProductList.this),
                    sellPrice = new TextView(ReservedProductList.this),
                    purchaseDate = new TextView(ReservedProductList.this),
                    compName = new TextView(ReservedProductList.this);
            btnSell[i] = new Button(ReservedProductList.this);
            btnAdd[i] = new Button(ReservedProductList.this);
            btnEdit[i] = new Button(ReservedProductList.this);

            ll.addView(name);
            ll.addView(code);
            ll.addView(compName);
            ll.addView(amount);
            ll.addView(purPrice);
            ll.addView(sellPrice);
            ll.addView(purchaseDate);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT),
                    paramsLl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400);

            ll.setLayoutParams(paramsLl);
            ll.setGravity(Gravity.CENTER);

            name.setLayoutParams(params);
            name.setGravity(Gravity.CENTER);
            name.setTextColor(Color.CYAN);
            name.setTextSize(20);

            code.setLayoutParams(params);
            code.setGravity(Gravity.CENTER);
            code.setTextSize(17);

            compName.setLayoutParams(params);
            compName.setGravity(Gravity.CENTER);
            compName.setTextSize(17);

            amount.setLayoutParams(params);
            amount.setGravity(Gravity.CENTER);
            amount.setTextSize(17);

            purPrice.setLayoutParams(params);
            purPrice.setGravity(Gravity.CENTER);
            purPrice.setTextSize(17);

            sellPrice.setLayoutParams(params);
            sellPrice.setGravity(Gravity.CENTER);
            sellPrice.setTextSize(17);

            purchaseDate.setLayoutParams(params);
            purchaseDate.setGravity(Gravity.CENTER);
            purchaseDate.setTextSize(17);



            String s;

            s = "পণ্যের নাম: " + reservedProducts.get(i).getProductName();

            name.setText(s);
            s = "পণ্যের কোড: " + reservedProducts.get(i).getProductCode();
            code.setText(s);

            s = "কোম্পানিঃ " + reservedProducts.get(i).getCompanyName();
            compName.setText(s);

            s = "Reserved: " + df.format(Double.parseDouble(reservedProducts.get(i).getReservedAmount())) + " টি";
            amount.setText(s);
            s = "ইউনিট প্রতি কেনা মুল্য: " + df.format(Double.parseDouble(reservedProducts.get(i).getPurchasePricePerUnit()));
            purPrice.setText(s);
            s = "ইউনিট প্রতি বিক্রয় মুল্য: " + df.format(Double.parseDouble(reservedProducts.get(i).getSellingPricePerUnit()));
            sellPrice.setText(s);

            s = "ক্রয়ের তারিখঃ " + reservedProducts.get(i).getPurchaseDate();

            purchaseDate.setText(s);


            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    400);
            sellBtnLayout.setLayoutParams(params1);
            sellBtnLayout.setGravity(Gravity.CENTER);

            sellBtnLayout.addView(btnSell[i]);
            sellBtnLayout.addView(btnAdd[i]);
            sellBtnLayout.addView(btnEdit[i]);

            btnSell[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            btnAdd[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            btnEdit[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));


            s = "Sell";
            btnSell[i].setText(s);
            s = "Add";
            btnAdd[i].setText(s);
            s = "Edit";
            btnEdit[i].setText(s);


            sellLayout.addView(fake1);
            fake1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 7));
            productListLayout.addView(fake);
            fake.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 7));

            fake.setBackgroundColor(Color.GREEN);
            fake1.setBackgroundColor(Color.GREEN);

            int finalI = i;
            btnSell[i].setOnClickListener(v -> {

                Product product1 = reservedProducts.get(finalI);
                CartProduct product = new CartProduct(product1.getProductCode(), product1.getProductName(),
                        product1.getReservedAmount(), product1.getPurchaseDate(),
                        product1.getPurchasePricePerUnit(), product1.getSellingPricePerUnit(),
                        product1.getCompanyName(), "0");

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
                        selPric.setText(product.getSellingPricePerUnit());

                        String ss = "Product name: " + product.getProductName();
                        name.setText(ss);
                        name.setTextSize(20);
                        name.setTextColor(Color.CYAN);

                        findViewById(R.id.layout_btnSell).setOnClickListener(this);
                        findViewById(R.id.layout_btnCancel).setOnClickListener(this);
                        findViewById(R.id.layout_imgCalc).setOnClickListener(this);
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
                            AddProduct.saveCartProductToStorage(product);
                            readProductsInCart(ReservedProductList.this::setProductList);
                            dismiss();
                        }
                        if(id == R.id.layout_btnCancel){
                            dismiss();
                        }
                        if(id == R.id.layout_imgCalc){
                            CalculatorInterface calc =
                                    new CalculatorInterface(ReservedProductList.this,
                                            R.id.layout_edtSellingPrice,
                                    (s, ID) -> {
                                        EditText ed = findViewById(ID);
                                        ed.setText(s);
                                    });
                            calc.show();
                            WindowManager.LayoutParams p = getDisplaySize(calc);
                            calc.getWindow().setAttributes(p);
                        }
                    }
                }

                SellDialog sellDialog = new SellDialog(ReservedProductList.this);
                sellDialog.show();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(sellDialog.getWindow().getAttributes());
                int dialogWindowWidth = (int) (displayWidth * 0.9f);
                int dialogWindowHeight = (int) (displayHeight * 0.5f);
                layoutParams.width = dialogWindowWidth;
                layoutParams.height = dialogWindowHeight;
                sellDialog.getWindow().setAttributes(layoutParams);
            });

            btnAdd[i].setOnClickListener(v -> {

                Product product = reservedProducts.get(finalI);

                class SellDialog extends Dialog implements View.OnClickListener{

                    EditText editText, editText1, edtAmount, edtPurch, edtSell, edtSellP, add_edtCompanyName;

                    public SellDialog(@NonNull Context context) {
                        super(context);
                    }

                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        setContentView(R.layout.activity_add_product);
                        initialize();
                    }

                    private void initialize(){
                        editText = findViewById(R.id.addProductEdtProductName);
                        editText1 = findViewById(R.id.addProductEdtProductCode);
                        edtAmount = findViewById(R.id.addProductEdtProductAmount);
                        edtPurch = findViewById(R.id.addProductEdtPurchasePrice);
                        edtSellP = findViewById(R.id.addProductEdtSellingPrice);
                        add_edtCompanyName = findViewById(R.id.add_edtCompanyName);

                        editText.setText(product.getProductName());
                        editText1.setText(product.getProductCode());
                        edtAmount.setText(df.format(Double.parseDouble(product.getReservedAmount())));
                        edtSellP.setText(df.format(Double.parseDouble(product.getSellingPricePerUnit())));
                        add_edtCompanyName.setText(product.getCompanyName());


                        Button button = findViewById(R.id.addProductBtnAdd);
                        String s = "Save";
                        button.setText(s);

                        button.setOnClickListener(this);
                        findViewById(R.id.add_imgCalcAmount).setOnClickListener(this);
                        findViewById(R.id.add_imgCalcPPrice).setOnClickListener(this);
                        findViewById(R.id.add_imgCalcSPrice).setOnClickListener(this);

                    }

                    @Override
                    public void onClick(View v) {
                        int id = v.getId();
                        if(id == R.id.addProductBtnAdd){

                            String productName = editText.getText().toString().trim(),
                                    productCode = editText1.getText().toString().trim(),
                                    reservedAmount = edtAmount.getText().toString().trim(),
                                    sellingPricePerUnit = edtSellP.getText().toString().trim(),
                                    companyName = add_edtCompanyName.getText().toString().trim(),
                                    totalPurchPrice = edtPurch.getText().toString().trim();


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

                            if(sellingPricePerUnit.isEmpty()){
                                edtSellP.setError("Enter selling price");
                                edtSellP.requestFocus();
                                return;
                            }
                            if(totalPurchPrice.isEmpty()){
                                edtPurch.setError("Enter Total price");
                                edtPurch.requestFocus();
                                return;
                            }
                            double amount = Double.parseDouble(reservedAmount);
                            if(amount < 1.0){
                                Toast.makeText(ReservedProductList.this,
                                        "Amount can't be less than 1", Toast.LENGTH_LONG).show();
                                return;
                            }

                            String purchPricePerUnit = (Double.parseDouble(totalPurchPrice)/amount) + "";

                            Product product1 = new Product(productCode, productName, reservedAmount, getTodayDate(),
                                    purchPricePerUnit, sellingPricePerUnit, companyName);

                            ReservedProductList.this.findViewById(R.id.reserved_progress)
                                    .setVisibility(View.VISIBLE);
                            ReservedProductList.this.findViewById(R.id.reserved_scroll)
                                    .setVisibility(View.GONE);

                            AddProduct.writeProduct(product1,
                                    () -> MainActivity.readProductsFromStorage(
                                            () -> {
                                                ReservedProductList.this.findViewById(
                                                        R.id.reserved_progress)
                                                        .setVisibility(View.GONE);
                                                ReservedProductList.this.findViewById(
                                                        R.id.reserved_scroll)
                                                        .setVisibility(View.VISIBLE);
                                                setProductList();
                                            }
                                    ));



                            dismiss();
                        }
                        if(id == R.id.add_imgCalcAmount){
                            CalculatorInterface calc = new CalculatorInterface(ReservedProductList.this,
                                    R.id.addProductEdtProductAmount,
                                    (s, ID) -> {
                                        EditText ed = findViewById(ID);
                                        ed.setText(s);
                                    });
                            calc.show();
                            WindowManager.LayoutParams p = getDisplaySize(calc);
                            calc.getWindow().setAttributes(p);
                        }
                        if(id == R.id.add_imgCalcPPrice){
                            CalculatorInterface calc = new CalculatorInterface(ReservedProductList.this, R.id.addProductEdtPurchasePrice,
                                    (s, ID) -> {
                                        EditText ed = findViewById(ID);
                                        ed.setText(s);
                                    });
                            calc.show();
                            WindowManager.LayoutParams p = getDisplaySize(calc);
                            calc.getWindow().setAttributes(p);
                        }
                        if(id == R.id.add_imgCalcSPrice){
                            CalculatorInterface calc = new CalculatorInterface(ReservedProductList.this, R.id.addProductEdtSellingPrice,
                                    (s, ID) -> {
                                        EditText ed = findViewById(ID);
                                        ed.setText(s);
                                    });

                            calc.show();
                            WindowManager.LayoutParams p = getDisplaySize(calc);
                            calc.getWindow().setAttributes(p);
                        }
                    }
                }

                SellDialog sellDialog = new SellDialog(ReservedProductList.this);
                sellDialog.show();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(sellDialog.getWindow().getAttributes());
                int dialogWindowWidth = (int) (displayWidth * 0.95f);
                int dialogWindowHeight = (int) (displayHeight * 0.95f);
                layoutParams.width = dialogWindowWidth;
                layoutParams.height = dialogWindowHeight;
                sellDialog.getWindow().setAttributes(layoutParams);


            });

            btnEdit[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Product product = reservedProducts.get(finalI);

                    class WantPermission extends Dialog implements View.OnClickListener{

                        public WantPermission(@NonNull Context context) {
                            super(context);
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
                            String s = "এডিট করলে এই পণ্য সংক্রান্ত পুর্বের সকল ইনফরমেশন মুছে যাবে।\n" +
                                    "এবং এখন দেয়া ইনফরমেশন যুক্ত হবে।\n" +
                                    "টিপসঃ নতুন করে যোগ করতে চাইলে \"Add\" বাটন টি ক্লিক করুন।";

                            tv.setText(s);
                        }

                        @Override
                        public void onClick(View v) {
                            int id = v.getId();
                            if(id == R.id.permission_btnYes){


                                class SellDialog extends Dialog implements View.OnClickListener{

                                    EditText editText, editText1, edtAmount, edtPurch, edtSellP,
                                            add_edtCompanyName;

                                    public SellDialog(@NonNull Context context) {
                                        super(context);
                                    }

                                    @Override
                                    protected void onCreate(Bundle savedInstanceState) {
                                        super.onCreate(savedInstanceState);
                                        setContentView(R.layout.activity_add_product);
                                        initialize();
                                    }

                                    private void initialize(){
                                        editText = findViewById(R.id.addProductEdtProductName);
                                        editText1 = findViewById(R.id.addProductEdtProductCode);
                                        edtAmount = findViewById(R.id.addProductEdtProductAmount);
                                        edtPurch = findViewById(R.id.addProductEdtPurchasePrice);
                                        edtSellP = findViewById(R.id.addProductEdtSellingPrice);
                                        add_edtCompanyName = findViewById(R.id.add_edtCompanyName);

                                        editText.setText(product.getProductName());
                                        editText1.setText(product.getProductCode());

                                        edtSellP.setText(df.format(Double.parseDouble(
                                                product.getSellingPricePerUnit())));
                                        add_edtCompanyName.setText(product.getCompanyName());


                                        Button button = findViewById(R.id.addProductBtnAdd);
                                        String s = "Save";
                                        button.setText(s);

                                        button.setOnClickListener(this);
                                        findViewById(R.id.add_imgCalcAmount).setOnClickListener(this);
                                        findViewById(R.id.add_imgCalcPPrice).setOnClickListener(this);
                                        findViewById(R.id.add_imgCalcSPrice).setOnClickListener(this);

                                    }

                                    @Override
                                    public void onClick(View v) {
                                        int id = v.getId();
                                        if(id == R.id.addProductBtnAdd){

                                            String productName = editText.getText().toString().trim(),
                                                    productCode = editText1.getText().toString().trim(),
                                                    reservedAmount = edtAmount.getText().toString().trim(),
                                                    sellingPricePerUnit = edtSellP.getText().toString().trim(),
                                                    companyName = add_edtCompanyName.getText().toString().trim(),
                                                    totalPurchPrice = edtPurch.getText().toString().trim();


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

                                            if(sellingPricePerUnit.isEmpty()){
                                                edtSellP.setError("Enter selling price");
                                                edtSellP.requestFocus();
                                                return;
                                            }
                                            if(totalPurchPrice.isEmpty()){
                                                edtPurch.setError("Enter Total price");
                                                edtPurch.requestFocus();
                                                return;
                                            }
                                            double amount = Double.parseDouble(reservedAmount);
                                            if(amount < 1.0){
                                                Toast.makeText(ReservedProductList.this,
                                                        "Amount can't be less than 1",
                                                        Toast.LENGTH_LONG).show();
                                                return;
                                            }

                                            String purchPricePerUnit =
                                                    (Double.parseDouble(totalPurchPrice)/amount) + "";

                                            Product product1 = new Product(productCode, productName,
                                                    reservedAmount, getTodayDate(),
                                                    purchPricePerUnit, sellingPricePerUnit, companyName);


                                            ReservedProductList.this.findViewById(R.id.reserved_progress)
                                                    .setVisibility(View.VISIBLE);
                                            ReservedProductList.this.findViewById(R.id.reserved_scroll)
                                                    .setVisibility(View.GONE);

                                            overrideProduct(product1);


                                            MainActivity.readProductsFromStorage(
                                                    () -> {
                                                        ReservedProductList.this.findViewById(
                                                                R.id.reserved_progress)
                                                                .setVisibility(View.GONE);
                                                        ReservedProductList.this.findViewById(
                                                                R.id.reserved_scroll)
                                                                .setVisibility(View.VISIBLE);
                                                        setProductList();
                                                    }
                                            );



                                            dismiss();
                                        }
                                        if(id == R.id.add_imgCalcAmount){
                                            CalculatorInterface calc = new CalculatorInterface(
                                                    ReservedProductList.this,
                                                    R.id.addProductEdtProductAmount,
                                                    (s, ID) -> {
                                                        EditText ed = findViewById(ID);
                                                        ed.setText(s);
                                                    });
                                            calc.show();
                                            WindowManager.LayoutParams p = getDisplaySize(calc);
                                            calc.getWindow().setAttributes(p);
                                        }
                                        if(id == R.id.add_imgCalcPPrice){
                                            CalculatorInterface calc = new CalculatorInterface(
                                                    ReservedProductList.this,
                                                    R.id.addProductEdtPurchasePrice,
                                                    (s, ID) -> {
                                                        EditText ed = findViewById(ID);
                                                        ed.setText(s);
                                                    });
                                            calc.show();
                                            WindowManager.LayoutParams p = getDisplaySize(calc);
                                            calc.getWindow().setAttributes(p);
                                        }
                                        if(id == R.id.add_imgCalcSPrice){
                                            CalculatorInterface calc = new CalculatorInterface(
                                                    ReservedProductList.this,
                                                    R.id.addProductEdtSellingPrice,
                                                    (s, ID) -> {
                                                        EditText ed = findViewById(ID);
                                                        ed.setText(s);
                                                    });

                                            calc.show();
                                            WindowManager.LayoutParams p = getDisplaySize(calc);
                                            calc.getWindow().setAttributes(p);
                                        }
                                    }
                                }

                                SellDialog sellDialog = new SellDialog(ReservedProductList.this);
                                sellDialog.show();
                                DisplayMetrics displayMetrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                int displayWidth = displayMetrics.widthPixels;
                                int displayHeight = displayMetrics.heightPixels;
                                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                                layoutParams.copyFrom(sellDialog.getWindow().getAttributes());
                                int dialogWindowWidth = (int) (displayWidth * 0.95f);
                                int dialogWindowHeight = (int) (displayHeight * 0.95f);
                                layoutParams.width = dialogWindowWidth;
                                layoutParams.height = dialogWindowHeight;
                                sellDialog.getWindow().setAttributes(layoutParams);

                                dismiss();
                            }
                            else if(id == R.id.permission_btnNo){
                                dismiss();
                            }
                        }
                    }

                    WantPermission w = new WantPermission(ReservedProductList.this);
                    w.show();
                }
            });

        }

    }

    private void overrideProduct(Product p){
        DatabaseReference r = MainActivity.getReserves().child(p.getProductCode())
                .child(p.getProductName());

        r.setValue(p);
    }

    private void setOnSearchItems(){
        productListLayout.removeAllViews();
        sellLayout.removeAllViews();
        Button[] btnSell = new Button[onSearchItems.size() + 1],
                btnAdd = new Button[onSearchItems.size() + 1],
                btnEdit = new Button[onSearchItems.size() + 1];
        for(int i = 0; i < onSearchItems.size(); i++){
            LinearLayout ll = new LinearLayout(ReservedProductList.this),
                    sellBtnLayout = new LinearLayout(ReservedProductList.this),
                    fake = new LinearLayout(ReservedProductList.this),
                    fake1 = new LinearLayout(ReservedProductList.this);

            ll.setOrientation(LinearLayout.VERTICAL);
            sellBtnLayout.setOrientation(LinearLayout.VERTICAL);
            productListLayout.addView(ll);
            sellLayout.addView(sellBtnLayout);



            TextView name = new TextView(ReservedProductList.this),
                    code = new TextView(ReservedProductList.this),
                    amount = new TextView(ReservedProductList.this),
                    purPrice = new TextView(ReservedProductList.this),
                    sellPrice = new TextView(ReservedProductList.this),
                    purchaseDate = new TextView(ReservedProductList.this),
                    compName = new TextView(ReservedProductList.this);
            btnSell[i] = new Button(ReservedProductList.this);
            btnAdd[i] = new Button(ReservedProductList.this);
            btnEdit[i] = new Button(ReservedProductList.this);

            ll.addView(name);
            ll.addView(code);
            ll.addView(compName);
            ll.addView(amount);
            ll.addView(purPrice);
            ll.addView(sellPrice);
            ll.addView(purchaseDate);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT),
                    paramsLl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400);

            ll.setLayoutParams(paramsLl);
            ll.setGravity(Gravity.CENTER);

            name.setLayoutParams(params);
            name.setGravity(Gravity.CENTER);
            name.setTextColor(Color.CYAN);
            name.setTextSize(20);

            code.setLayoutParams(params);
            code.setGravity(Gravity.CENTER);
            code.setTextSize(17);

            compName.setLayoutParams(params);
            compName.setGravity(Gravity.CENTER);
            compName.setTextSize(17);

            amount.setLayoutParams(params);
            amount.setGravity(Gravity.CENTER);
            amount.setTextSize(17);

            purPrice.setLayoutParams(params);
            purPrice.setGravity(Gravity.CENTER);
            purPrice.setTextSize(17);

            sellPrice.setLayoutParams(params);
            sellPrice.setGravity(Gravity.CENTER);
            sellPrice.setTextSize(17);

            purchaseDate.setLayoutParams(params);
            purchaseDate.setGravity(Gravity.CENTER);
            purchaseDate.setTextSize(17);



            String s;

            s = "পণ্যের নাম: " + onSearchItems.get(i).getProductName();

            name.setText(s);
            s = "পণ্যের কোড: " + onSearchItems.get(i).getProductCode();
            code.setText(s);

            s = "কোম্পানিঃ " + onSearchItems.get(i).getCompanyName();
            compName.setText(s);

            s = "Reserved: " + df.format(Double.parseDouble(onSearchItems.get(i).getReservedAmount())) + " টি";
            amount.setText(s);
            s = "ইউনিট প্রতি কেনা মুল্য: " + df.format(Double.parseDouble(onSearchItems.get(i).getPurchasePricePerUnit()));
            purPrice.setText(s);
            s = "ইউনিট প্রতি বিক্রয় মুল্য: " + df.format(Double.parseDouble(onSearchItems.get(i).getSellingPricePerUnit()));
            sellPrice.setText(s);

            s = "ক্রয়ের তারিখঃ " + onSearchItems.get(i).getPurchaseDate();

            purchaseDate.setText(s);


            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    400);
            sellBtnLayout.setLayoutParams(params1);
            sellBtnLayout.setGravity(Gravity.CENTER);

            sellBtnLayout.addView(btnSell[i]);
            sellBtnLayout.addView(btnAdd[i]);
            sellBtnLayout.addView(btnEdit[i]);

            btnSell[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            btnAdd[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            btnEdit[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));


            s = "Sell";
            btnSell[i].setText(s);
            s = "Add";
            btnAdd[i].setText(s);
            s = "Edit";
            btnEdit[i].setText(s);


            sellLayout.addView(fake1);
            fake1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 7));
            productListLayout.addView(fake);
            fake.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 7));

            fake.setBackgroundColor(Color.GREEN);
            fake1.setBackgroundColor(Color.GREEN);

            int finalI = i;
            btnSell[i].setOnClickListener(v -> {

                Product product1 = onSearchItems.get(finalI);
                CartProduct product = new CartProduct(product1.getProductCode(), product1.getProductName(),
                        product1.getReservedAmount(), product1.getPurchaseDate(),
                        product1.getPurchasePricePerUnit(), product1.getSellingPricePerUnit(),
                        product1.getCompanyName(), "0");

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
                        selPric.setText(product.getSellingPricePerUnit());

                        String ss = "Product name: " + product.getProductName();
                        name.setText(ss);
                        name.setTextSize(20);
                        name.setTextColor(Color.CYAN);

                        findViewById(R.id.layout_btnSell).setOnClickListener(this);
                        findViewById(R.id.layout_btnCancel).setOnClickListener(this);
                        findViewById(R.id.layout_imgCalc).setOnClickListener(this);
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
                            AddProduct.saveCartProductToStorage(product);
                            readProductsInCart(ReservedProductList.this::setProductList);
                            dismiss();
                        }
                        if(id == R.id.layout_btnCancel){
                            dismiss();
                        }
                        if(id == R.id.layout_imgCalc){
                            CalculatorInterface calc =
                                    new CalculatorInterface(ReservedProductList.this,
                                            R.id.layout_edtSellingPrice,
                                            (s, ID) -> {
                                                EditText ed = findViewById(ID);
                                                ed.setText(s);
                                            });
                            calc.show();
                            WindowManager.LayoutParams p = getDisplaySize(calc);
                            calc.getWindow().setAttributes(p);
                        }
                    }
                }

                SellDialog sellDialog = new SellDialog(ReservedProductList.this);
                sellDialog.show();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(sellDialog.getWindow().getAttributes());
                int dialogWindowWidth = (int) (displayWidth * 0.9f);
                int dialogWindowHeight = (int) (displayHeight * 0.5f);
                layoutParams.width = dialogWindowWidth;
                layoutParams.height = dialogWindowHeight;
                sellDialog.getWindow().setAttributes(layoutParams);
            });

            btnAdd[i].setOnClickListener(v -> {

                Product product = onSearchItems.get(finalI);

                class SellDialog extends Dialog implements View.OnClickListener{

                    EditText editText, editText1, edtAmount, edtPurch, edtSell, edtSellP, add_edtCompanyName;

                    public SellDialog(@NonNull Context context) {
                        super(context);
                    }

                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        setContentView(R.layout.activity_add_product);
                        initialize();
                    }

                    private void initialize(){
                        editText = findViewById(R.id.addProductEdtProductName);
                        editText1 = findViewById(R.id.addProductEdtProductCode);
                        edtAmount = findViewById(R.id.addProductEdtProductAmount);
                        edtPurch = findViewById(R.id.addProductEdtPurchasePrice);
                        edtSellP = findViewById(R.id.addProductEdtSellingPrice);
                        add_edtCompanyName = findViewById(R.id.add_edtCompanyName);

                        editText.setText(product.getProductName());
                        editText1.setText(product.getProductCode());
                        edtAmount.setText(df.format(Double.parseDouble(product.getReservedAmount())));
                        edtSellP.setText(df.format(Double.parseDouble(product.getSellingPricePerUnit())));
                        add_edtCompanyName.setText(product.getCompanyName());


                        Button button = findViewById(R.id.addProductBtnAdd);
                        String s = "Save";
                        button.setText(s);

                        button.setOnClickListener(this);
                        findViewById(R.id.add_imgCalcAmount).setOnClickListener(this);
                        findViewById(R.id.add_imgCalcPPrice).setOnClickListener(this);
                        findViewById(R.id.add_imgCalcSPrice).setOnClickListener(this);

                    }

                    @Override
                    public void onClick(View v) {
                        int id = v.getId();
                        if(id == R.id.addProductBtnAdd){

                            String productName = editText.getText().toString().trim(),
                                    productCode = editText1.getText().toString().trim(),
                                    reservedAmount = edtAmount.getText().toString().trim(),
                                    sellingPricePerUnit = edtSellP.getText().toString().trim(),
                                    companyName = add_edtCompanyName.getText().toString().trim(),
                                    totalPurchPrice = edtPurch.getText().toString().trim();


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

                            if(sellingPricePerUnit.isEmpty()){
                                edtSellP.setError("Enter selling price");
                                edtSellP.requestFocus();
                                return;
                            }
                            if(totalPurchPrice.isEmpty()){
                                edtPurch.setError("Enter Total price");
                                edtPurch.requestFocus();
                                return;
                            }
                            double amount = Double.parseDouble(reservedAmount);
                            if(amount < 1.0){
                                Toast.makeText(ReservedProductList.this,
                                        "Amount can't be less than 1", Toast.LENGTH_LONG).show();
                                return;
                            }

                            String purchPricePerUnit = (Double.parseDouble(totalPurchPrice)/amount) + "";

                            Product product1 = new Product(productCode, productName, reservedAmount, getTodayDate(),
                                    purchPricePerUnit, sellingPricePerUnit, companyName);

                            ReservedProductList.this.findViewById(R.id.reserved_progress)
                                    .setVisibility(View.VISIBLE);
                            ReservedProductList.this.findViewById(R.id.reserved_scroll)
                                    .setVisibility(View.GONE);

                            AddProduct.writeProduct(product1,
                                    () -> MainActivity.readProductsFromStorage(
                                            () -> {
                                                ReservedProductList.this.findViewById(
                                                        R.id.reserved_progress)
                                                        .setVisibility(View.GONE);
                                                ReservedProductList.this.findViewById(
                                                        R.id.reserved_scroll)
                                                        .setVisibility(View.VISIBLE);
                                                setProductList();
                                            }
                                    ));



                            dismiss();
                        }
                        if(id == R.id.add_imgCalcAmount){
                            CalculatorInterface calc = new CalculatorInterface(ReservedProductList.this,
                                    R.id.addProductEdtProductAmount,
                                    (s, ID) -> {
                                        EditText ed = findViewById(ID);
                                        ed.setText(s);
                                    });
                            calc.show();
                            WindowManager.LayoutParams p = getDisplaySize(calc);
                            calc.getWindow().setAttributes(p);
                        }
                        if(id == R.id.add_imgCalcPPrice){
                            CalculatorInterface calc = new CalculatorInterface(ReservedProductList.this, R.id.addProductEdtPurchasePrice,
                                    (s, ID) -> {
                                        EditText ed = findViewById(ID);
                                        ed.setText(s);
                                    });
                            calc.show();
                            WindowManager.LayoutParams p = getDisplaySize(calc);
                            calc.getWindow().setAttributes(p);
                        }
                        if(id == R.id.add_imgCalcSPrice){
                            CalculatorInterface calc = new CalculatorInterface(ReservedProductList.this, R.id.addProductEdtSellingPrice,
                                    (s, ID) -> {
                                        EditText ed = findViewById(ID);
                                        ed.setText(s);
                                    });

                            calc.show();
                            WindowManager.LayoutParams p = getDisplaySize(calc);
                            calc.getWindow().setAttributes(p);
                        }
                    }
                }

                SellDialog sellDialog = new SellDialog(ReservedProductList.this);
                sellDialog.show();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(sellDialog.getWindow().getAttributes());
                int dialogWindowWidth = (int) (displayWidth * 0.95f);
                int dialogWindowHeight = (int) (displayHeight * 0.95f);
                layoutParams.width = dialogWindowWidth;
                layoutParams.height = dialogWindowHeight;
                sellDialog.getWindow().setAttributes(layoutParams);


            });

            btnEdit[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Product product = onSearchItems.get(finalI);

                    class WantPermission extends Dialog implements View.OnClickListener{

                        public WantPermission(@NonNull Context context) {
                            super(context);
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
                            String s = "এডিট করলে এই পণ্য সংক্রান্ত পুর্বের সকল ইনফরমেশন মুছে যাবে।\n" +
                                    "এবং এখন দেয়া ইনফরমেশন যুক্ত হবে।\n" +
                                    "টিপসঃ নতুন করে যোগ করতে চাইলে \"Add\" বাটন টি ক্লিক করুন।";

                            tv.setText(s);
                        }

                        @Override
                        public void onClick(View v) {
                            int id = v.getId();
                            if(id == R.id.permission_btnYes){


                                class SellDialog extends Dialog implements View.OnClickListener{

                                    EditText editText, editText1, edtAmount, edtPurch, edtSellP,
                                            add_edtCompanyName;

                                    public SellDialog(@NonNull Context context) {
                                        super(context);
                                    }

                                    @Override
                                    protected void onCreate(Bundle savedInstanceState) {
                                        super.onCreate(savedInstanceState);
                                        setContentView(R.layout.activity_add_product);
                                        initialize();
                                    }

                                    private void initialize(){
                                        editText = findViewById(R.id.addProductEdtProductName);
                                        editText1 = findViewById(R.id.addProductEdtProductCode);
                                        edtAmount = findViewById(R.id.addProductEdtProductAmount);
                                        edtPurch = findViewById(R.id.addProductEdtPurchasePrice);
                                        edtSellP = findViewById(R.id.addProductEdtSellingPrice);
                                        add_edtCompanyName = findViewById(R.id.add_edtCompanyName);

                                        editText.setText(product.getProductName());
                                        editText1.setText(product.getProductCode());

                                        edtSellP.setText(df.format(Double.parseDouble(
                                                product.getSellingPricePerUnit())));
                                        add_edtCompanyName.setText(product.getCompanyName());


                                        Button button = findViewById(R.id.addProductBtnAdd);
                                        String s = "Save";
                                        button.setText(s);

                                        button.setOnClickListener(this);
                                        findViewById(R.id.add_imgCalcAmount).setOnClickListener(this);
                                        findViewById(R.id.add_imgCalcPPrice).setOnClickListener(this);
                                        findViewById(R.id.add_imgCalcSPrice).setOnClickListener(this);

                                    }

                                    @Override
                                    public void onClick(View v) {
                                        int id = v.getId();
                                        if(id == R.id.addProductBtnAdd){

                                            String productName = editText.getText().toString().trim(),
                                                    productCode = editText1.getText().toString().trim(),
                                                    reservedAmount = edtAmount.getText().toString().trim(),
                                                    sellingPricePerUnit = edtSellP.getText().toString().trim(),
                                                    companyName = add_edtCompanyName.getText().toString().trim(),
                                                    totalPurchPrice = edtPurch.getText().toString().trim();


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

                                            if(sellingPricePerUnit.isEmpty()){
                                                edtSellP.setError("Enter selling price");
                                                edtSellP.requestFocus();
                                                return;
                                            }
                                            if(totalPurchPrice.isEmpty()){
                                                edtPurch.setError("Enter Total price");
                                                edtPurch.requestFocus();
                                                return;
                                            }
                                            double amount = Double.parseDouble(reservedAmount);
                                            if(amount < 1.0){
                                                Toast.makeText(ReservedProductList.this,
                                                        "Amount can't be less than 1",
                                                        Toast.LENGTH_LONG).show();
                                                return;
                                            }

                                            String purchPricePerUnit =
                                                    (Double.parseDouble(totalPurchPrice)/amount) + "";

                                            Product product1 = new Product(productCode, productName,
                                                    reservedAmount, getTodayDate(),
                                                    purchPricePerUnit, sellingPricePerUnit, companyName);


                                            ReservedProductList.this.findViewById(R.id.reserved_progress)
                                                    .setVisibility(View.VISIBLE);
                                            ReservedProductList.this.findViewById(R.id.reserved_scroll)
                                                    .setVisibility(View.GONE);

                                            overrideProduct(product1);


                                            MainActivity.readProductsFromStorage(
                                                    () -> {
                                                        ReservedProductList.this.findViewById(
                                                                R.id.reserved_progress)
                                                                .setVisibility(View.GONE);
                                                        ReservedProductList.this.findViewById(
                                                                R.id.reserved_scroll)
                                                                .setVisibility(View.VISIBLE);
                                                        setProductList();
                                                    }
                                            );



                                            dismiss();
                                        }
                                        if(id == R.id.add_imgCalcAmount){
                                            CalculatorInterface calc = new CalculatorInterface(
                                                    ReservedProductList.this,
                                                    R.id.addProductEdtProductAmount,
                                                    (s, ID) -> {
                                                        EditText ed = findViewById(ID);
                                                        ed.setText(s);
                                                    });
                                            calc.show();
                                            WindowManager.LayoutParams p = getDisplaySize(calc);
                                            calc.getWindow().setAttributes(p);
                                        }
                                        if(id == R.id.add_imgCalcPPrice){
                                            CalculatorInterface calc = new CalculatorInterface(
                                                    ReservedProductList.this,
                                                    R.id.addProductEdtPurchasePrice,
                                                    (s, ID) -> {
                                                        EditText ed = findViewById(ID);
                                                        ed.setText(s);
                                                    });
                                            calc.show();
                                            WindowManager.LayoutParams p = getDisplaySize(calc);
                                            calc.getWindow().setAttributes(p);
                                        }
                                        if(id == R.id.add_imgCalcSPrice){
                                            CalculatorInterface calc = new CalculatorInterface(
                                                    ReservedProductList.this,
                                                    R.id.addProductEdtSellingPrice,
                                                    (s, ID) -> {
                                                        EditText ed = findViewById(ID);
                                                        ed.setText(s);
                                                    });

                                            calc.show();
                                            WindowManager.LayoutParams p = getDisplaySize(calc);
                                            calc.getWindow().setAttributes(p);
                                        }
                                    }
                                }

                                SellDialog sellDialog = new SellDialog(ReservedProductList.this);
                                sellDialog.show();
                                DisplayMetrics displayMetrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                int displayWidth = displayMetrics.widthPixels;
                                int displayHeight = displayMetrics.heightPixels;
                                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                                layoutParams.copyFrom(sellDialog.getWindow().getAttributes());
                                int dialogWindowWidth = (int) (displayWidth * 0.95f);
                                int dialogWindowHeight = (int) (displayHeight * 0.95f);
                                layoutParams.width = dialogWindowWidth;
                                layoutParams.height = dialogWindowHeight;
                                sellDialog.getWindow().setAttributes(layoutParams);

                                dismiss();
                            }
                            else if(id == R.id.permission_btnNo){
                                dismiss();
                            }
                        }
                    }

                    WantPermission w = new WantPermission(ReservedProductList.this);
                    w.show();
                }
            });

        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.layout_cart){
            if(inCart.size() > 0){
                Intent intent = new Intent(ReservedProductList.this, CartActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(ReservedProductList.this, "No product in Cart!",
                        Toast.LENGTH_LONG).show();
            }
        }
        else if(id == R.id.reserved_btnMail){
            sendMail();
        }

    }

    private void sendMail(){
        File file = new File(getExternalFilesDir(MainActivity.appName), "Sell Document.pdf");
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);

        Intent emailSelectorIntent = new Intent(Intent.ACTION_SENDTO);
        emailSelectorIntent.setData(Uri.parse("mailto:"));

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"iaminul237@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sell Document");
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        emailIntent.setSelector( emailSelectorIntent );
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

        if( emailIntent.resolveActivity(getPackageManager()) != null )
            startActivity(emailIntent);
    }

    public static ArrayList<CartProduct> getInCart() {
        return inCart;
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
                    Toast.makeText(ReservedProductList.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ReservedProductList.this, "Can't include negative numbers\n" +
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
                Toast.makeText(ReservedProductList.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String s = txtDisplayResult.getText().toString() + num;
            txtDisplayResult.setText(s);
        }
        private void operation(String op){

            if(equalClicked){
                Toast.makeText(ReservedProductList.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
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
    private int getColor(String hex){
        int color = Integer.parseInt(hex, 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;

        return Color.rgb(r, g, b);
    }
}