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
            case Small_Dams:
                return tableNameSmallDamEconomic;
            case Holding_Ponds:
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
            case Small_Dams:
                return tableNameSmallDam;
            case Holding_Ponds:
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
    
    public static String columnNameID = "id";
    public static String columnNameYear = "Year";
    public static String columnNameCost = "cost";

    public static String columnNameSmallDamEmbankment = "embankment";
    public static String columnNameSmallDamLifetime = "life_time";

    public static String columnNameHoldingPondHRU = "hru";
    public static String columnNameHoldingPondCattles = "cattle";
    public static String columnNameHoldingPondClayLiner = "clay_liner";
    public static String columnNameHoldingPondPlasticLn = "plastic_ln";
    public static String columnNameHoldingPondWireFence = "wire_fence";
    public static String columnNameHoldingPondDistance = "distance";
    public static String columnNameHoldingPondTrenching = "trenching";
    public static String columnNameHoldingPondPondYrs = "pond_yrs";
    public static String columnNameHoldingPondAnnualCost = "annual_cost";
    public static String columnNameHoldingPondMaintenance = "maintenance";
    public static String columnNameHoldingPondTotalCost = "total_cost";

    public static String columnNameGrazingArea = "grazing_ha";
    public static String columnNameGrazingUnitCost = "unit_cost";

    protected static String columnNameTillageType = "tillage";

    protected static String columnNameYield = "Yield";
    protected static String columnNameRevenue = "Revenue";
    protected static String columnNameNetReturn = "NetReturn";    
}
