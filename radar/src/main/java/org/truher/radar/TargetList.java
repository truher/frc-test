package org.truher.radar;

import java.util.ArrayList;
import java.util.List;

public class TargetList {
    public List<Target> targets = new ArrayList<Target>();

    @Override
    public String toString() {
        return "TargetList [targets=" + targets + "]";
    }
}
