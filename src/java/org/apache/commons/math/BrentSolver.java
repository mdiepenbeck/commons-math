/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.math;

/**
 * Provide the Brent algorithm for solving for zeros of real univariate
 * functions.
 * It will only search for one zero in the given interval.
 * The function is supposed to be continuous but not necessarily smooth.
 *  
 * @author pietsch at apache.org
 */
public class BrentSolver extends UnivariateRealSolverImpl {

    private UnivariateRealFunction f;

    public BrentSolver(UnivariateRealFunction f) {
        super(100, 1E-6);
        this.f = f;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.math.UnivariateRealSolver#solve(double, double)
     */
    public double solve(double min, double max) throws MathException {
        clearResult();
        double x0 = min;
        double x1 = max;
        double y0 = f.value(x0);
        double y1 = f.value(x1);
        if ((y0 > 0) == (y1 > 0)) {
            throw new MathException("Interval doesn't bracket a zero.");
        }
        double x2 = x0;
        double y2 = y0;
        double delta = x1 - x0;
        double oldDelta = delta;

        int i = 0;
        while (i < maximalIterationCount) {
            if (Math.abs(y2) < Math.abs(y1)) {
                x0 = x1;
                x1 = x2;
                x2 = x0;
                y0 = y1;
                y1 = y2;
                y2 = y0;
            }
            double tolerance = Math.max(relativeAccuracy * Math.abs ( x1 ),
                                        absoluteAccuracy);
            if (Math.abs(y1) <= functionValueAccuracy) {
                // Avoid division by very small values. Assume
                // the iteration has converged (the problem may
                // still be ill conditioned
                setResult(x1,i);
                return result;
            }
            double dx = 0.5 * (x2 - x1);
            if (Math.abs(dx) <= tolerance) {
                setResult(x1,i);
                return result;
            }
            if (Math.abs(oldDelta) < tolerance || Math.abs(y0) <= Math.abs(y1)) {
                // Force bisection.
                delta = dx;
                oldDelta = delta;
            } else {
                double r3 = y1 / y0;
                double p;
                double p1;
                if (x0 == x2) {
                    // Linear interpolation.
                    p = 2.0 * dx * r3;
                    p1 = 1.0 - r3;
                } else {
                    // Inverse quadratic interpolation.
                    double r1 = y0 / y2;
                    double r2 = y1 / y2;
                    p = r3 * (2.0 * dx * r1 * (r1 - r2) - (x1 - x0) * (r2 - 1.0));
                    p1 = (r1 - 1.0) * (r2 - 1.0) * (r3 - 1.0);
                }
                if (p > 0.0) {
                    p1 = -p1;
                } else {
                    p = -p;
                }
                if (2.0 * p >= 3.0 * dx * p1 - Math.abs(tolerance * p1)
                    || p >= Math.abs(0.5 * oldDelta * p1)) {
                    // Inverse quadratic interpolation gives a value
                    // in the wrong direction, or progress is slow.
                    // Fall back to bisection.
                    delta = dx;
                    oldDelta = delta;
                } else {
                    oldDelta = delta;
                    delta = p / p1;
                }
            }
            // Save old X1, Y1 
            x0 = x1;
            y0 = y1;
            // Compute new X1, Y1
            if (Math.abs(delta) > tolerance) {
                x1 = x1 + delta;
            } else if (dx > 0.0) {
                x1 = x1 + tolerance;
            } else if (dx <= 0.0) {
                x1 = x1 - tolerance;
            }
            y1 = f.value(x1);
            if ((y1 > 0) == (y2 > 0)) {
                x2 = x0;
                y2 = y0;
                delta = x1 - x0;
                oldDelta = delta;
            }
            i++;
        }
        throw new MathException("Maximal iteration number exceeded.");
    }

}
