package com.apelsinovaya.graphics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private GraphView graphTSRK4, graphVSRK4, graphTSRKM, graphVSRKM;
    private Button buttonHour, buttonDay, buttonWeek;
    private ProgressDialog progressDialog;
    private String uid;
    private int category, value;
    private Date dateTime;
    private ArrayList<Srk> srk4ArrayList, srkmArrayList;
    private ArrayList<Temperature> temperatureSRKArrayList;
    private ArrayList<Voltage> voltageSRKArrayList;
    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
    private final String patternDateFormat = "dd.MM HH:mm";
    private final String progressDialodMessage = "Downloading data from Database";
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DATABASE_URL = "***";
    static final String USERPASS = "***";
    String columnLabelUid = "Uid";
    String columnLabelCategory = "IndicatorNum";
    String columnLabelValue = "IndicatorVal";
    String columnLabelSt = "St";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFilterButtons();
        setGraphics();
        connectDBToday();
    }

    private void setFilterButtons() {
        buttonHour = (Button) findViewById(R.id.buttonHour);
        buttonHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDBHour();
            }
        });

        buttonDay = (Button) findViewById(R.id.buttonToday);
        buttonDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDBToday();
            }
        });

        buttonWeek = (Button) findViewById(R.id.buttonDayAgo);
        buttonWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDBDayAgo();
            }
        });
    }

    private void connectDBHour() {
        ConnectHour task = new ConnectHour();
        task.execute();
    }

    private void connectDBToday() {
        ConnectToday task = new ConnectToday();
        task.execute();
    }

    private void connectDBDayAgo() {
        ConnectDayAgo task = new ConnectDayAgo();
        task.execute();
    }

    private void setGraphics() {
        //TemperatureSRK4
        graphTSRK4 = (GraphView) findViewById(R.id.graphTSRK4);
        setGraphLabelTimeFormat(graphTSRK4);

        //VoltageSRK4
        graphVSRK4 = (GraphView) findViewById(R.id.graphVSRK4);
        setGraphLabelTimeFormat(graphVSRK4);

        //TemperatureSRKM
        graphTSRKM = (GraphView) findViewById(R.id.graphTSRKM);
        setGraphLabelTimeFormat(graphTSRKM);

        //VoltageSRKM
        graphVSRKM = (GraphView) findViewById(R.id.graphVSRKM);
        setGraphLabelTimeFormat(graphVSRKM);
    }

    private void setGraphLabelTimeFormat(GraphView graph) {
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) { // dateTimeFormatter.format(new Date((long) value));
                    Format formatter = new SimpleDateFormat(patternDateFormat);
                    return formatter.format(value);
                }
                return super.formatLabel(value, isValueX);
            }
        });
        graph.getGridLabelRenderer().setHumanRounding(true);
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(90);
    }

    private void retrievingDataAndDisplayOnGraphics(ResultSet resultSet) {
        srk4ArrayList = new ArrayList<>();
        srkmArrayList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                uid = resultSet.getString(columnLabelUid);
                category = Integer.parseInt(resultSet.getString(columnLabelCategory));
                value = Integer.parseInt(resultSet.getString(columnLabelValue));
                dateTime = resultSet.getTimestamp(columnLabelSt);
                sortByUid(uid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        displayDataOnGraphics(srk4ArrayList, graphTSRK4, graphVSRK4);
        displayDataOnGraphics(srkmArrayList, graphTSRKM, graphVSRKM);
    }

    private void sortByUid(String uid) {
        if (uid.equals("SRK4")) {
            srk4ArrayList.add(new Srk(uid, category, value, dateTime));
        } else {
            srkmArrayList.add(new Srk(uid, category, value, dateTime));
        }
    }

    private void displayDataOnGraphics(ArrayList<Srk> srkArrayList, GraphView graphTSRK, GraphView graphVSRK) {
        temperatureSRKArrayList = new ArrayList<>();
        voltageSRKArrayList = new ArrayList<>();
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
            displayGraphT(temperatureSRKArrayList, graphTSRK);
        } else {
            System.out.println("temperatureList is empty");
            displayEmptyGraph(graphTSRK);
        }
        if (!voltageSRKArrayList.isEmpty()) {
            displayGraphV(voltageSRKArrayList, graphVSRK);
        } else {
            System.out.println("voltageList is empty");
            displayEmptyGraph(graphVSRK);
        }
    }

    private void displayGraphT(ArrayList<Temperature> temperatureArrayList, GraphView graphTSRK) {
        LineGraphSeries<DataPoint> seriesTSRK = new LineGraphSeries<DataPoint>(setDataT(temperatureArrayList, temperatureArrayList.size()));
        seriesTSRK.setColor(Color.BLACK);
        graphTSRK.removeAllSeries();
        graphTSRK.onDataChanged(true, true);
        graphTSRK.addSeries(seriesTSRK);

        graphTSRK.getViewport().setMinY(10);
        graphTSRK.getViewport().setMaxY(30);
        graphTSRK.getViewport().setYAxisBoundsManual(true);
        graphTSRK.getViewport().setMinX(temperatureArrayList.get(0).getDateTime().getTime());
        graphTSRK.getViewport().setMaxX(temperatureArrayList.get(temperatureArrayList.size() - 1).getDateTime().getTime());
        graphTSRK.getViewport().setXAxisBoundsManual(true);
    }

    private void displayGraphV(ArrayList<Voltage> voltageArrayList, GraphView graphVSRK) {
        LineGraphSeries<DataPoint> seriesVSRK = new LineGraphSeries<DataPoint>(setDataV(voltageArrayList, voltageArrayList.size()));
        seriesVSRK.setColor(Color.BLUE);
        graphVSRK.removeAllSeries();
        graphVSRK.onDataChanged(true, true);
        graphVSRK.addSeries(seriesVSRK);

        graphVSRK.getViewport().setMinY(210);
        graphVSRK.getViewport().setMaxY(230);
        graphVSRK.getViewport().setYAxisBoundsManual(true);
        graphVSRK.getViewport().setMinX(voltageArrayList.get(0).getDateTime().getTime());
        graphVSRK.getViewport().setMaxX(voltageArrayList.get(voltageArrayList.size() - 1).getDateTime().getTime());
        graphVSRK.getViewport().setXAxisBoundsManual(true);
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

    private class ConnectToday extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
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

                Date date = Calendar.getInstance().getTime();
                java.sql.Date sqlDate = new java.sql.Date(date.getTime());

//              Creating a Statement object
                System.out.println("Creating statement...");

//              SQL request
                String sql = "SELECT * FROM Indicators WHERE St >= ? ORDER BY St";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, String.valueOf(sqlDate));

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

    private class ConnectDayAgo extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
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

                Date date = Calendar.getInstance().getTime();
                long dayAgo = date.getTime() - MILLIS_IN_A_DAY;
                java.sql.Date sqlDate = new java.sql.Date(dayAgo);

//              Creating a Statement object
                System.out.println("Creating statement...");

//              SQL request
                String sql = "SELECT * FROM Indicators WHERE St >= ? ORDER BY St";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, String.valueOf(sqlDate));

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

    private class ConnectHour extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
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

                Date date = Calendar.getInstance().getTime();
                java.sql.Date sqlDate = new java.sql.Date(date.getTime());

//              Creating a Statement object
                System.out.println("Creating statement...");

//              SQL request
                String sql = "SELECT * FROM Indicators WHERE St >= ? ORDER BY St";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, String.valueOf(sqlDate));

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