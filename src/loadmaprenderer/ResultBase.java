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
public class ResultBase{
    protected double _water;
    protected double _sediment;
    protected double _pp;
    protected double _dp;
    protected double _pn;
    protected double _dn;

    public void SetResult(SWATResultColumnType type, double r){
        switch (type)
        {
            case water :
                _water = r;
                break;
            case sediment :
                _sediment = r;
                break;
            case PP :
                _pp = r;
                break;
            case DP:
                _dp = r;
                break;
            case PN :
                _pn = r;
                break;
            case DN :
                _dn = r;
                break;
            default :
                return;
        }
    }

    public double Result(SWATResultColumnType type){
        switch (type)
        {
            case water:
                return _water;
            case sediment:
                return _sediment;
            case PP:
                return _pp;
            case DP:
                return _dp;
            case PN:
                return _pn;
            case DN:
                return _dn;
            case TN:
                return _pn + _dn;
            case TP:
                return _pp + _dp;
            default:
                return -1.0;
        }
    }
}
