package ai.foodscan.aggregate.db.model.api;

public enum ActivityType {
    WALKING("Walking", 4),      // kcal per minute
    RUNNING("Running", 10),     // kcal per minute
    BICYCLING("Bicycling", 8);  // kcal per minute

    private final String displayName;
    private final int kcalPerMinute;

    ActivityType(String displayName, int kcalPerMinute) {
        this.displayName = displayName;
        this.kcalPerMinute = kcalPerMinute;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getKcalPerMinute() {
        return kcalPerMinute;
    }
}
