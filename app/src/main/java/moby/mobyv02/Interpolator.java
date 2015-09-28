package moby.mobyv02;

import java.util.ArrayList;

/**
 * Created by quezadjo on 9/26/2015.
 */
public class Interpolator {


    public static Float CosineInterpolate(Float y1,Float y2,Float mu)
    {
        double mu2;

        mu2 = (1.0f-Math.cos(mu*Math.PI))/2.0f;
        Float f_mu2 = new Float(mu2);
        return(y1*(1.0f-f_mu2)+y2*f_mu2);
    }

    public static Float LinearInterpolate(Float y1,Float y2,Float mu)
    {
        return(y1*(1-mu)+y2*mu);
    }


    public static Float[] Interpolate(Float[] a, String mode) {

        // Check that have at least the very first and very last values non-null
        if (!(a[0] != null && a[a.length-1] != null)) return null;

        ArrayList<Integer> non_null_idx = new ArrayList<Integer>();
        ArrayList<Integer> steps = new ArrayList<Integer>();

        int step_cnt = 0;
        for (int i=0; i<a.length; i++)
        {
            if (a[i] != null)
            {
                non_null_idx.add(i);
                if (step_cnt != 0) {
                    steps.add(step_cnt);
                    System.err.println("aDDed step >> " + step_cnt);
                }
                step_cnt = 0;
            }
            else
            {
                step_cnt++;
            }
        }

        Float f_start = null;
        Float f_end = null;
        Float f_step = null;
        Float f_mu = null;

        int i = 0;
        while (i < a.length - 1) // Don't do anything for the very last element (which should never be null)
        {
            if (a[i] != null && non_null_idx.size() > 1 && steps.size() > 0)
            {
                f_start = a[non_null_idx.get(0)];
                f_end = a[non_null_idx.get(1)];
                f_step = new Float(1.0) / new Float(steps.get(0) + 1);
                f_mu = f_step;
                non_null_idx.remove(0);
                steps.remove(0);
            }
            else if (a[i] == null)
            {
                if (mode.equalsIgnoreCase("cosine"))
                    a[i] = CosineInterpolate(f_start, f_end, f_mu);
                else
                    a[i] = LinearInterpolate(f_start, f_end, f_mu);
                f_mu += f_step;
            }
            i++;
        }

        return a;
    }

}
