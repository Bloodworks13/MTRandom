package DiscreteMurTree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Branch {
    HashSet<Feature> branchLeft;
    HashSet<Feature> branchRight;
    private int hashCode;

    public Branch() {
        branchLeft = new HashSet<>();
        branchRight = new HashSet<>();
        hashCode = Objects.hash(branchLeft, branchRight);
    }

    public void addFeature(Feature f, boolean right) {
        if(!right) {
            branchLeft.add(f);
        }
        else {
            branchRight.add(f);
        }
        hashCode = Objects.hash(branchLeft, branchRight);
    }

    public HashSet<Feature> getBranchLeft() {
        return branchLeft;
    }

    public HashSet<Feature> getBranchRight() {
        return branchRight;
    }

    public Branch newBranchLeft(Feature f) {
        Branch newBranch = new Branch();
        for(Feature lf : branchLeft) {
            newBranch.addFeature(lf, false);
        }
        for(Feature rf : branchRight) {
            newBranch.addFeature(rf, true);
        }
        newBranch.addFeature(f, false);
        return newBranch;
    }

    public Branch newBranchRight(Feature f) {
        Branch newBranch = new Branch();
        for(Feature lf : branchLeft) {
            newBranch.addFeature(lf, false);
        }
        for(Feature rf : branchRight) {
            newBranch.addFeature(rf, true);
        }
        newBranch.addFeature(f, true);
        return newBranch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Branch that = (Branch) o;
        HashSet<Feature> otherLeft = that.getBranchLeft();
        HashSet<Feature> otherRight = that.getBranchRight();

        if(otherLeft.size() != branchLeft.size()) {
            return false;
        }

        if(otherRight.size() != branchRight.size()) {
            return false;
        }

        if(!otherLeft.equals(branchLeft)) {
            return false;
        }

        if(!otherRight.equals(branchRight)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
