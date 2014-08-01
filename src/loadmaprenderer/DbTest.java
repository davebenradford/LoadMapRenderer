package loadmaprenderer;

import java.sql.*;

public class DbTest {
    
    public static void main(String args[]) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            System.out.println("Opened database successfully");
            
            stmt = c.createStatement();
            String sql = "CREATE TABLE COMPANY " + 
                    "(ID INT PRIMARY KEY    NOT NULL," +
                    "NAME           TEXT    NOT NULL," +
                    "AGE            INT     NOT NULL," +
                    "ADDRESS        CHAR(50)," +
                    "SALARY         REAL)";
            
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
            
        } catch ( ClassNotFoundException | SQLException e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }
    public static void ProjectBuilder() {
        /**
        {"SpatialFolder":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\\\data\\Spatial",
        "SWATFolder":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\\\data\\txtinout",
        "FileName":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\STC.wbprj",
        "Watersehd":"STC","Path":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\",
        "Spatial":{"ShapefileNameBoundary":"boundary",
        "ShapefileNameStream":"stream",
        "ShapefileNameSubbasin":"subbasin",
        "ShapefileNameFarm":"farm2010",
        "ShapefileNameField":"land2010_by_land_id",
        "ShapefileNameSmallDam":"small_dam",
        "ShapefileNameHoldingPond":"cattle_yard",
        "ShapefileNameGrazing":"grazing",
        *                                                                                                    *  "SpatialFolder":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\data\\Spatial\\",
        "SpatialDatabase":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\data\\Spatial\\spatial.db3"},

        "SWATInput":{"FigFile":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\data\\txtinout\\fig.fig",
        *                                                                                                    *  "SWATFolder":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\data\\txtinout\\",
        "StartYear":1991,"EndYear":2010},

        "Scenarios":[{"BMPScenerioBaseType":0,"TillageType":2,
        "ProjectFolder":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\",
        "ID":-1,
        "Description":"Historical Base Scenario",
        "CreateTime":"2014-07-21T15:04:37.1726382-04:00",
        "LastModifiedTime":"2014-07-21T15:04:37.1726382-04:00",
        "LastSWATRunTime":"0001-01-01T00:00:00",
        "Name":"Historical Base Scenario",
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3",
        "Type":1,
        "CropBMPLevel":0,
        "Result":{"SWATResult":{"HasResult":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3"},
        "EconomicResult":{"HasResult":true,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3"},
        "HasResult":false,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3"},
        "Design":{"HasSmallDam":true,
        "HasHoldingPond":true,
        "HasGrazing":true,
        "HasTillage":false,
        "HasForageConversion":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3"}
        },

        {"BMPScenerioBaseType":1,"TillageType":2,"ProjectFolder":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\",
        "ID":-2,
        "Description":"Conventional Base Scenario",
        "CreateTime":"2014-07-21T15:04:42.4641673-04:00",
        "LastModifiedTime":"2014-07-21T15:04:42.4641673-04:00",
        "LastSWATRunTime":"0001-01-01T00:00:00",
        "Name":"Conventional Base Scenario",
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3",
        "Type":2,
        "CropBMPLevel":0,
        "Result":{"SWATResult":{"HasResult":false,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3"},
        "EconomicResult":{"HasResult":true,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3"},
        "HasResult":false,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3"},
        "Design":{"HasSmallDam":false,
        "HasHoldingPond":false,
        "HasGrazing":false,
        "HasTillage":false,
        "HasForageConversion":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3"}},

        {"BMPScenerioBaseType":0,
        "TillageType":2,
        "ProjectFolder":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\",
        "ID":1,
        "Description":"Scenario1",
        "CreateTime":"2014-07-21T15:04:51.7971005-04:00",
        "LastModifiedTime":"2014-07-21T15:04:51.7971005-04:00",
        "LastSWATRunTime":"0001-01-01T00:00:00","Name":"Scenario1",
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Scenario1\\WEBs_Scenario_1.db3",
        "Type":0,
        "CropBMPLevel":0,
        "Result":{"SWATResult":{"HasResult":false,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Scenario1\\WEBs_Scenario_1.db3"},

        "EconomicResult":{"HasResult":true,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Scenario1\\WEBs_Scenario_1.db3"},
        "HasResult":false,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Scenario1\\WEBs_Scenario_1.db3"},
        "Design":{"HasSmallDam":true,
        "HasHoldingPond":true,
        "HasGrazing":true,
        "HasTillage":false,
        "HasForageConversion":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Scenario1\\WEBs_Scenario_1.db3"}}],

        "HistoricalBaseScenario":{"BMPScenerioBaseType":0,"TillageType":2,"ProjectFolder":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\",
        "ID":-1,"Description":"Historical Base Scenario",
        "CreateTime":"2014-07-21T15:04:37.1726382-04:00",
        "LastModifiedTime":"2014-07-21T15:04:37.1726382-04:00",
        "LastSWATRunTime":"0001-01-01T00:00:00",
        "Name":"Historical Base Scenario",
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3",
        "Type":1,
        "CropBMPLevel":0,
        "Result":{"SWATResult":{"HasResult":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3"},
        "EconomicResult":{"HasResult":true,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3"},
        "HasResult":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3"},
        "Design":{"HasSmallDam":true,"HasHoldingPond":true,"HasGrazing":true,"HasTillage":false,"HasForageConversion":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Historical\\WEBs_Scenario_Historical.db3"}},

        "ConventionalBaseScenario":{"BMPScenerioBaseType":1,"TillageType":2,"ProjectFolder":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\",
        "ID":-2,"Description":"Conventional Base Scenario",
        "CreateTime":"2014-07-21T15:04:42.4641673-04:00",
        "LastModifiedTime":"2014-07-21T15:04:42.4641673-04:00",
        "LastSWATRunTime":"0001-01-01T00:00:00",
        "Name":"Conventional Base Scenario",
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3",
        "Type":2,
        "CropBMPLevel":0,
        "Result":{"SWATResult":{"HasResult":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3"},
        "EconomicResult":{"HasResult":true,"DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3"},
        "HasResult":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3"},
        "Design":{"HasSmallDam":false,"HasHoldingPond":false,"HasGrazing":false,"HasTillage":false,"HasForageConversion":false,
        "DatabasePath":"C:\\Users\\radfordd\\Documents\\Projects\\STC\\Conventional\\WEBs_Scenario_Conventional.db3"}}}
        * */        
    }
}
