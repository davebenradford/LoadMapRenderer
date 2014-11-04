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
public class BMPFieldTillage extends BMPCrop
{
    public BMPFieldTillage(ResultSet row, int feaIndex, Project project, Scenario scenario) throws SQLException{
        super(row, feaIndex, BMPType.Tillage_Field, project, scenario);
    }
}
