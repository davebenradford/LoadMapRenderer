/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shao
 */
public class FieldWeights{
    private Map<Integer,FieldWeight> _weights;

    public FieldWeights(String sqliteDB) throws SQLException
    {
        _weights = new HashMap<Integer, FieldWeight>();

        ResultSet rs = Query.GetDataTable(
            "select * from field_weight", sqliteDB);

        Map<Integer, Double> areas = new HashMap<Integer, Double>();
        while (rs.next())
        {
            FieldWeight w = new FieldWeight(rs);
            _weights.put(w.getID(),w);
        }
    }

    private double TotalVolumnFraction(Map<Integer, ResultBase> rs, SWATResultColumnType type)
    {
        double total = 0.0;
        double totalWeightedVolumn = 0.0;

        for (int field : rs.keySet())
        {
            FieldWeight w = null;
            if (!_weights.containsKey(field)) continue;
            
            w = _weights.get(field);

            double area = w.getArea();
            double weight = w.Weight(type);
            double field_old = rs.get(field).Result(type);

            total += area * field_old;
            totalWeightedVolumn += area * field_old * weight;
        }
        return total / totalWeightedVolumn;
    }

    private void ReCalculateFieldResults(Map<Integer, ResultBase> map, SWATResultColumnType type)// !! ref
    {
        double totalVolumnFraction = this.TotalVolumnFraction(map, type);

        for (int field : map.keySet())
        {
            FieldWeight w = null;
            if (!_weights.containsKey(field)) continue;

            w = _weights.get(field);
            double weight = w.Weight(type);
            double field_old = map.get(field).Result(type);
            double field_new = weight * field_old * totalVolumnFraction;

            map.get(field).SetResult(type, field_new); //set the new value
        }
    }

    public void ReCalculateFieldResults(Map<Integer, ResultBase> map) // !! ref
    {
        ReCalculateFieldResults(map, SWATResultColumnType.water);// !! ref
        ReCalculateFieldResults(map, SWATResultColumnType.sediment);// !! ref
        ReCalculateFieldResults(map, SWATResultColumnType.PP);// !! ref
        ReCalculateFieldResults(map, SWATResultColumnType.DP);// !! ref
        ReCalculateFieldResults(map, SWATResultColumnType.PN);// !! ref
        ReCalculateFieldResults(map, SWATResultColumnType.DN);// !! ref
    }
}
