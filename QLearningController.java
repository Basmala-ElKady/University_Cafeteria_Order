
import java.util.*;

public class QLearningController extends BaseLearningController implements IQLearning {

    private final Map<String, EnumMap<Action, Double>> qTable = new HashMap<>();
    private final Random rnd = new Random();

    public QLearningController(double alpha, double gamma, double epsilon) {
        super(alpha, gamma, epsilon);
    }

    @Override
    public String encodeState(List<IOrder> orders) {
    long p = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
    long r = orders.stream().filter(o -> o.getStatus() == OrderStatus.READY_FOR_PICKUP).count();
    long pr = orders.stream().filter(o -> o.getStatus() == OrderStatus.PREPARING).count();
    return "P:" + p + "|PR:" + pr + "|R:" + r;
    }

    @Override
    public Action chooseAction(String state) {
        ensureState(state);
        if (rnd.nextDouble() < epsilon) {
            Action[] A = Action.values();
            return A[rnd.nextInt(A.length)];
        }
        return qTable.get(state).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Action.PRIORITIZE_OLDEST_PENDING);
    }

    @Override
    public void update(String state, Action action, double reward, String nextState) {
        ensureState(state);
        ensureState(nextState);
        double q = qTable.get(state).get(action);
        double maxNext = qTable.get(nextState).values().stream().mapToDouble(x -> x).max().orElse(0.0);
        double updated = q + alpha * (reward + gamma * maxNext - q);
        qTable.get(state).put(action, updated);
    }

    private void ensureState(String s) {
        qTable.computeIfAbsent(s, k -> {
            EnumMap<Action, Double> m = new EnumMap<>(Action.class);
            for (Action a : Action.values()) m.put(a, 0.0);
            return m;
        });
    }

    @Override
    public List<String> prioritize(List<IOrder> orders, Action action) {
        List<IOrder> sorted = new ArrayList<>(orders);

        switch (action) {
            case PRIORITIZE_PREPARING_FIRST:
                sorted.sort(Comparator.comparing(o -> {
                    switch (o.getStatus()) {
                        case PREPARING:
                            return 0;
                        case PENDING:
                            return 1;
                        case READY_FOR_PICKUP:
                            return 2;
                        default:
                            return 3;
                    }
                }));
                break;

            case PRIORITIZE_LARGEST_TOTAL:
                sorted.sort((o1, o2) -> Double.compare(o2.getTotalPrice(), o1.getTotalPrice()));
                break;

            case PRIORITIZE_OLDEST_PENDING:
                sorted.sort(Comparator.comparing(IOrder::getCreatedAt));
                break;
        }

        List<String> result = new ArrayList<>();
        for (IOrder o : sorted) {
            result.add(o.getOrderId());
        }
        return result;
    }
}