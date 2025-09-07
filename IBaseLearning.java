

public interface IBaseLearning {
    IBaseLearning alpha(double a);
    IBaseLearning gamma(double g);
    IBaseLearning epsilon(double e);
    void decayEpsilon(double factor);
}
