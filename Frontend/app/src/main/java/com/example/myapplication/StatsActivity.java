package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatsActivity extends AppCompatActivity {
    String username;

    String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        username = getIntent().getStringExtra("user");
        ip = getIntent().getStringExtra("ip");
        HashMap<String, Double> stats = (HashMap<String, Double>) getIntent().getSerializableExtra("result");
        System.out.println(stats);


        double dist_user = (double) stats.get("dist_user") / 1000;
        double dist_avg = (double)stats.get("dist_avg") / 1000;

        dist_user = Double.parseDouble(String.format("%.2f", dist_user));
        dist_avg = Double.parseDouble(String.format("%.2f", dist_avg));

        double sec_user = (double)stats.get("sec_user") / 60;
        double sec_avg = (double)stats.get("sec_avg") / 60;

        sec_user =  Double.parseDouble(String.format("%.2f", sec_user));
        sec_avg= Double.parseDouble(String.format("%.2f", sec_avg));

        double ele = (double)stats.get("ele_user");
        double totele =(double) stats.get("ele_avg");

        Button backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the ActivitySelectUser
                Intent main = new Intent(StatsActivity.this, MainActivity.class);
                main.putExtra("user", username);
                main.putExtra("ip", ip);
                startActivity(main);
                finish();
            }
        });

        // Find the chart views
        HorizontalBarChart char1 = (HorizontalBarChart) findViewById(R.id.chart1);
        HorizontalBarChart char2 = findViewById(R.id.chart2);
        HorizontalBarChart char3 = findViewById(R.id.chart3);

        // Set the data for each chart
        int num=1;
        setChartData(char1, new float[]{(float) dist_user}, new float[]{(float) dist_avg}, new String[]{"Personal", "Total"}, "km", "km");
        num++;
        setChartData(char2, new float[]{(float) sec_user}, new float[]{(float) sec_avg}, new String[]{"Personal", "Total"},"min", "min");
        num++;
        setChartData(char3, new float[]{(float)(ele)}, new float[]{(float)(totele)}, new String[]{"Personal", "Total"}, "m", "m");
    }


    private void setChartData(HorizontalBarChart chart, float[] values1, float[] values2, String[] labels, String text1, String text2) {
        ArrayList<BarEntry> entries1 = new ArrayList<>();
        ArrayList<BarEntry> entries2 = new ArrayList<>();

        entries1.add(new BarEntry(0, values1));
        entries2.add(new BarEntry(1, values2));

        BarDataSet set1 = new BarDataSet(entries1, "Bar 1");
        BarDataSet set2 = new BarDataSet(entries2, "Bar 2");

        // Set colors for each BarDataSet separately

        set1.setColor(getResources().getColor(R.color.green1));
        set2.setColor(getResources().getColor(R.color.green2));

        // Set value text size and enable drawing of value text
        set1.setValueTextSize(12f);
        set1.setDrawValues(true);
        set2.setValueTextSize(12f);
        set2.setDrawValues(true);

        // Set bold typeface for value text
        Typeface tf = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD);
        set1.setValueTypeface(tf);
        set2.setValueTypeface(tf);

        // Set custom value formatter to add text next to value text
        set1.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f %s", value, text1);
            }
        });
        set2.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f %s", value, text2);
            }
        });

        // Adjust spacing between bars
        float barWidth = 0.4f;
        float groupSpace = 0.05f;
        float barSpace = 0.05f;
        BarData data = new BarData(set1, set2);
        data.setBarWidth(barWidth);
        data.groupBars(0f, groupSpace, barSpace);

        chart.setData(data);
        chart.invalidate();

        // Remove grid lines and axis labels
        chart.setDrawGridBackground(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setDrawLabels(false);

        // Customize chart appearance and behavior as needed
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setAxisMinimum(0f);

        // Show value text above the bar
        chart.setDrawValueAboveBar(true);

        // Add legend
        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        legend.setFormSize(10f);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setXEntrySpace(5f);
        legend.setYEntrySpace(5f);
        legend.setCustom(getLegendEntries(new int[]{getResources().getColor(R.color.green1), getResources().getColor(R.color.green2)}, labels));
    }




    private List<LegendEntry> getLegendEntries(int[] colors, String[] labels) {
        List<LegendEntry> entries = new ArrayList<>();
        for (int i = 0; i < colors.length; i++) {
            LegendEntry entry = new LegendEntry();
            entry.formColor = colors[i];
            entry.label = labels[i];
            entries.add(entry);
        }
        return entries;
    }

}
