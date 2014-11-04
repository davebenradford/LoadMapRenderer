/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;


import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Shao
 */
public abstract class BMPCrop extends BMPItem
{
    public BMPCrop(ResultSet row, int feaIndex, BMPType type, Project project, Scenario scenario) throws SQLException{
        super(row, feaIndex, type,project,scenario);
    }

    //public event EventHandler onEconomicChanged; // !! Shao.H

    /* H.shao
    private void getEconomic(Object sender, EventArgs e) throws Exception{
        if (_project != null && _scenario != null)
        {
            if (getType() == BMPType.Tillage_Field)
                _economic = _project.getSpatial().GetCropEconomic(BMPSelectionLevelType.Field,getID(), _scenario.getBMPScenerioBaseType(), _scenario.getTillageType(), null, -1);
            else if (getType() == BMPType.Tillage_Farm)
                _economic = _project.getSpatial().GetCropEconomic(BMPSelectionLevelType.Farm, getID(), _scenario.getBMPScenerioBaseType(), _scenario.getTillageType(), null, -1);
            else if (getType() == BMPType.Tillage_Subbasin)
                _economic = _project.getSpatial().GetCropEconomic(BMPSelectionLevelType.Subbasin, getID(), _scenario.getBMPScenerioBaseType(), _scenario.getTillageType(), null, -1);

            else if (getType() == BMPType.Forage_Field)
                _economic = _project.getSpatial().GetCropEconomic(BMPSelectionLevelType.Field, getID(), _scenario.getBMPScenerioBaseType(), TillageType.Forage, null, -1);
            else if (getType() == BMPType.Forage_Farm)
                _economic = _project.getSpatial().GetCropEconomic(BMPSelectionLevelType.Farm, getID(), _scenario.getBMPScenerioBaseType(), TillageType.Forage, null, -1);
            else if (getType() == BMPType.Forage_Subbasin)
                _economic = _project.getSpatial().GetCropEconomic(BMPSelectionLevelType.Subbasin, getID(), _scenario.getBMPScenerioBaseType(), TillageType.Forage, null, -1);

            if (onEconomicChanged != null)
                onEconomicChanged(this, new EventArgs());
        }
    } */

    private CropEconomic _economic;

    public CropEconomic getEconomic() throws Exception{
        //H.Shao if (_economic == null)
            //H.Shao getEconomic(null, null);

        return _economic;
    }

    @Override
    public String toString(){
        return String.valueOf(getID());
    }

    @Override
    public double getCost(){
        return 0.0;
    }

    @Override
    public double getAnnualCost(){
        return 0.0;
    }

    @Override
    public String InsertSQL(){
        return String.valueOf(getID()) + ");";
    }

    @Override
    public String InsertSQL_Economic(int year){
        //H.Shao throw new NotImplementedException();
        return "";
    }
}
