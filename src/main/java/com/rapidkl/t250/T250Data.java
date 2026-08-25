package com.rapidkl.t250;

/**
 * T250Data - seeds the graph with the real Rapid KL route T250.
 *
 * Route source: Rapid KL feeder service T250, which runs as a LOOP between
 * LRT Wangsa Maju station and Setapak Sentral (published length ~13.9 km).
 * The bus travels out through the TAR UMT / Taman Bunga Raya side, passes
 * Danau Kota (Platinum Lake condos) up to Setapak Central, returns past
 * Vista Wirajaya, then completes an inner loop around Wangsa Maju Section 2
 * (AEON Alpha Angle, Desa Setapak, Section 2 flats and the military hospital)
 * before arriving back at LRT Wangsa Maju.
 *
 * Assumptions used when converting the real route into graph form:
 *  1. Inbound and outbound bus stops that share the same name are merged
 *     into ONE vertex (they serve the same location on opposite sides of
 *     the road), e.g. the four TAR UMT gate stops appear once each.
 *  2. Per-segment distances are approximate values estimated from the
 *     published total route length (~13.1 km in this model vs 13.9 km real).
 */
public class T250Data {

    /** Default vertices: {stop name, official code, "Y" if interchange/hub}. */
    private static final String[][] DEFAULT_STOPS = {
        {"LRT Wangsa Maju",                "KL2097", "Y"},
        {"Tar Villa Setapak",              "KL193",  "N"},
        {"PULAPOT",                        "KL164",  "N"},
        {"Surau Taman Bunga Raya",         "KL2100", "N"},
        {"TAR UMT Gate 4",                 "-",      "N"},
        {"TAR UMT Main Gate",              "KL163",  "N"},
        {"TAR UMT Gate 2",                 "-",      "N"},
        {"Surau Al-Amin",                  "KL980",  "N"},
        {"Indah Apartments",               "KL981",  "N"},
        {"SK Danau Kota",                  "KL970",  "N"},
        {"PV 12 Platinum Lake",            "KL942",  "N"},
        {"PV 10 Platinum Lake",            "KL1519", "N"},
        {"PV 16 Platinum Lake",            "KL1520", "N"},
        {"Columbia Hospital Danau Kota",   "KL1598", "N"},
        {"Setapak Central",                "KL680",  "Y"},
        {"Vista Wirajaya",                 "KL973",  "N"},
        {"AEON Alpha Angle",               "KL171",  "N"},
        {"Wangsa Metroview",               "KL2103", "N"},
        {"Desa Setapak",                   "KL1604", "N"},
        {"Flat WM Sec 2 (Timur)",          "KL172",  "N"},
        {"Pasar & Penjaja WM Sec 2",       "KL173",  "N"},
        {"Flat WM Sec 2 (Utara)",          "KL174",  "N"},
        {"Flat WM Sec 2 (Barat)",          "KL175",  "N"},
        {"Hospital Tentera (Utara)",       "KL176",  "N"},
        {"Hospital Tentera (Selatan)",     "KL177",  "N"},
        {"Flat WM Sec 2 (Selatan)",        "KL178",  "N"}
    };

    /** Default edges: {stop A, stop B, approximate distance in km}. */
    private static final Object[][] DEFAULT_SEGMENTS = {
        // --- outbound leg: LRT Wangsa Maju -> TAR UMT -> Danau Kota -> Setapak Central
        {"LRT Wangsa Maju",              "Tar Villa Setapak",             0.9},
        {"Tar Villa Setapak",            "PULAPOT",                       0.6},
        {"PULAPOT",                      "Surau Taman Bunga Raya",        0.5},
        {"Surau Taman Bunga Raya",       "TAR UMT Gate 4",                0.5},
        {"TAR UMT Gate 4",               "TAR UMT Main Gate",             0.3},
        {"TAR UMT Main Gate",            "TAR UMT Gate 2",                0.3},
        {"TAR UMT Gate 2",               "Surau Al-Amin",                 0.6},
        {"Surau Al-Amin",                "Indah Apartments",              0.4},
        {"Indah Apartments",             "SK Danau Kota",                 0.5},
        {"SK Danau Kota",                "PV 12 Platinum Lake",           0.5},
        {"PV 12 Platinum Lake",          "PV 10 Platinum Lake",           0.2},
        {"PV 10 Platinum Lake",          "PV 16 Platinum Lake",           0.2},
        {"PV 16 Platinum Lake",          "Columbia Hospital Danau Kota",  0.4},
        {"Columbia Hospital Danau Kota", "Setapak Central",               0.6},
        // --- return leg: Setapak Central -> Vista Wirajaya -> back past TAR UMT
        {"Setapak Central",              "Vista Wirajaya",                0.6},
        {"Vista Wirajaya",               "TAR UMT Gate 2",                0.9},
        {"Surau Taman Bunga Raya",       "Tar Villa Setapak",             1.0},
        // --- inner loop: LRT Wangsa Maju -> AEON -> Wangsa Maju Section 2
        {"LRT Wangsa Maju",              "AEON Alpha Angle",              0.6},
        {"AEON Alpha Angle",             "Wangsa Metroview",              0.3},
        {"Wangsa Metroview",             "Desa Setapak",                  0.4},
        {"Desa Setapak",                 "Flat WM Sec 2 (Timur)",         0.4},
        {"Flat WM Sec 2 (Timur)",        "Pasar & Penjaja WM Sec 2",      0.3},
        {"Pasar & Penjaja WM Sec 2",     "Flat WM Sec 2 (Utara)",         0.3},
        {"Flat WM Sec 2 (Utara)",        "Flat WM Sec 2 (Barat)",         0.3},
        {"Flat WM Sec 2 (Barat)",        "Hospital Tentera (Utara)",      0.4},
        {"Hospital Tentera (Utara)",     "Hospital Tentera (Selatan)",    0.2},
        {"Hospital Tentera (Selatan)",   "Flat WM Sec 2 (Selatan)",       0.3},
        {"Flat WM Sec 2 (Selatan)",      "Wangsa Metroview",              0.6}
    };

    /**
     * Loads the complete default T250 network into the graph.
     * Called once at startup; the caller may clear the graph first if needed.
     */
    public static void loadDefaultNetwork(GraphADT graph) {
        for (String[] row : DEFAULT_STOPS) {
            graph.addStop(row[0], row[1], row[2].equals("Y"));
        }
        for (Object[] row : DEFAULT_SEGMENTS) {
            graph.addSegment((String) row[0], (String) row[1], (Double) row[2]);
        }
    }
}
