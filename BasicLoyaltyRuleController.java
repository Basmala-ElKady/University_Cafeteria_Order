

public class BasicLoyaltyRuleController implements ILoyaltyRule {
    private double baseRate;

    public BasicLoyaltyRuleController(double baseRate) {
        this.baseRate = baseRate;
    }

    @Override
    public int calculatePoints(double orderTotal) {
        return (int) (orderTotal * baseRate);
    }
}