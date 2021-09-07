package com.apelsinovaya.graphics;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class RetrieveDataFromDB implements Runnable {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DATABASE_URL = "***";
    private static final String USERPASS = "***";
    private static final String COLUMNLABEL_UID = "Uid";
    private static final String COLUMNLABEL_CATEGORY = "IndicatorNum";
    private static final String COLUMNLABEL_VALUE = "IndicatorVal";
    private static final String COLUMNLABEL_ST = "St";
    private int category, value;
    private String uid;
    private Date dateTime;
    private java.sql.Date sqlDate;
    private ArrayList<Srk> srkArrayList;

    @Override
    public void run() {
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

            srkArrayList = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    uid = resultSet.getString(COLUMNLABEL_UID);
                    category = Integer.parseInt(resultSet.getString(COLUMNLABEL_CATEGORY));
                    value = Integer.parseInt(resultSet.getString(COLUMNLABEL_VALUE));
                    dateTime = resultSet.getTimestamp(COLUMNLABEL_ST);
                    srkArrayList.add(new Srk(uid,category,value,dateTime));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("Closing connection and releasing resources...");
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Srk> getSrkArrayList() {
        return srkArrayList;
    }

    public void setSqlDate(java.sql.Date sqlDate) {
        this.sqlDate = sqlDate;
    }
}