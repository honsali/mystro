package app.common.model;

public final class NativeAspect {
    private String left;
    private String aspect;
    private String right;
    private Double orb;
    private String motion;

    public NativeAspect() {
    }

    public NativeAspect(String left, String aspect, String right) {
        this(left, aspect, right, null, null);
    }

    public NativeAspect(String left, String aspect, String right, Double orb, String motion) {
        this.left = left;
        this.aspect = aspect;
        this.right = right;
        this.orb = orb;
        this.motion = motion;
    }

    public String left() {
        return left;
    }

    public void left(String left) {
        this.left = left;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String aspect() {
        return aspect;
    }

    public void aspect(String aspect) {
        this.aspect = aspect;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public String right() {
        return right;
    }

    public void right(String right) {
        this.right = right;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public Double orb() {
        return orb;
    }

    public void orb(Double orb) {
        this.orb = orb;
    }

    public Double getOrb() {
        return orb;
    }

    public void setOrb(Double orb) {
        this.orb = orb;
    }

    public String motion() {
        return motion;
    }

    public void motion(String motion) {
        this.motion = motion;
    }

    public String getMotion() {
        return motion;
    }

    public void setMotion(String motion) {
        this.motion = motion;
    }
}
