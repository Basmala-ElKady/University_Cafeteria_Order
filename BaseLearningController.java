import java.util.List;

public class BaseLearningController implements IBaseLearning {
    protected double alpha;
    protected double gamma;
    protected double epsilon;

    public BaseLearningController(double alpha, double gamma, double epsilon) {
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
    }

    @Override
    public IBaseLearning alpha(double a) {
        this.alpha = a;
        return this;
    }

    @Override
    public IBaseLearning gamma(double g) {
        this.gamma = g;
        return this;
    }

    @Override
    public IBaseLearning epsilon(double e) {
        this.epsilon = e;
        return this;
    }

    @Override
    public void decayEpsilon(double factor) {
        epsilon = Math.max(0.01, epsilon * factor);
    }

    public void update(String state, Action action, double reward, String nextState) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    public List<String> prioritize(List<IOrder> orders, Action action) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'prioritize'");
    }
    
}
