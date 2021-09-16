package com.amine.bmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Scanner;

public class ShopSelection extends AppCompatActivity implements View.OnClickListener {

    private RadioButton selection_islam_porda_bitan, selection_islam_it_communication,
            selection_add_master_plaza;
    private String selectedName = "";
    private boolean isSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_selection);
        initialize();

    }

    private void initialize(){

        selection_islam_porda_bitan = findViewById(R.id.selection_islam_porda_bitan);
        selection_islam_it_communication = findViewById(R.id.selection_islam_it_communication);
        selection_add_master_plaza = findViewById(R.id.selection_add_master_plaza);

        selection_islam_porda_bitan.setOnClickListener(this);
        selection_islam_it_communication.setOnClickListener(this);
        selection_add_master_plaza.setOnClickListener(this);

        findViewById(R.id.selection_btnSave).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.selection_islam_porda_bitan){
            selection_islam_porda_bitan.setChecked(true);
            selection_islam_it_communication.setChecked(false);
            selection_add_master_plaza.setChecked(false);
            selectedName = selection_islam_porda_bitan.getText().toString();
            isSelected = true;
        }
        else if(id == R.id.selection_islam_it_communication){
            selection_islam_porda_bitan.setChecked(false);
            selection_islam_it_communication.setChecked(true);
            selection_add_master_plaza.setChecked(false);
            selectedName = selection_islam_it_communication.getText().toString();
            isSelected = true;
        }
        else if(id == R.id.selection_add_master_plaza){
            selection_islam_porda_bitan.setChecked(false);
            selection_islam_it_communication.setChecked(false);
            selection_add_master_plaza.setChecked(true);
            selectedName = selection_add_master_plaza.getText().toString();
            isSelected = true;
        }
        else if(id == R.id.selection_btnSave){
            if(isSelected){
                File selectedShop = MainActivity.getSelectedShop();
                try {
                    PrintWriter pr = new PrintWriter(selectedShop);
                    pr.println(selectedName);
                    pr.close();

                    Intent intent = new Intent(ShopSelection.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }catch (Exception e){
                    Toast.makeText(ShopSelection.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(ShopSelection.this, "Select a Shop", Toast.LENGTH_LONG).show();
            }
        }
    }
}