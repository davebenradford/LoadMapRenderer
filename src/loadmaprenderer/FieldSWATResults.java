/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shao
 */
public class FieldSWATResults{
    private Map<Integer, Map<Integer, ResultBase>> _fieldResults = null;

    public void Add(int field, int year, SWATResultColumnType type, double r){
        if (_fieldResults == null) _fieldResults = new HashMap<Integer, Map<Integer, ResultBase>>();

        Map<Integer, ResultBase> yearValue = null;
        if (!_fieldResults.containsKey(year))
        {
            yearValue = new HashMap<Integer, ResultBase>();
            _fieldResults.put(year, yearValue);
        }
        
        yearValue = _fieldResults.get(year);
        ResultBase values = null;
        if (!yearValue.containsKey(field))
        {
            values = new ResultBase();
            yearValue.put(field, values);
        }
        
        values = yearValue.get(field);

        values.SetResult(type, r);
    }

    public void ReCalculateWithWeight(FieldWeights ws){
        for (int year : _fieldResults.keySet())
        {
            Map<Integer, ResultBase> map = _fieldResults.get(year);
            ws.ReCalculateFieldResults(map); // !! ref
        }
    }

    public String SQLInsert(String prefix){
        StringBuilder sb = new StringBuilder();

        for (int year : _fieldResults.keySet())
        {
            Map<Integer, ResultBase> r = _fieldResults.get(year);

            for (int field : r.keySet())
            {
                sb.append(prefix + String.valueOf(field) + "," + String.valueOf(year));

                ResultBase rb = r.get(field);
                for (SWATResultColumnType type : SWATResultColumnType.values())
                {
                    double value = rb.Result(type);
                    sb.append("," + String.valueOf(value));

                }
                sb.append(");"); 
            }
        }
        return sb.toString();
    }
}
