/*
 * TruthDegree.java
 *
 * Created on April 3, 2008, 11:03 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.i3s.app.rdfminer.fuzzy;

/**
 * A fuzzy degree of truth.
 * <p>
 * A fuzzy degree of truth is a real number in the [0, 1] interval. 0
 * corresponds to (totally) false and 1 corresponds to (totally) true.
 *
 * @author Andrea G. B. Tettamanzi
 */
public class TruthDegree implements Comparable<TruthDegree> {
	/**
	 * The fuzzy degree of truth, represented as a double-precision floating-point
	 * value. Representing a fuzzy degree of truth by means of a double-precision
	 * floating-point number is probably an overshoot, because in most, if not all,
	 * applications a much coarser granularity of 0.01 is already more than enough.
	 * However, this is a convenient choice for a preliminary implementation, which
	 * could be revised later on.
	 */
	protected double truth;

	/** The <code>true</code> truth degree. */
	public static final TruthDegree TRUE = new TruthDegree(true);

	/** The <code>false</code> truth degree. */
	public static final TruthDegree FALSE = new TruthDegree(false);

	/** The neither <code>true</code> nor <code>false</code> truth degree. */
	public static final TruthDegree NEUTRAL = new TruthDegree(0.5);

	/** Creates a new degree of truth from a Boolean truth value. */
	public TruthDegree(boolean b) {
		if (b)
			truth = 1.0;
		else
			truth = 0.0;
	}

	/**
	 * Creates a new degree of truth from a given double-precision floating point
	 * number in [0, 1].
	 */
	public TruthDegree(double v) {
		if (v < 0.0 || v > 1.0)
			throw new IllegalArgumentException("A truth degree must be within the [0, 1] interval");
		truth = v;
	}

	/**
	 * Returns this truth degree as a double-precision floating-point number.
	 * 
	 * @return a number in the [0, 1] interval
	 */
	public double doubleValue() {
		return truth;
	}

	/**
	 * Returns <code>true</code> if the degree of truth is 0.
	 */
	public boolean isFalse() {
		return truth == 0.0;
	}

	/**
	 * Returns <code>true</code> if the degree of truth is 1.
	 */
	public boolean isTrue() {
		return truth == 1.0;
	}

	/**
	 * Returns <code>true</code> if the degree of truth is greater than, or equal to
	 * the given digree of truth. This method is useful to check whether an element
	 * belongs to a level set of a fuzzy set.
	 */
	public boolean isAtLeastAsTrueAs(TruthDegree that) {
		return truth >= that.truth;
	}

	/**
	 * Returns the distance between this truth degree and another truth degree. The
	 * difference is a number in [0, 1].
	 * 
	 * @param that
	 * @return the distance between the two truth degrees
	 */
	public double distance(TruthDegree that) {
		return Math.abs(truth - that.truth);
	}

	/**
	 * The natural comparison method for truth degrees.
	 */
	@Override
	public int compareTo(TruthDegree that) {
		return Double.compare(truth, that.truth);
	}

	/**
	 * Returns the negated degree of truth.
	 */
	public TruthDegree negated() {
		return new TruthDegree(1.0 - truth);
	}

	/**
	 * The triangular norm, used to calculate the truth of a conjunction.
	 */
	public static TruthDegree tnorm(TruthDegree x, TruthDegree y) {
		double z = x.truth;
		if (y.truth < z)
			z = y.truth;
		return new TruthDegree(z);
	}

	/**
	 * The triangular co-norm, used to calculate the truth of a conjunction.
	 */
	public static TruthDegree snorm(TruthDegree x, TruthDegree y) {
		double z = x.truth;
		if (y.truth > z)
			z = y.truth;
		return new TruthDegree(z);
	}

	/**
	 * Checks whether this truth degree equals another object. Two truth degrees are
	 * equal if and only if their <code>truth</code> attributes are equal.
	 * 
	 * @param o an object
	 * @return true if this truth degree equals the object
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TruthDegree))
			return false;
		TruthDegree that = (TruthDegree) o;
		return truth == that.truth;
	}

	/**
	 * Returns a hash code for this truth degree.
	 * 
	 * @return the hash code for this truth degree
	 */
	@Override
	public int hashCode() {
		// return (new Double(truth)).hashCode();
		return (int) Math.round(truth * (Integer.MAX_VALUE - 1.0));
	}

	/**
	 * Returns a string representation of the truth degree.
	 */
	public String toString() {
		return "" + truth;
	}
}
