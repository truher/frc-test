package org.truher.radar.model;

import java.util.ArrayList;
import java.util.List;

public class TargetList {
    public final List<Target> targets = new ArrayList<Target>();

    public void update(TargetList update) {
        targets.clear();
        targets.addAll(update.targets);
    }
    
    @Override
    public String toString() {
        return "TargetList [targets=" + targets + "]";
    }
}
