package com.amine.bmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class ActivityStatistics extends AppCompatActivity {
    private double totalSell = 0.0, expProfit = 0.0, netProfit = 0.0, netLoss = 0.0, minorLoss = 0.0;
    private final DecimalFormat df =  new DecimalFormat("0.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        findViewById(R.id.statistics_progress).setVisibility(View.VISIBLE);
        setStatistics(() -> {
            findViewById(R.id.statistics_progress).setVisibility(View.GONE);
            initialize();
        });
    }

    private void initialize(){
        TextView txtTotalSell = findViewById(R.id.statistics_txtTotalSell),
                txtExpProfit = findViewById(R.id.statistics_txtExpProfit),
                txtNetProfit = findViewById(R.id.statistics_txtNetProfit),
                txtMinorLoss = findViewById(R.id.statistics_txtMinorLoss),
                txtNetLoss = findViewById(R.id.statistics_txtNetLoss);

        String s = "মোট বিক্রিঃ " + df.format(totalSell);
        txtTotalSell.setText(s);
        s = "প্রত্যাশিত লাভঃ " + df.format(expProfit);
        txtExpProfit.setText(s);
        s = "আসল লাভঃ " + df.format(netProfit);
        txtNetProfit.setText(s);
        s = "প্রত্যাশিত লাভের থেকে ক্ষতিঃ " + df.format(minorLoss);
        txtMinorLoss.setText(s);
        s = "আসোল ক্ষতিঃ " + df.format(netLoss);
        txtNetLoss.setText(s);
    }

    private interface StCallback{
        void onCallback();
    }

    private void setStatistics(StCallback callback){

        MainActivity.getRootPath().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("totalSell").exists())
                    totalSell = snapshot.child("totalSell").getValue(Double.class);
                if(snapshot.child("expectedProfit").exists())
                    expProfit = snapshot.child("expectedProfit").getValue(Double.class);
                if(snapshot.child("netProfit").exists())
                    netProfit = snapshot.child("netProfit").getValue(Double.class);
                if(snapshot.child("minorLoss").exists())
                    minorLoss = snapshot.child("minorLoss").getValue(Double.class);
                if(snapshot.child("netLoss").exists())
                    netLoss = snapshot.child("netLoss").getValue(Double.class);

                callback.onCallback();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}