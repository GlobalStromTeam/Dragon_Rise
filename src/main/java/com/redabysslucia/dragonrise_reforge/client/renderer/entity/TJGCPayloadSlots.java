package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

/** Authored payload hierarchy, fitted by build/tjgc_payloads.py. */
final class TJGCPayloadSlots {
    private TJGCPayloadSlots() {}

    static final String[] WEAPONS = {"Rocket", "BigRocket", "AAMissile", "ATMissile", "BigATMissile"};

    static final String[][][] BONES = {
        { // Rocket
            {"move_tjgc_Rocket_1", "bone8", "bone9", "bone13", "bone19", "bone21", "bone20"},
            {"move_tjgc_Rocket_2", "bone154", "bone155", "bone156", "bone157", "bone158", "bone159"},
            {"move_tjgc_Rocket_3", "bone97", "bone98", "bone99", "bone100", "bone101", "bone102"},
            {"move_tjgc_Rocket_4", "bone135", "bone136", "bone137", "bone138", "bone139", "bone140"},
            {"move_tjgc_Rocket_5", "bone103", "bone104", "bone105", "bone106", "bone107", "bone108"},
            {"move_tjgc_Rocket_6", "bone82", "bone83", "bone84", "bone119", "bone120", "bone121"},
            {"move_tjgc_Rocket_7", "bone109", "bone110", "bone111", "bone112", "bone113", "bone114"},
            {"move_tjgc_Rocket_8", "bone45", "bone46", "bone47", "bone48", "bone61", "bone62"},
            {"move_tjgc_Rocket_9", "bone25", "bone26", "bone27", "bone28", "bone29", "bone30"},
            {"move_tjgc_Rocket_10", "bone160", "bone161", "bone162", "bone163", "bone164", "bone165"},
            {"move_tjgc_Rocket_11", "bone55", "bone56", "bone57", "bone58", "bone59", "bone60"},
            {"move_tjgc_Rocket_12", "bone141", "bone142", "bone143", "bone144", "bone145", "bone146"},
            {"move_tjgc_Rocket_13", "bone37", "bone38", "bone39", "bone40", "bone41", "bone42"},
            {"move_tjgc_Rocket_14", "bone128", "bone129", "bone130", "bone131", "bone132", "bone133"},
            {"move_tjgc_Rocket_15", "bone91", "bone92", "bone93", "bone94", "bone95", "bone96"},
            {"move_tjgc_Rocket_16", "bone63", "bone64", "bone65", "bone66", "bone73", "bone74"},
            {"move_tjgc_Rocket_17", "bone31", "bone32", "bone33", "bone34", "bone35", "bone36"},
            {"move_tjgc_Rocket_18", "bone166", "bone167", "bone168", "bone169", "bone170", "bone171"},
            {"move_tjgc_Rocket_19", "bone49", "bone50", "bone51", "bone52", "bone53", "bone54"},
            {"move_tjgc_Rocket_20", "bone147", "bone148", "bone149", "bone150", "bone151", "bone152"},
            {"move_tjgc_Rocket_21", "bone67", "bone68", "bone69", "bone70", "bone71", "bone72"},
            {"move_tjgc_Rocket_22", "bone122", "bone123", "bone124", "bone125", "bone126", "bone127"},
            {"move_tjgc_Rocket_23", "bone85", "bone86", "bone87", "bone88", "bone89", "bone90"},
            {"move_tjgc_Rocket_24", "bone75", "bone76", "bone77", "bone78", "bone79", "bone80"},
            {"move_tjgc_Rocket_25", "bone335", "bone336", "bone337", "bone338", "bone339", "bone340"},
            {"move_tjgc_Rocket_26", "bone347", "bone348", "bone349", "bone350", "bone351", "bone352"},
            {"move_tjgc_Rocket_27", "bone221", "bone222", "bone223", "bone224", "bone225", "bone226"},
            {"move_tjgc_Rocket_28", "bone298", "bone299", "bone300", "bone301", "bone302", "bone303"},
            {"move_tjgc_Rocket_29", "bone202", "bone203", "bone204", "bone205", "bone206", "bone207"},
            {"move_tjgc_Rocket_30", "bone279", "bone280", "bone281", "bone282", "bone283", "bone284"},
            {"move_tjgc_Rocket_31", "bone183", "bone184", "bone185", "bone186", "bone187", "bone188"},
            {"move_tjgc_Rocket_32", "bone260", "bone261", "bone262", "bone263", "bone264", "bone265"},
            {"move_tjgc_Rocket_33", "bone189", "bone190", "bone191", "bone192", "bone193", "bone194"},
            {"move_tjgc_Rocket_34", "bone266", "bone267", "bone268", "bone269", "bone270", "bone271"},
            {"move_tjgc_Rocket_35", "bone240", "bone241", "bone242", "bone243", "bone244", "bone245"},
            {"move_tjgc_Rocket_36", "bone317", "bone318", "bone319", "bone320", "bone321", "bone322"},
            {"move_tjgc_Rocket_37", "bone227", "bone228", "bone229", "bone230", "bone231", "bone232"},
            {"move_tjgc_Rocket_38", "bone304", "bone305", "bone306", "bone307", "bone308", "bone309"},
            {"move_tjgc_Rocket_39", "bone214", "bone215", "bone216", "bone217", "bone218", "bone219"},
            {"move_tjgc_Rocket_40", "bone291", "bone292", "bone293", "bone294", "bone295", "bone296"},
            {"move_tjgc_Rocket_41", "bone195", "bone196", "bone197", "bone198", "bone199", "bone200"},
            {"move_tjgc_Rocket_42", "bone272", "bone273", "bone274", "bone275", "bone276", "bone277"},
            {"move_tjgc_Rocket_43", "bone246", "bone247", "bone248", "bone249", "bone250", "bone251"},
            {"move_tjgc_Rocket_44", "bone323", "bone324", "bone325", "bone326", "bone327", "bone328"},
            {"move_tjgc_Rocket_45", "bone252", "bone253", "bone254", "bone255", "bone256", "bone257"},
            {"move_tjgc_Rocket_46", "bone329", "bone330", "bone331", "bone332", "bone333", "bone334"},
            {"move_tjgc_Rocket_47", "bone233", "bone234", "bone235", "bone236", "bone237", "bone238"},
            {"move_tjgc_Rocket_48", "bone310", "bone311", "bone312", "bone313", "bone314", "bone315"},
            {"move_tjgc_Rocket_49", "bone208", "bone209", "bone210", "bone211", "bone212", "bone213"},
            {"move_tjgc_Rocket_50", "bone285", "bone286", "bone287", "bone288", "bone289", "bone290"},
        },
        { // BigRocket
            {"move_tjgc_BigRocket_1", "aim120-16"},
            {"move_tjgc_BigRocket_2", "aim120-21"},
            {"move_tjgc_BigRocket_3", "aim120-14"},
            {"move_tjgc_BigRocket_4", "aim120-19"},
            {"move_tjgc_BigRocket_5", "aim120-13"},
            {"move_tjgc_BigRocket_6", "aim120-17"},
            {"move_tjgc_BigRocket_7", "aim120-12"},
            {"move_tjgc_BigRocket_8", "aim120-18"},
            {"move_tjgc_BigRocket_9", "aim120-15"},
            {"move_tjgc_BigRocket_10", "aim120-20"},
        },
        { // AAMissile
            {"move_tjgc_AAMissile_1", "aim120-2"},
            {"move_tjgc_AAMissile_2", "aim120-7"},
            {"move_tjgc_AAMissile_3", "aim120-3"},
            {"move_tjgc_AAMissile_4", "aim120-8"},
            {"move_tjgc_AAMissile_5", "aim120-4"},
            {"move_tjgc_AAMissile_6", "aim120-9"},
            {"move_tjgc_AAMissile_7", "aim120-5"},
            {"move_tjgc_AAMissile_8", "aim120-10"},
            {"move_tjgc_AAMissile_9", "aim120-6"},
            {"move_tjgc_AAMissile_10", "aim120-11"},
        },
        { // ATMissile
            {"move_tjgc_ATMissile_1", "agm65-11"},
            {"move_tjgc_ATMissile_2", "agm65-12"},
            {"move_tjgc_ATMissile_3", "agm65-13"},
            {"move_tjgc_ATMissile_4", "agm65-14"},
            {"move_tjgc_ATMissile_5", "agm65-15"},
            {"move_tjgc_ATMissile_6", "agm65-1"},
            {"move_tjgc_ATMissile_7", "agm65-2"},
            {"move_tjgc_ATMissile_8", "agm65-3"},
            {"move_tjgc_ATMissile_9", "agm65-4"},
            {"move_tjgc_ATMissile_10", "agm65-5"},
        },
        { // BigATMissile
            {"move_tjgc_BigATMissile_1", "gbu24"},
            {"move_tjgc_BigATMissile_2", "gbu2"},
            {"move_tjgc_BigATMissile_3", "gbu5"},
            {"move_tjgc_BigATMissile_4", "gbu3"},
            {"move_tjgc_BigATMissile_5", "gbu4"},
        },
    };

    static boolean isVisible(int slot, int ammo, int capacity, int fireIndex, boolean bound) {
        int remaining = Math.max(0, Math.min(ammo, capacity));
        return bound ? slot < remaining
                : Math.floorMod(slot - Math.floorMod(fireIndex, capacity), capacity) < remaining;
    }
}
