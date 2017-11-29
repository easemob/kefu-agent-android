package lecho.lib.hellocharts.animation;

public interface PieChartRotationAnimator {

    int FAST_ANIMATION_DURATION = 200;

    void startAnimation(float startAngle, float angleToRotate);

    void cancelAnimation();

    boolean isAnimationStarted();

    void setChartAnimationListener(ChartAnimationListener animationListener);

}
