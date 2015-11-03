
/*********************************************************************************************************
 * Software License Agreement (BSD License)
 * 
 * Copyright 2014 Next Century Corporation. All rights reserved.   
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ***********************************************************************************************************/
package com.ncc.sightt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Put data into a certain number of bins
 */
public class Binner {

    public class Bin {
        double begin;
        double end;
        double number;
    }

    List<Bin> bins;

    List<Double> data = new ArrayList<Double>();

    double max = -999999999.;
    double min = 999999999.;

    public Binner() {

    }

    public List<Bin> bin(int numbins) {
        bins = new ArrayList<Bin>();

        // Create all the bins. Add a tiny bit so that the data all falls inside
        // the range
        max *= 1.00001;
        min *= 0.99999;
        double binsize = (max - min) / numbins;
        double begin = min;
        for (int ii = 0; ii < numbins; ii++) {
            Bin b = new Bin();
            b.begin = begin;
            begin += binsize;
            b.end = begin;
            bins.add(b);
        }

        for (Double d : data) {
            // Determine which bin this data goes into
            int whichBin = (int) ((d - min) / binsize);
            Bin b = bins.get(whichBin);
            if (d >= b.begin && d <= b.end) {
                b.number++;
            } else {
                System.out.println("Error binning: Got data " + d
                        + " but bin has range: " + b.begin + " " + b.end);
            }
        }

        return bins;
    }

    public void addData(double d) {
        data.add(d);
        if (d > max) {
            max = d;
        }
        if (d < min) {
            min = d;
        }
    }

    public void printBins() {
        System.out.println("Bins Min: " + min);
        System.out.println("Bins Max: " + max);
        for (int ii = 0; ii < bins.size(); ii++) {
            Bin bin = bins.get(ii);
            System.out.println(" " + bin.begin + " " + bin.end + " " + bin.number);
        }
    }

    /**
     * Main
     */
    final public static int NUMTRIALS = 100000;

    public static void main(String[] args) {
        Binner b = new Binner();
        Random r = new Random();
        for (int ii = 0; ii < NUMTRIALS; ii++) {
            b.addData(r.nextDouble());
        }

        b.bin(30);
        b.printBins();
    }

}
