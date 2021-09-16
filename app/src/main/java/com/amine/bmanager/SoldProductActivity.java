package com.amine.bmanager;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

public class SoldProductActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        AdapterView.OnItemSelectedListener {

    private static ArrayList<SoldProduct> soldProducts;
    private ArrayList<CategorizedSearched> searched, cNameWiseSearch, pNameWiseSearch, sDateWiseSearch,
            pCodeWiseSearch, compNameWiseSearch;
    private static ArrayList<String> customerNames, dates, productNames, productCodes, companyNames;
    private Spinner spinner;
    private SearchView search;
    private ListView sold_lstSrcResult, sold_lstSrcResult1;
    private TextView txtSelectedCategory, txtTotalDue;
    private final String[] spinItems =
            {"Customer Name", "Selling Date", "Product Name", "Product Code", "Company Name", "Due"};
    private String selectedCategory;
    private final DecimalFormat df =  new DecimalFormat("0.#");
    private BaseAdapter soldAdapter, categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sold_product);
        initialize();
    }

    private void initialize(){
        findViewById(R.id.sold_progress).setVisibility(View.VISIBLE);
        searched = new ArrayList<>();
        customerNames = new ArrayList<>();
        dates = new ArrayList<>();
        productNames = new ArrayList<>();
        productCodes = new ArrayList<>();
        companyNames = new ArrayList<>();
        soldProducts = new ArrayList<>();
        cNameWiseSearch = new ArrayList<>();
        sDateWiseSearch = new ArrayList<>();
        pNameWiseSearch = new ArrayList<>();
        pCodeWiseSearch = new ArrayList<>();
        compNameWiseSearch = new ArrayList<>();
        txtSelectedCategory = findViewById(R.id.sold_selectedCategory);
        sold_lstSrcResult = findViewById(R.id.sold_lstSrcResult);
        txtTotalDue = findViewById(R.id.sold_totalDue);
        sold_lstSrcResult1 = findViewById(R.id.sold_lstSrcResult1);

        readSellHistory(() -> {
            getThreadForCategorizedProducts().start();
        });

        spinner = findViewById(R.id.sold_spinner);
        search = findViewById(R.id.sold_search);
        spinner.setOnItemSelectedListener(this);
        search.setOnQueryTextListener(this);
    }

    private Thread getThreadForCategorizedProducts(){
        final Handler handler = new Handler(getApplicationContext().getMainLooper());

        return new Thread(() -> handler.post(() -> {
            setCNameWiseSearch();
            setSDateWiseSearch();
            setPNameWiseSearch();
            setPCodeWiseSearch();
            setCompNameWiseSearch();
            findViewById(R.id.sold_progress).setVisibility(View.GONE);
            setSpinner();

            search.setVisibility(View.VISIBLE);
            sold_lstSrcResult.setVisibility(View.GONE);
        }));
    }

    private void setCNameWiseSearch(){
        for(int i = 0; i < customerNames.size(); i++){
            double payable = 0, paid = 0, due = 0;
            String pho = "", cName1 = customerNames.get(i);
            for (int j = 0; j < soldProducts.size(); j++) {
                String cName2 = soldProducts.get(j).getBuyerName();

                if (cName1.equals(cName2)) {
                    pho = soldProducts.get(j).getPhone();
                    payable += Double.parseDouble(soldProducts.get(j).getPayableAmount());
                    paid += Double.parseDouble(soldProducts.get(j).getPaid());
                    due += Double.parseDouble(soldProducts.get(j).getDue());
                }
            }
            CategorizedSearched s = new CategorizedSearched(cName1, payable + "",
                    paid + "", due + "", pho);

            cNameWiseSearch.add(s);
        }
        //print(cNameWiseSearch);
    }
    private void setSDateWiseSearch(){
        for(int i = 0; i < dates.size(); i++){
            String cName1 = dates.get(i);
            double payable = 0, paid = 0, due = 0;
            for (int j = 0; j < soldProducts.size(); j++) {
                String cName2 = soldProducts.get(j).getSellingDate();
                if (cName1.equals(cName2)) {
                    payable += Double.parseDouble(soldProducts.get(j).getPayableAmount());
                    paid += Double.parseDouble(soldProducts.get(j).getPaid());
                    due += Double.parseDouble(soldProducts.get(j).getDue());
                }
            }
            CategorizedSearched s = new CategorizedSearched(cName1,
                    payable + "", paid + "",
                    due + "", "N/A");
            sDateWiseSearch.add(s);

        }
    }
    private void setPNameWiseSearch(){
        for(int i = 0; i < productNames.size(); i++){
            double payable = 0, paid = 0, due = 0;
            for (int j = 0; j < soldProducts.size(); j++) {
                if (productNames.get(i).equals(soldProducts.get(j).getProductName())) {
                    payable += Double.parseDouble(soldProducts.get(j).getPayableAmount());
                    paid += Double.parseDouble(soldProducts.get(j).getPaid());
                    due += Double.parseDouble(soldProducts.get(j).getDue());
                }
            }
            CategorizedSearched s = new CategorizedSearched(productNames.get(i),
                    payable + "", paid + "",
                    due + "","N/A");
            pNameWiseSearch.add(s);
        }
    }
    private void setPCodeWiseSearch(){
        for(int i = 0; i < productCodes.size(); i++){
            double payable = 0, paid = 0, due = 0;
            for (int j = 0; j < soldProducts.size(); j++) {
                if (productCodes.get(i).equals(soldProducts.get(j).getProductCode())) {

                    payable += Double.parseDouble(soldProducts.get(j).getPayableAmount());
                    paid += Double.parseDouble(soldProducts.get(j).getPaid());
                    due += Double.parseDouble(soldProducts.get(j).getDue());
                }
            }
            CategorizedSearched s = new CategorizedSearched(productCodes.get(i),
                    payable + "", paid + "",
                    due + "", "N/A");
            pCodeWiseSearch.add(s);
        }
    }
    private void setCompNameWiseSearch(){
        for(int i = 0; i < companyNames.size(); i++){
            double payable = 0, paid = 0, due = 0;
            for (int j = 0; j < soldProducts.size(); j++) {
                if (companyNames.get(i).equals(soldProducts.get(j).getCompanyName())) {

                    payable += Double.parseDouble(soldProducts.get(j).getPayableAmount());
                    paid += Double.parseDouble(soldProducts.get(j).getPaid());
                    due += Double.parseDouble(soldProducts.get(j).getDue());
                }
            }
            CategorizedSearched s = new CategorizedSearched(companyNames.get(i),
                    payable + "", paid + "",
                    due + "", "N/A");
            compNameWiseSearch.add(s);
        }
    }

    private void initializeSoldAdapter(ArrayList<CategorizedSearched> list){

        findViewById(R.id.sold_lstSrcResult).setVisibility(View.GONE);
        findViewById(R.id.sold_lstSrcResult1).setVisibility(View.VISIBLE);

        soldAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int position) {
                return list.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {

                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.sold_history_list_items, null);

                TextView cat = view.findViewById(R.id.sold_history_categoryName),
                        payable = view.findViewById(R.id.sold_history_totalPayableAmount),
                        paid = view.findViewById(R.id.sold_history_paid),
                        due = view.findViewById(R.id.sold_history_due),
                        phone = view.findViewById(R.id.sold_history_phone);

                Button button = view.findViewById(R.id.sold_history_btnDetails);
                String s;
                s = getCategoryName() + list.get(position).getCategoryName();
                cat.setText(s);
                s = "মোট টাকা: " + df.format(Double.parseDouble(list.get(position).getTotalPayableAmount()));
                payable.setText(s);
                s = "মোট পরিশোধ: " + df.format(Double.parseDouble(list.get(position).getPaid()));
                paid.setText(s);
                s = "মোট বাকী: " + df.format(Double.parseDouble(list.get(position).getDue()));
                due.setText(s);
                s = "Mobile: " + list.get(position).getPhone();
                phone.setText(s);

                if(selectedCategory.equals("Customer Name")) button.setVisibility(View.VISIBLE);
                else button.setVisibility(View.GONE);

                button.setOnClickListener(v -> {
                    Intent intent = new Intent(SoldProductActivity.this, CategorizedDetails.class);
                    intent.putExtra("PERSON_NAME", list.get(position).getCategoryName());
                    startActivity(intent);

                });

                return view;
            }
        };
        sold_lstSrcResult1.setAdapter(soldAdapter);
        setTotalDue(list);
    }
    private void setTotalDue(ArrayList<CategorizedSearched> list){
        double totalDue = 0.0;
        for(int i = 0; i < list.size(); i++) totalDue += Double.parseDouble(list.get(i).getDue());

        findViewById(R.id.sold_totalDueLayout).setVisibility(View.VISIBLE);
        String s = "মোট বাকিঃ " + totalDue;
        txtTotalDue.setText(s);
    }

    private void initializeCategorizedAdapters(){
        categoryAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return searched.size();
            }

            @Override
            public Object getItem(int position) {
                return searched.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.sold_history_list_items, null);

                TextView cat = view.findViewById(R.id.sold_history_categoryName),
                        payable = view.findViewById(R.id.sold_history_totalPayableAmount),
                        paid = view.findViewById(R.id.sold_history_paid),
                        due = view.findViewById(R.id.sold_history_due),
                        phone = view.findViewById(R.id.sold_history_phone);

                Button button = view.findViewById(R.id.sold_history_btnDetails);
                String s;
                s = getCategoryName() + searched.get(position).getCategoryName();
                cat.setText(s);
                s = "মোট টাকা: " + searched.get(position).getTotalPayableAmount();
                payable.setText(s);
                s = "মোট পরিশোধ: " + searched.get(position).getPaid();
                paid.setText(s);
                s = "মোট বাকী: " + searched.get(position).getDue();
                due.setText(s);
                s = "Mobile: " + searched.get(position).getPhone();
                phone.setText(s);

                if(selectedCategory.equals("Customer Name")) button.setVisibility(View.VISIBLE);
                else button.setVisibility(View.GONE);

                button.setOnClickListener(v -> {
                    Intent intent = new Intent(SoldProductActivity.this, CategorizedDetails.class);
                    intent.putExtra("PERSON_NAME", searched.get(position).getCategoryName());
                    startActivity(intent);

                });

                return view;
            }
        };
        sold_lstSrcResult.setAdapter(categoryAdapter);
    }

    private String getCategoryName(){

        String s = "";
        switch (selectedCategory) {
            case "Due":
            case "Customer Name":
                s = "Name: ";
                break;
            case "Selling Date":
                s = "Date: ";
                break;
            case "Product Name":
                s = "Product Name: ";
                break;
            case "Product Code":
                s = "Code: ";
                break;
            default:
                s = "Company: ";
                break;
        }

        return s;
    }

    private void setSpinner(){
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinItems);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

    }

    public interface ReadSellHistory{
        void onCallback();
    }


    private void readSellHistory(ReadSellHistory read){

        customerNames = new ArrayList<>();
        dates = new ArrayList<>();
        productNames = new ArrayList<>();
        productCodes = new ArrayList<>();
        companyNames = new ArrayList<>();

        MainActivity.getSold().addListenerForSingleValueEvent(new ValueEventListener() {
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

                                        if(!customerNames.contains(p.getBuyerName()))
                                            customerNames.add(p.getBuyerName());
                                        if(!dates.contains(p.getSellingDate()))
                                            dates.add(p.getSellingDate());
                                        if(!productNames.contains(p.getProductName()))
                                            productNames.add(p.getProductName());
                                        if(!productCodes.contains(p.getProductCode()))
                                            productCodes.add(p.getProductCode());
                                        if(!companyNames.contains(p.getCompanyName()))
                                            companyNames.add(p.getCompanyName());


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
    public boolean onQueryTextSubmit(String query) {
        findViewById(R.id.sold_progress).setVisibility(View.VISIBLE);
        findViewById(R.id.sold_lstSrcResult1).setVisibility(View.GONE);
        findViewById(R.id.sold_lstSrcResult).setVisibility(View.VISIBLE);
        findViewById(R.id.sold_totalDueLayout).setVisibility(View.GONE);

        if(query.length() == 0){
            findViewById(R.id.sold_progress).setVisibility(View.GONE);
            initializeSoldAdapters();
            return false;
        }
        searched.clear();
        switch (selectedCategory) {
            case "Customer Name":

                for(int i = 0; i < cNameWiseSearch.size(); i++){
                    String value = cNameWiseSearch.get(i).getCategoryName();
                    if(isMatched(query, value)){
                        searched.add(cNameWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                break;

            case "Selling Date":

                for(int i = 0; i < sDateWiseSearch.size(); i++){
                    String val = sDateWiseSearch.get(i).getCategoryName();
                    if(isMatched(query, val)){
                        searched.add(sDateWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                break;


            case "Product Name":

                for(int i = 0; i < pNameWiseSearch.size(); i++){
                    String val = pNameWiseSearch.get(i).getCategoryName();
                    if(isMatched(query, val)){
                        searched.add(pNameWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();

                break;


            case "Product Code":

                for(int i = 0; i < pCodeWiseSearch.size(); i++){
                    String val = pCodeWiseSearch.get(i).getCategoryName();
                    if(isMatched(query, val)){
                        searched.add(pCodeWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();

                break;

            case "Company Name":

                for(int i = 0; i < compNameWiseSearch.size(); i++){
                    String val = compNameWiseSearch.get(i).getCategoryName();
                    if(isMatched(query, val)){
                        searched.add(compNameWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();

                break;
        }
        findViewById(R.id.sold_progress).setVisibility(View.GONE);


        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        findViewById(R.id.sold_progress).setVisibility(View.VISIBLE);
        findViewById(R.id.sold_lstSrcResult1).setVisibility(View.GONE);
        findViewById(R.id.sold_lstSrcResult).setVisibility(View.VISIBLE);
        findViewById(R.id.sold_totalDueLayout).setVisibility(View.GONE);

        if(newText.length() == 0){
            findViewById(R.id.sold_progress).setVisibility(View.GONE);
            initializeSoldAdapters();
            return false;
        }
        Log.i("test", "Category name: " + selectedCategory);
        searched.clear();
        switch (selectedCategory) {
            case "Customer Name":
                for(int i = 0; i < cNameWiseSearch.size(); i++){
                    String value = cNameWiseSearch.get(i).getCategoryName();
                    if(isMatched(newText, value)){
                        searched.add(cNameWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                break;

            case "Selling Date":

                for(int i = 0; i < sDateWiseSearch.size(); i++){
                    String val = sDateWiseSearch.get(i).getCategoryName();
                    if(isMatched(newText, val)){
                        searched.add(sDateWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                break;


            case "Product Name":

                for(int i = 0; i < pNameWiseSearch.size(); i++){
                    String val = pNameWiseSearch.get(i).getCategoryName();
                    if(isMatched(newText, val)){
                        searched.add(pNameWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();

                break;


            case "Product Code":

                for(int i = 0; i < pCodeWiseSearch.size(); i++){
                    String val = pCodeWiseSearch.get(i).getCategoryName();
                    if(isMatched(newText, val)){
                        searched.add(pCodeWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();

                break;

            case "Company Name":

                for(int i = 0; i < compNameWiseSearch.size(); i++){
                    String val = compNameWiseSearch.get(i).getCategoryName();
                    if(isMatched(newText, val)){
                        searched.add(compNameWiseSearch.get(i));
                    }
                }
                categoryAdapter.notifyDataSetChanged();

                break;
        }
        findViewById(R.id.sold_progress).setVisibility(View.GONE);

        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String s = spinItems[position];
        selectedCategory = s;
        initializeCategorizedAdapters();
        initializeSoldAdapters();

        if(s.equals("Due")){
            searched.clear();
            search.setVisibility(View.INVISIBLE);

            findViewById(R.id.sold_lstSrcResult).setVisibility(View.VISIBLE);
            findViewById(R.id.sold_lstSrcResult1).setVisibility(View.GONE);

            for (int i = 0; i < cNameWiseSearch.size(); i++) {
                double d = Double.parseDouble(cNameWiseSearch.get(i).getDue());
                if (d > 0.0) searched.add(cNameWiseSearch.get(i));
            }
            categoryAdapter.notifyDataSetChanged();
        }
    }

    private void initializeSoldAdapters(){
        switch (selectedCategory) {
            case "Customer Name":
                search.setVisibility(View.VISIBLE);
                initializeSoldAdapter(cNameWiseSearch);
                break;
            case "Selling Date":
                search.setVisibility(View.VISIBLE);
                initializeSoldAdapter(sDateWiseSearch);
                break;
            case "Product Name":
                search.setVisibility(View.VISIBLE);
                initializeSoldAdapter(pNameWiseSearch);
                break;
            case "Product Code":
                search.setVisibility(View.VISIBLE);
                initializeSoldAdapter(pCodeWiseSearch);
                break;
            case "Company Name":
                search.setVisibility(View.VISIBLE);
                initializeSoldAdapter(compNameWiseSearch);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static ArrayList<SoldProduct> getSoldProducts(){
        return soldProducts;
    }
}