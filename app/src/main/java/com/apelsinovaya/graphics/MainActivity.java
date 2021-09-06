package com.apelsinovaya.graphics;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.sql.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {
    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
    private static final String PATTERN_DATEFORMAT = "dd.MM HH:mm";
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DATABASE_URL = "***";
    private static final String USERPASS = "***";
    private static final String COLUMNLABEL_UID = "Uid";
    private static final String COLUMNLABEL_CATEGORY = "IndicatorNum";
    private static final String COLUMNLABEL_VALUE = "IndicatorVal";
    private static final String COLUMNLABEL_ST = "St";
    private GraphView graphTSRK4, graphVSRK4, graphTSRKM, graphVSRKM;
    private ProgressDialog progressDialog;
    private int category, value;
    private Date dateTime;
    private java.sql.Date sqlDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setGraphics();
        setFilterButtons();
        setDataForToday();
    }

    private void setFilterButtons() {
        Button buttonCalendar = (Button) findViewById(R.id.buttonCalendar);
        buttonCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDataForCalendarDay();
            }
        });
        Button buttonDay = (Button) findViewById(R.id.buttonToday);
        buttonDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDataForToday();
            }
        });
        Button buttonDayAgo = (Button) findViewById(R.id.buttonDayAgo);
        buttonDayAgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDataForDayAgo();
            }
        });
    }

    private void setDataForToday() {
        Date today = Calendar.getInstance().getTime();
        sqlDate = new java.sql.Date(today.getTime());
        connectToDB();
    }

    private void setDataForDayAgo() {
        Date today = Calendar.getInstance().getTime();
        long dayAgo = today.getTime() - MILLIS_IN_A_DAY;
        sqlDate = new java.sql.Date(dayAgo);
        connectToDB();
    }

    private void setDataForCalendarDay() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                sqlDate = new java.sql.Date(calendar.getTime().getTime());
                connectToDB();
            }
        };
        showWigetCalendar(calendar, datePickerListener);
    }

    private void showWigetCalendar(Calendar calendar, DatePickerDialog.OnDateSetListener datePickerListener) {
        new DatePickerDialog(this, datePickerListener, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void connectToDB() {
        ConnectToDB task = new ConnectToDB();
        task.execute();
    }

    private void retrievingDataAndDisplayOnGraphics(ResultSet resultSet) {
        ArrayList<Srk> srk4ArrayList = new ArrayList<>();
        ArrayList<Srk> srkmArrayList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                String uid = resultSet.getString(COLUMNLABEL_UID);
                category = Integer.parseInt(resultSet.getString(COLUMNLABEL_CATEGORY));
                value = Integer.parseInt(resultSet.getString(COLUMNLABEL_VALUE));
                dateTime = resultSet.getTimestamp(COLUMNLABEL_ST);
                sortByUid(uid, srk4ArrayList, srkmArrayList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        displayDataOnGraphics(srk4ArrayList, graphTSRK4, graphVSRK4);
        displayDataOnGraphics(srkmArrayList, graphTSRKM, graphVSRKM);
    }

    private void sortByUid(String uid, ArrayList<Srk> list1, ArrayList<Srk> list2) {
        if (uid.equals("SRK4")) {
            list1.add(new Srk(uid, category, value, dateTime));
        } else {
            list2.add(new Srk(uid, category, value, dateTime));
        }
    }

    private void displayDataOnGraphics(ArrayList<Srk> srkArrayList, GraphView graphTSRK, GraphView graphVSRK) {
        ArrayList<Temperature> temperatureSRKArrayList = new ArrayList<>();
        ArrayList<Voltage> voltageSRKArrayList = new ArrayList<>();
        if (!srkArrayList.isEmpty()) {
            for (Srk srk : srkArrayList) {
                if (srk.getCategory() == 0) {
                    temperatureSRKArrayList.add(new Temperature(srk.getValue(), srk.getDateTime()));
                } else {
                    voltageSRKArrayList.add(new Voltage(srk.getValue(), srk.getDateTime()));
                }
            }
        } else {
            System.out.println("srkArrayList is Empty");
        }
        if (!temperatureSRKArrayList.isEmpty()) {
            addSeriesOnGraphTSRK(temperatureSRKArrayList, graphTSRK);
        } else {
            System.out.println("temperatureList is empty");
            displayEmptyGraph(graphTSRK);
        }
        if (!voltageSRKArrayList.isEmpty()) {
            addSeriesOnGraphVSRK(voltageSRKArrayList, graphVSRK);
        } else {
            System.out.println("voltageList is empty");
            displayEmptyGraph(graphVSRK);
        }
    }

    private void setGraphics() {
        //TemperatureSRK4
        graphTSRK4 = (GraphView) findViewById(R.id.graphTSRK4);
        setGraphLabelFormat(graphTSRK4);
        //VoltageSRK4
        graphVSRK4 = (GraphView) findViewById(R.id.graphVSRK4);
        setGraphLabelFormat(graphVSRK4);
        //TemperatureSRKM
        graphTSRKM = (GraphView) findViewById(R.id.graphTSRKM);
        setGraphLabelFormat(graphTSRKM);
        //VoltageSRKM
        graphVSRKM = (GraphView) findViewById(R.id.graphVSRKM);
        setGraphLabelFormat(graphVSRKM);
    }

    private void setGraphLabelFormat(GraphView graph) {
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) { // dateTimeFormatter.format(new Date((long) value));
                    Format formatter = new SimpleDateFormat(PATTERN_DATEFORMAT);
                    return formatter.format(new Date((long) value));
                }
                return super.formatLabel(value, isValueX);
            }
        });
        graph.getGridLabelRenderer().setHumanRounding(true);
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(90);

    }

    private void addSeriesOnGraphTSRK(ArrayList<Temperature> temperatureArrayList, GraphView graph) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(setDataT(temperatureArrayList, temperatureArrayList.size()));
        series.setColor(Color.BLACK);
        graph.getViewport().setMinY(10);
        graph.getViewport().setMaxY(30);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(temperatureArrayList.get(0).getDateTime().getTime());
        graph.getViewport().setMaxX(temperatureArrayList.get(temperatureArrayList.size()-1).getDateTime().getTime());
        graph.getGridLabelRenderer().setNumHorizontalLabels(7);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.removeAllSeries();
        graph.addSeries(series);
        graph.onDataChanged(true, true);
    }

    private void addSeriesOnGraphVSRK(ArrayList<Voltage> voltageArrayList, GraphView graph) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(setDataV(voltageArrayList, voltageArrayList.size()));
        series.setColor(Color.BLUE);
        graph.getViewport().setMinY(210);
        graph.getViewport().setMaxY(230);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(voltageArrayList.get(0).getDateTime().getTime());
        graph.getViewport().setMaxX(voltageArrayList.get(voltageArrayList.size()-1).getDateTime().getTime());
        graph.getGridLabelRenderer().setNumHorizontalLabels(7);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.removeAllSeries();
        graph.addSeries(series);
        graph.onDataChanged(true, true);
    }

    private void displayEmptyGraph(GraphView graph) {
        graph.removeAllSeries();
        graph.onDataChanged(true, true);
    }

    private DataPoint[] setDataT(List<Temperature> temperatureArrayList, int size) {
        DataPoint[] values = new DataPoint[size];     //creating an object of type DataPoint[] of size 'n'
        for (int i = 0; i < size; i++) {
            DataPoint v = new DataPoint(temperatureArrayList.get(i).getDateTime(), temperatureArrayList.get(i).getValue());
            values[i] = v;
        }
        return values;
    }

    private DataPoint[] setDataV(ArrayList<Voltage> voltageArrayList, int size) {
        DataPoint[] values = new DataPoint[size];     //creating an object of type DataPoint[] of size 'n'
        for (int i = 0; i < size; i++) {
            DataPoint v = new DataPoint(voltageArrayList.get(i).getDateTime(), voltageArrayList.get(i).getValue());
            values[i] = v;
        }
        return values;
    }

    private class ConnectToDB extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            String progressDialodMessage = "Downloading data from Database";
            progressDialog.setMessage(progressDialodMessage);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "response";
            Connection connection = null;
            PreparedStatement preparedStatement = null;
//              Registering JDBC driver
            try {
                System.out.println("Registering JDBC driver...");
                Class.forName(JDBC_DRIVER);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
//              Connecting to database
                System.out.println("Connecting to database...");
                connection = DriverManager.getConnection(DATABASE_URL + USERPASS);

                String date1 = sqlDate + " " + "00:00:00";
                String date2 = sqlDate + " " + "23:59:59";
                System.out.println("sqlDate..." + sqlDate);
//              Creating a Statement object
                System.out.println("Creating statement...");
//              SQL request
                String sql = "SELECT * FROM Indicators WHERE St BETWEEN ? AND ? ORDER BY St";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, date1);
                preparedStatement.setString(2, date2);
//              Retrieving data
                System.out.println("Retrieving data...");
                ResultSet resultSet = preparedStatement.executeQuery();

                retrievingDataAndDisplayOnGraphics(resultSet);

                System.out.println("Closing connection and releasing resources...");
                resultSet.close();
                preparedStatement.close();
                connection.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }
    }
}