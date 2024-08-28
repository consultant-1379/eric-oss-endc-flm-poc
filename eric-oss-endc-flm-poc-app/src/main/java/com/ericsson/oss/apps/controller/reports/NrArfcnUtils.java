/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.apps.controller.reports;

import java.util.List;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
class NrArfcnUtils {
    @Getter
    private class NrBandArfcn {
        private int band;
        private int firstArfdn;
        private int lastArfcn;

        private NrBandArfcn(int b, int f, int l) {
            band = b;
            firstArfdn = f;
            lastArfcn = l;
        }

        public static NrBandArfcn of(int b, int f, int l) {
            return new NrBandArfcn(b, f, l);
        }

        public boolean isArfcnInBand(Integer arfcn) {
            return arfcn >= firstArfdn && arfcn <= lastArfcn;
        }
    }

    // nrBandDlMap is defined according to table 5.4.2.3-1 & 5.4.2.3-2 in 3GPP 38.104-hd0.
    private static final List<NrBandArfcn> nrBandDlArfcnRange = List.of(
        NrBandArfcn.of(1, 422000, 434000),
        NrBandArfcn.of(2, 386000, 398000),
        NrBandArfcn.of(3, 361000, 376000),
        NrBandArfcn.of(5, 173800, 178800),
        NrBandArfcn.of(7, 524000, 538000),
        NrBandArfcn.of(8, 185000, 192000),
        NrBandArfcn.of(12, 145800, 149200),
        NrBandArfcn.of(13, 149200, 151200),
        NrBandArfcn.of(14, 151600, 153600),
        NrBandArfcn.of(18, 172000, 175000),
        NrBandArfcn.of(20, 158200, 164200),
        NrBandArfcn.of(24, 305000, 311800),
        NrBandArfcn.of(25, 386000, 399000),
        NrBandArfcn.of(26, 171800, 178800),
        NrBandArfcn.of(28, 151600, 160600),
        NrBandArfcn.of(29, 143400, 145600),
        NrBandArfcn.of(30, 470000, 472000),
        NrBandArfcn.of(34, 402000, 405000),
        NrBandArfcn.of(38, 514000, 524000),
        NrBandArfcn.of(39, 376000, 384000),
        NrBandArfcn.of(40, 460000, 480000),
        NrBandArfcn.of(41, 499200, 537999),
        NrBandArfcn.of(46, 743334, 795000),
        NrBandArfcn.of(48, 636667, 646666),
        NrBandArfcn.of(50, 286400, 303400),
        NrBandArfcn.of(51, 285400, 286400),
        NrBandArfcn.of(53, 496700, 499000),
        NrBandArfcn.of(65, 422000, 440000),
        NrBandArfcn.of(66, 422000, 440000),
        NrBandArfcn.of(67, 147600, 151600),
        NrBandArfcn.of(70, 399000, 404000),
        NrBandArfcn.of(71, 123400, 130400),
        NrBandArfcn.of(74, 295000, 303600),
        NrBandArfcn.of(75, 286400, 303400),
        NrBandArfcn.of(76, 285400, 286400),
        NrBandArfcn.of(77, 620000, 680000),
        NrBandArfcn.of(78, 620000, 653333),
        NrBandArfcn.of(79, 693334, 733333),
        NrBandArfcn.of(85, 145600, 149200),
        NrBandArfcn.of(90, 499200, 538000),
        NrBandArfcn.of(91, 285400, 286400),
        NrBandArfcn.of(92, 286400, 303400),
        NrBandArfcn.of(93, 285400, 286400),
        NrBandArfcn.of(94, 286400, 303400),
        NrBandArfcn.of(96, 795000, 875000),
        NrBandArfcn.of(100, 183880, 185000),
        NrBandArfcn.of(101, 380000, 382000),
        NrBandArfcn.of(102, 796334, 828333),
        NrBandArfcn.of(104, 828334, 875000),
        NrBandArfcn.of(257, 2054166, 2104165),
        NrBandArfcn.of(258, 2016667, 2070832),
        NrBandArfcn.of(259, 2270832, 2337499),
        NrBandArfcn.of(260, 2229166, 2279165),
        NrBandArfcn.of(261, 2070833, 2084999),
        NrBandArfcn.of(262, 2399166, 2415832),
        NrBandArfcn.of(263, 2564083, 2794243)
    );

    private static final int NREF1 = 600000;
    private static final int NREF2 = 2016667;
    private static final int NREF3 = 3279165;

    public List<Integer> getPossibleBands(Integer arfcn) {
        return nrBandDlArfcnRange.stream().filter(item -> item.isArfcnInBand(arfcn)).map(item -> item.getBand()).toList();
    }

    public int getFirstBand(Integer arfcn) {
        List<Integer> bands = getPossibleBands(arfcn);
        if (!bands.isEmpty()) {
            return bands.get(0);
        }
        throw new IllegalArgumentException("Not an ARFCN defined for 5G NR Band");
    }

    public double nrArfcnToFreq(Integer arfcn) {
        double frequency;
        if (0 <= arfcn && arfcn < NREF1) {
            frequency = calcFreq(arfcn, 0, 5, 0);
        } else if (NREF1 <= arfcn && arfcn < NREF2) {
            frequency = calcFreq(arfcn, 3000, 15, NREF1);
        } else if (NREF2 <= arfcn && arfcn <= NREF3) {
            frequency = calcFreq(arfcn, 24250.08, 60, NREF2);
        } else {
            throw new IllegalArgumentException("ARFCN out of range for 5G NR");
        }
        return frequency;
    }

    public String nrArfcnFreqType(Integer arfcn) {
        if (arfcn == null || arfcn < 0 || arfcn > NREF3) {
            throw new IllegalArgumentException("ARFCN out of range of 5G NR");
        }
        return arfcn >= NREF2 ? "FR2" : "FR1";
    }

    private double calcFreq(Integer arfcn, double fRefOffs, double deltaF, int nRefOffs) {
        return fRefOffs + deltaF * (arfcn - nRefOffs) / 1000;
    }
}
