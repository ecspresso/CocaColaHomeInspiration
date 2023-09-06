package com.ecspresso.mau;

public enum Building {
    NIAGARA("Niagara", "FLIK-0017"),
    G8("Gäddan", "FLIK_0003");

    private final String name;
    private final String flik;

    Building(String name, String flik) {
        this.name = name;
        this.flik = flik;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getFlik() {
        return flik;
    }

    public static Building convertString(String buildingName) {
        switch(buildingName) {
            // Alla giltiga alternativ.
            case "n" -> {
                return Building.NIAGARA;
            }
            case "g8" -> {
                return Building.G8;
            }
            default -> {
                return null;
            }
        }
    }

}
