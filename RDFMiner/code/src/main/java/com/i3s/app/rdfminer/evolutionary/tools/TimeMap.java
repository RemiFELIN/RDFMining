/**
 * 
 */
package com.i3s.app.rdfminer.evolutionary.tools;

import java.util.HashMap;

/**
 * A map storing a relation between time predictor and observed time.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class TimeMap extends HashMap<Long, Long> {
	/**
	 * Serial version UID for the Serializable interface.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct an empty time map.
	 */
	public TimeMap() {
		super();
	}

	/**
	 * Get the observed time for the given predictor, zero if no observation has
	 * been made.
	 * 
	 * @param predictor a time predictor.
	 * @return the observed time for the predictor.
	 */
	public long get(long predictor) {
		Long o = get(new Long(predictor));
		return o == null ? 0L : o.longValue();
	}

	/**
	 * Update the observed time for the given predictor with the max of the current
	 * value and the supplied value.
	 */
	public void maxput(long predictor, long value) {
		if (value > get(predictor))
			put(predictor, value);
	}

	/**
	 * Perform a linear regression of the stored data, and return the coefficients
	 * of the best fit.
	 * <p>
	 * This method computes the best fit (least squares) line <var>y</var> =
	 * <var>a</var><var>x</var> + <var>b</var> through the set of (<var>x</var>,
	 * <var>y</var>) points given by the (key, value) pairs of this map.
	 * </p>
	 * <p>
	 * The implementation of this method is an adaptation of
	 * {@link http://introcs.cs.princeton.edu/java/97data/LinearRegression.java.html}
	 * 
	 * @return the coefficients of the line best fitting the data.
	 */
	public double[] linearRegression() {
		double[] x = new double[size()];
		double[] y = new double[size()];
		double[] coeff = new double[2];

		// first pass: read in data, compute xbar and ybar
		double sumx = 0.0, sumy = 0.0;
		int n = 0;
		for (Long tp : this.keySet()) {
			x[n] = tp.doubleValue();
			y[n] = get(tp).doubleValue();
			sumx += x[n];
			sumy += y[n];
			n++;
		}
		double xbar = sumx / n;
		double ybar = sumy / n;

		// second pass: compute summary statistics
		double xxbar = 0.0, xybar = 0.0;
		for (int i = 0; i < n; i++) {
			xxbar += (x[i] - xbar) * (x[i] - xbar);
			xybar += (x[i] - xbar) * (y[i] - ybar);
		}
		coeff[1] = xybar / xxbar;
		coeff[0] = ybar - coeff[1] * xbar;

		return coeff;
	}

	/**
	 * Fit the data with a line passing through the origin and return its angular
	 * coefficient.
	 * <p>
	 * This method computes the best fit (least squares) line <var>y</var> =
	 * <var>a</var><var>x</var> through the set of (<var>x</var>, <var>y</var>)
	 * points given by the (key, value) pairs of this map.
	 * </p>
	 * 
	 * @return the angular coefficient of the line best fitting the data.
	 */
	public double angularCoeff() {
		double[] x = new double[size()];
		double[] y = new double[size()];

		double sumx2 = 0.0, sumxy = 0.0;
		int n = 0;
		for (Long tp : this.keySet()) {
			x[n] = tp.doubleValue();
			y[n] = get(tp).doubleValue();
			sumx2 += x[n] * x[n];
			sumxy += x[n] * y[n];
			n++;
		}

		return sumxy / sumx2;
	}
}
