package app.common.model;

import java.time.OffsetDateTime;

public final class NativeSyzygy {
    private String phase;
    private OffsetDateTime syzygyDateTime;

    public NativeSyzygy() {}

    public NativeSyzygy(String phase, OffsetDateTime syzygyDateTime) {
        this.phase = phase;
        this.syzygyDateTime = syzygyDateTime;
    }

    public String phase() {
        return phase;
    }

    public void phase(String phase) {
        this.phase = phase;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public OffsetDateTime syzygyDateTime() {
        return syzygyDateTime;
    }

    public void syzygyDateTime(OffsetDateTime syzygyDateTime) {
        this.syzygyDateTime = syzygyDateTime;
    }

    public OffsetDateTime getSyzygyDateTime() {
        return syzygyDateTime;
    }

    public void setSyzygyDateTime(OffsetDateTime syzygyDateTime) {
        this.syzygyDateTime = syzygyDateTime;
    }
}
