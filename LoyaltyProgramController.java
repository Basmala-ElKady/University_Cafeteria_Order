
// public class LoyaltyProgramController implements
//         IPointsAdder, IPointsRedeemer, IPointsViewer, IRuleSetter, IOrderCancellationHandler {

//     private int studentId;
//     private int points;
//     private ILoyaltyRule rule;
//     private INotificationService notifier;
//     private IQLearning qLearningEngine;

//     public LoyaltyProgramController(int studentId, ILoyaltyRule rule,
//             INotificationService notifier, IQLearning qLearningEngine) {
//         this.studentId = studentId;
//         this.rule = rule;
//         this.notifier = notifier;
//         this.qLearningEngine = qLearningEngine;
//         this.points = 0;
//     }

//     @Override
//     public void addPoints(int orderTotal) {
//         int basePoints = rule.calculatePoints(orderTotal);
//         String state = "STUDENT_" + studentId + "POINTS" + points;
//         Action action = qLearningEngine.chooseAction(state);

//         String rewardDescription = "";

//         switch (action) {
//             case PRIORITIZE_PREPARING_FIRST:
//                 rewardDescription = "Student gets priority preparation reward (e.g., faster order)";
//                 break;
//             case PRIORITIZE_LARGEST_TOTAL:
//                 rewardDescription = "Student receives bonus for largest order";
//                 break;
//             case PRIORITIZE_OLDEST_PENDING:
//                 rewardDescription = "Student cannot redeem rewards temporarily";
//                 break;
//         }

//         points += basePoints;
//         notifier.sendNotification("Action: " + action + " -> Reward: " + rewardDescription);

//         String nextState = "STUDENT_" + studentId + "POINTS" + points;
//         qLearningEngine.update(state, action, basePoints, nextState);
//     }

//     @Override
//     public void redeemPoints(int pointsToRedeem) {
//         if (this.points >= pointsToRedeem) {
//             this.points -= pointsToRedeem;
//             notifier.sendNotification("Student redeemed points.");
//         } else {
//             notifier.sendNotification("Not enough points to redeem!");
//         }
//     }

//     @Override
//     public int getPoints() {
//         return points;
//     }

//     @Override
//     public void setRule(ILoyaltyRule rule) {
//         this.rule = rule;
//     }

//     @Override
//     public void handleCancelledOrder(int orderId) {
//         notifier.notifyOrderCancelled(orderId);
//         String state = "STUDENT_" + studentId + "_ORDER_CANCELLED";
//         qLearningEngine.update(state, Action.PRIORITIZE_OLDEST_PENDING, 0, state);
//     }

//     @Override
//     public int calculatePoints(double totalPrice) {
//         return rule.calculatePoints(totalPrice);
//     }
// }


public class LoyaltyProgramController implements IPointsAdder, IPointsRedeemer, IPointsViewer, IRuleSetter, IOrderCancellationHandler {

    private int studentId;
    private int points;
    private ILoyaltyRule rule;
    private INotificationService notifier;
    private IQLearning qLearningEngine;
    private StudentManagement studentManagement; // Added to sync with database

    public LoyaltyProgramController(int studentId, ILoyaltyRule rule, INotificationService notifier, IQLearning qLearningEngine, StudentManagement studentManagement) {
        this.studentId = studentId;
        this.rule = rule;
        this.notifier = notifier;
        this.qLearningEngine = qLearningEngine;
        this.studentManagement = studentManagement;
        this.points = syncPointsFromDatabase(); // Initialize points from database
    }

    // Added: Sync points from database
    private int syncPointsFromDatabase() {
        Student student = studentManagement.getStudent(studentId);
        return (student != null) ? (int) student.getLoyaltyPoints() : 0;
    }

    @Override
    public void addPoints(int orderTotal) {
        int basePoints = rule.calculatePoints(orderTotal);
        String state = "STUDENT_" + studentId + "POINTS" + points;
        Action action = qLearningEngine.chooseAction(state);

        String rewardDescription = "";

        switch (action) {
            case PRIORITIZE_PREPARING_FIRST:
                rewardDescription = "Student gets priority preparation reward (e.g., faster order)";
                break;
            case PRIORITIZE_LARGEST_TOTAL:
                rewardDescription = "Student receives bonus for largest order";
                break;
            case PRIORITIZE_OLDEST_PENDING:
                rewardDescription = "Student cannot redeem rewards temporarily";
                break;
        }

        points += basePoints;
        studentManagement.addLoyaltyPoints(studentId, basePoints); // Update database
        notifier.sendNotification("Action: " + action + " -> Reward: " + rewardDescription);

        String nextState = "STUDENT_" + studentId + "POINTS" + points;
        qLearningEngine.update(state, action, basePoints, nextState);
    }

    @Override
    public void redeemPoints(int pointsToRedeem) {
        if (this.points >= pointsToRedeem) {
            this.points -= pointsToRedeem;
            studentManagement.redeemLoyaltyPoints(studentId, pointsToRedeem); // Update database
            notifier.sendNotification("Student redeemed " + pointsToRedeem + " points.");
        } else {
            notifier.sendNotification("Not enough points to redeem!");
        }
    }

    @Override
    public int getPoints() {
        return points; // Now synced with database via syncPointsFromDatabase and updates
    }

    @Override
    public void setRule(ILoyaltyRule rule) {
        this.rule = rule;
    }

    @Override
    public void handleCancelledOrder(int orderId) {
        notifier.notifyOrderCancelled(orderId);
        String state = "STUDENT_" + studentId + "_ORDER_CANCELLED";
        qLearningEngine.update(state, Action.PRIORITIZE_OLDEST_PENDING, 0, state);
    }

    @Override
    public int calculatePoints(double totalPrice) {
        return rule.calculatePoints(totalPrice);
    }
}