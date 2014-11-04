/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

/**
 *
 * @author Shao
 */
public abstract class ScenarioDatabaseStructure extends SQLiteDatabase{
    public ScenarioDatabaseStructure(String scenarioDatabasePath){
        super(scenarioDatabasePath);
    }

    protected static String tableNameSmallDam = "small_dams";
    protected static String tableNameSmallDamEconomic = "small_dams_economic";

    protected static String tableNameHoldingPond = "holding_ponds";
    protected static String tableNameHoldingPondEconomic = "holding_ponds_economic";

    protected static String tableNameGrazing = "grazing";
    protected static String tableNameGrazingHRU = "grazing_hrus";
    protected static String tableNameGrazingEconomic = "grazing_economic";
    protected static String tableNameGrazingEconomicSubbasin = "grazing_economic_subbasins";

    protected static String tableNameTillage = "tillage";
    protected static String tableNameTillageHRU = "tillage_hrus";

    protected static String tableNameForage = "forage";
    protected static String tableNameForageHRU = "forage_hrus";

    protected static String tableNameCropEconomicfield = "crop_economic_fields";
    protected static String tableNameCropEconomicfarm = "crop_economic_farms";
    protected static String tableNameCropEconomicSubbasin = "crop_economic_subbasins";

    public static String BMPEconomicResultTableName(BMPType type) {
        switch (type)
        {
            case Small_Dam:
                return tableNameSmallDamEconomic;
            case Holding_Pond:
                return tableNameHoldingPondEconomic;
            case Grazing:
                return tableNameGrazingEconomic;
            case Grazing_Subbasin:
                return tableNameGrazingEconomicSubbasin;
            case Tillage_Farm:
            case Forage_Farm:
                return tableNameCropEconomicfarm;
            case Tillage_Field:
            case Forage_Field:
                return tableNameCropEconomicfield;
            case Tillage_Subbasin:           
            case Forage_Subbasin:
                return tableNameCropEconomicSubbasin;
            default:
                return "";
        }
    }
    
    public static String BMPDesignTableName(BMPType type)  {
        switch (type)
        {
            case Small_Dam:
                return tableNameSmallDam;
            case Holding_Pond:
                return tableNameHoldingPond;
            case Grazing:
                return tableNameGrazing;
            case Tillage_Farm:
            case Tillage_Field:
            case Tillage_Subbasin:
                return tableNameTillage;
            case Forage_Farm:
            case Forage_Field:
            case Forage_Subbasin:
                return tableNameForage;
            default:
                return "";
        }
    }
    
    public static String columnNameID = "ID";
    public static String columnNameYear = "Year";
    public static String columnNameCost = "Cost";

    public static String columnNameSmallDamEmbankment = "Embankment";
    public static String columnNameSmallDamLifetime = "LifeTime";

    public static String columnNameHoldingPondHRU = "HRU";
    public static String columnNameHoldingPondCattles = "Cattles";
    public static String columnNameHoldingPondClayLiner = "ClayLiner";
    public static String columnNameHoldingPondPlasticLn = "PlasticLn";
    public static String columnNameHoldingPondWireFence = "WireFence";
    public static String columnNameHoldingPondDistance = "Distance";
    public static String columnNameHoldingPondTrenching = "Trenching";
    public static String columnNameHoldingPondPondYrs = "PondYrs";
    public static String columnNameHoldingPondAnnualCost = "AnnualCost";
    public static String columnNameHoldingPondMaintenance = "Maintenance";
    public static String columnNameHoldingPondTotalCost = "TotalCost";

    public static String columnNameGrazingArea = "GrazingHa";
    public static String columnNameGrazingUnitCost = "UnitCost";

    protected static String columnNameTillageType = "Tillage";

    protected static String columnNameYield = "Yield";
    protected static String columnNameRevenue = "Revenue";
    protected static String columnNameNetReturn = "NetReturn";    
}
