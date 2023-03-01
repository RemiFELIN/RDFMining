/*
Grammatical Evolution in Java
Release: GEVA-v2.0.zip
Copyright (C) 2008 Michael O'Neill, Erik Hemberg, Anthony Brabazon, Conor Gilligan 
Contributors Patrick Middleburgh, Eliott Bartley, Jonathan Hugosson, Jeff Wrigh

Separate licences for asm, bsf, antlr, groovy, jscheme, commons-logging, jsci is included in the lib folder. 
Separate licence for rieps is included in src/com folder.

This licence refers to GEVA-v2.0.

This software is distributed under the terms of the GNU General Public License.


This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
/>.
*/

package com.i3s.app.rdfminer.evolutionary.geva.Util;


import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.MersenneTwisterFast;

/**
 * This class is used to generate constants for symbolic regression
 * and the Binomial 3 problem.
 * @author jbyrne
 */
public class ConstantGenerator {
    
    private static int modulo = 1000;
    private static int decimalPlaces = 2; 
    private static int divisor = 100;
    
    protected MersenneTwisterFast m;
            
    public ConstantGenerator(){ 
        m = new MersenneTwisterFast();
    }

    /**
     * This sets the modulo value to limit the range
     * @param mod
     */
    public static void setMod(int mod){
        if(mod > 1){
            modulo = mod;
            System.out.println("The new mod value is "+modulo );
        }
    }

    /**
     * This displays the current set modulo value (default is 1000);
     * @return int
     */
    public static int getMod(){
        return modulo;
    }
    
    /**
     * This sets the number of decimal places 
     * @param dp The number of decimal places
     */
    public static void setDecimalPlaces(int dp){
        if(dp >= 0){
           decimalPlaces = dp;
           double tmp = Math.pow(10,decimalPlaces);
           divisor = (int)tmp;
        }
        else
            System.out.println("decimalPlaces value was not valid");
	
	System.out.println("decimalPlaces set to "+decimalPlaces );
	System.out.println("divisor set to "+divisor );
    }

    /**
     * This displays the current decimal places (default is 2);
     * @return int
     */
    public static int getDecimalPlaces()
    {
        return decimalPlaces;
    }
     
    /**
     * This generates a float constant value 
     * @return float
     */
    public float getConstant()
    {
        
       float constant = Math.abs((float)m.nextInt());
       constant = constant %(modulo*divisor);    //set the Mod first
       constant = (constant /divisor);           //check to 2 decimal places
                           
       return constant;
    }
    
    /**
     * This generates a float constant value in a string format
     * @return String
     */
    public String getStringConstant()
    {
       float tmpConst = getConstant();       
       StringBuffer strConst =new StringBuffer();
       strConst.append(tmpConst);
       return strConst.toString();
    }
    
    public static void main(String[] args) 
    {
        ConstantGenerator cg = new ConstantGenerator();
        ConstantGenerator.setDecimalPlaces(2);
        System.out.println(cg.getStringConstant());
    }
    
}
