package com.compiler.ir;

import java.util.HashSet;
import java.util.Set;

public class PhiFunction {
    private String source = null;
    private Set<String> names = new HashSet<>();


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Set<String> getNames() {
        return names;
    }

    public void addName(String name) {
        names.add(name);
    }

    @Override
    public String toString() {
        return source + " = phi (" + String.join(", ", names) + ")";
    }
}
