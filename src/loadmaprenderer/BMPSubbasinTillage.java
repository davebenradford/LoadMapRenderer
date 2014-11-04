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
public class BMPSubbasinTillage extends BMPCrop
{
    public BMPSubbasinTillage(ResultSet row, int feaIndex, Project project, Scenario scenario) throws SQLException{
        super(row, feaIndex, BMPType.Tillage_Subbasin, project, scenario);
    }
}
