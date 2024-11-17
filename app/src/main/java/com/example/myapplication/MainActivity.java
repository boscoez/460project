//import package
package com.example.myapplication;

//import statements
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    // Define the variable of CalendarView type
    // and TextView type;
    //
    CalendarView calendarView;
    TextView date_view;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // By ID we can use each component
        // which id is assign in xml file
        // use findViewById() to get the
        // CalendarView and TextView
        calendarView = (CalendarView)
                findViewById(R.id.calendarView);
        date_view = (TextView)
                findViewById(R.id.date_view);

        // Add Listener in calendar
        calendarView
                .setOnDateChangeListener(
                        new CalendarView
                                .OnDateChangeListener() {
                            @Override

                            // In this Listener have one method
                            // and in this method we will
                            // get the value of DAYS, MONTH, YEARS
                            public void onSelectedDayChange(
                                    @NonNull CalendarView view,
                                    int year,
                                    int month,
                                    int dayOfMonth)
                            {

                                // Store the value of date with
                                // format in String type Variable
                                // Add 1 in month because month
                                // index is start with 0
                                String Date
                                        = dayOfMonth + "-"
                                        + (month + 1) + "-" + year;

                                // set this date in TextView for Display
                                date_view.setText(Date);
                            }
                        });

    }
}
